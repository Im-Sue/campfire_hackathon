package cn.iocoder.yudao.module.market.service.order;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.market.controller.app.order.vo.AppOrderCreateReqVO;
import cn.iocoder.yudao.module.market.dal.dataobject.market.PmMarketDO;
import cn.iocoder.yudao.module.market.dal.dataobject.order.PmOrderDO;
import cn.iocoder.yudao.module.market.dal.mysql.order.PmOrderMapper;
import cn.iocoder.yudao.module.market.enums.MarketStatusEnum;
import cn.iocoder.yudao.module.market.enums.OrderSideEnum;
import cn.iocoder.yudao.module.market.enums.OrderStatusEnum;
import cn.iocoder.yudao.module.market.enums.OrderTypeEnum;
import cn.iocoder.yudao.module.market.service.config.PmConfigService;
import cn.iocoder.yudao.module.market.service.market.PmMarketService;
import cn.iocoder.yudao.module.market.service.position.PmPositionService;
import cn.iocoder.yudao.module.market.service.price.PmPriceService;
import cn.iocoder.yudao.module.market.service.price.PriceInfo;
import cn.iocoder.yudao.module.point.enums.PointBizTypeEnum;
import cn.iocoder.yudao.module.point.enums.PointTransactionTypeEnum;
import cn.iocoder.yudao.module.point.service.PointService;
import cn.iocoder.yudao.module.wallet.dal.dataobject.WalletUserDO;
import cn.iocoder.yudao.module.wallet.service.WalletUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.market.enums.ErrorCodeConstants.*;

/**
 * 预测市场订单 Service 实现类
 */
@Service
@Validated
@Slf4j
public class PmOrderServiceImpl implements PmOrderService {

    @Resource
    private PmOrderMapper pmOrderMapper;

    @Resource
    private PmMarketService pmMarketService;

    @Resource
    private PmPositionService pmPositionService;

    @Resource
    private PmConfigService pmConfigService;

    @Resource
    private PointService pointService;

    @Resource
    private PmPriceService priceService;

    @Resource
    private WalletUserService walletUserService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(Long userId, AppOrderCreateReqVO createReqVO) {
        // 0. 查询用户钱包地址
        WalletUserDO walletUser = walletUserService.getUser(userId);
        String walletAddress = walletUser != null ? walletUser.getWalletAddress() : null;

        // 1. 校验市场状态
        PmMarketDO market = pmMarketService.getMarket(createReqVO.getMarketId());
        if (market == null) {
            throw exception(MARKET_NOT_EXISTS);
        }
        if (!MarketStatusEnum.TRADING.getStatus().equals(market.getStatus())) {
            throw exception(MARKET_NOT_TRADING);
        }

        // 2. 校验下单金额
        Integer minAmount = pmConfigService.getInteger("order.min_amount_points", 100);
        Integer maxAmount = pmConfigService.getInteger("order.max_amount_points", 1000000);

        // 3. 获取滑点设置
        BigDecimal slippage = createReqVO.getSlippageTolerance();
        if (slippage == null) {
            slippage = pmConfigService.getDecimal("order.default_slippage_tolerance", new BigDecimal("0.05"));
        }
        BigDecimal maxSlippage = pmConfigService.getDecimal("order.max_slippage_tolerance", new BigDecimal("0.20"));
        if (slippage.compareTo(maxSlippage) > 0) {
            slippage = maxSlippage;
        }

        // 4. 处理订单
        PmOrderDO order;
        if (OrderTypeEnum.MARKET.getType().equals(createReqVO.getOrderType())) {
            // 市价单
            order = processMarketOrder(userId, walletAddress, createReqVO, market, slippage);
        } else {
            // 限价单
            order = processLimitOrder(userId, walletAddress, createReqVO, market, slippage);
        }

        // 5. 校验敞口限制（仅买入时）
        if (OrderSideEnum.BUY.getSide().equals(createReqVO.getSide())) {
            BigDecimal maxExposure = BigDecimal.valueOf(pmConfigService.getInteger("market.max_net_exposure", 10000));
            BigDecimal currentExposure = pmPositionService.getNetExposure(market.getId());
            if (currentExposure.add(order.getQuantity()).compareTo(maxExposure) > 0) {
                throw exception(ORDER_EXPOSURE_EXCEEDED);
            }
        }

        // 6. 保存订单
        pmOrderMapper.insert(order);
        log.info("[createOrder][创建订单 orderId={}, orderNo={}, type={}, side={}]",
                order.getId(), order.getOrderNo(), order.getOrderType(), order.getSide());

        return order.getId();
    }

    private PmOrderDO processMarketOrder(Long userId, String walletAddress, AppOrderCreateReqVO req, PmMarketDO market,
            BigDecimal slippage) {
        // 1. 市价单强制使用 amount（积分），忽略 quantity
        if (req.getAmount() == null || req.getAmount() <= 0) {
            throw exception(ORDER_AMOUNT_REQUIRED);
        }

        // 2. 从缓存/API 获取实时价格
        PriceInfo priceInfo = priceService.getPriceByOutcomeName(market.getId(), req.getOutcome());
        if (!priceInfo.isValid()) {
            throw exception(MARKET_PRICE_NOT_AVAILABLE);
        }

        // 3. 确定成交价格：买入用 bestAsk，卖出用 bestBid（不使用 midPrice fallback）
        BigDecimal currentPrice;
        if (OrderSideEnum.BUY.getSide().equals(req.getSide())) {
            currentPrice = priceInfo.getBestAsk();
        } else {
            currentPrice = priceInfo.getBestBid();
        }

        if (currentPrice == null || currentPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(MARKET_PRICE_NOT_AVAILABLE);
        }

        // 4. 计算最大可接受价格（滑点保护）
        BigDecimal maxPrice = currentPrice.multiply(BigDecimal.ONE.add(slippage));

        // 5. 买入时检查滑点（卖出时不需要，因为价格下跌对卖方有利）
        if (OrderSideEnum.BUY.getSide().equals(req.getSide())) {
            if (currentPrice.compareTo(maxPrice) > 0) {
                throw exception(ORDER_SLIPPAGE_EXCEEDED);
            }
        }

        // 6. 金额反算份额：份额 = 积分 ÷ (价格 × 100)，保留 6 位小数
        BigDecimal quantity = BigDecimal.valueOf(req.getAmount())
                .divide(currentPrice.multiply(BigDecimal.valueOf(100)), 6, RoundingMode.DOWN);

        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(ORDER_AMOUNT_TOO_SMALL);
        }

        // 7. 计算实际扣减积分（向上取整）
        Long amount = currentPrice.multiply(quantity)
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.CEILING)
                .longValue();

        // 8. 处理积分和持仓
        if (OrderSideEnum.BUY.getSide().equals(req.getSide())) {
            // 买入：扣减积分
            pointService.deductPoints(userId, walletAddress, amount,
                    PointTransactionTypeEnum.MARKET_ORDER.getType(),
                    PointBizTypeEnum.MARKET.getCode(), null,
                    "预测市场买入", null);
        } else {
            // 卖出：先检查持仓是否足够
            cn.iocoder.yudao.module.market.dal.dataobject.position.PmPositionDO position = pmPositionService
                    .getPosition(userId, req.getMarketId(), req.getOutcome());
            if (position == null || position.getQuantity().compareTo(quantity) < 0) {
                throw exception(POSITION_INSUFFICIENT);
            }
            // 卖出：返还积分
            pointService.addPoints(userId, walletAddress, amount,
                    PointTransactionTypeEnum.MARKET_ORDER.getType(),
                    PointBizTypeEnum.MARKET.getCode(), null,
                    "预测市场卖出", null);
        }

        // 构建订单
        PmOrderDO order = PmOrderDO.builder()
                .orderNo(generateOrderNo())
                .userId(userId)
                .walletAddress(walletAddress)
                .marketId(req.getMarketId())
                .orderType(OrderTypeEnum.MARKET.getType())
                .side(req.getSide())
                .outcome(req.getOutcome())
                .price(currentPrice)
                .quantity(quantity)
                .amount(amount)
                .slippageTolerance(slippage)
                .filledQuantity(quantity)
                .filledAmount(amount)
                .filledPrice(currentPrice)
                .filledAt(LocalDateTime.now())
                .status(OrderStatusEnum.FILLED.getStatus())
                .build();

        // 更新持仓
        if (OrderSideEnum.BUY.getSide().equals(req.getSide())) {
            pmPositionService.addPosition(userId, walletAddress, req.getMarketId(), req.getOutcome(),
                    quantity, currentPrice, amount);
        } else {
            pmPositionService.reducePosition(userId, req.getMarketId(), req.getOutcome(),
                    quantity, currentPrice);
        }

        return order;
    }

    private PmOrderDO processLimitOrder(Long userId, String walletAddress, AppOrderCreateReqVO req, PmMarketDO market,
            BigDecimal slippage) {
        // 1. 限价单强制使用 quantity，忽略 amount
        if (req.getQuantity() == null || req.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(ORDER_QUANTITY_REQUIRED);
        }
        if (req.getPrice() == null) {
            throw new IllegalArgumentException("限价单必须指定价格");
        }

        // 2. 计算金额（积分 = 价格 × 份数 × 100）
        Long amount = req.getPrice().multiply(req.getQuantity())
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.CEILING)
                .longValue();

        // 3. 处理积分
        if (OrderSideEnum.BUY.getSide().equals(req.getSide())) {
            // 买入限价单：扣减积分（预冻结）
            pointService.deductPoints(userId, walletAddress, amount,
                    PointTransactionTypeEnum.MARKET_ORDER.getType(),
                    PointBizTypeEnum.MARKET.getCode(), null,
                    "预测市场限价买单", null);
        } else {
            // 卖出限价单：检查持仓是否足够（不扣减积分，成交时返还）
            cn.iocoder.yudao.module.market.dal.dataobject.position.PmPositionDO position = pmPositionService
                    .getPosition(userId, req.getMarketId(), req.getOutcome());
            if (position == null || position.getQuantity().compareTo(req.getQuantity()) < 0) {
                throw exception(POSITION_INSUFFICIENT);
            }
            // 卖出限价单下单时不操作积分，成交时再返还
        }

        // 4. 计算过期时间
        Integer maxDays = pmConfigService.getInteger("order.limit_order_max_days", 30);
        LocalDateTime expireAt = LocalDateTime.now().plusDays(maxDays);

        // 5. 构建订单
        return PmOrderDO.builder()
                .orderNo(generateOrderNo())
                .userId(userId)
                .walletAddress(walletAddress)
                .marketId(req.getMarketId())
                .orderType(OrderTypeEnum.LIMIT.getType())
                .side(req.getSide())
                .outcome(req.getOutcome())
                .price(req.getPrice())
                .quantity(req.getQuantity())
                .amount(amount)
                .slippageTolerance(slippage)
                .filledQuantity(BigDecimal.ZERO)
                .filledAmount(0L)
                .status(OrderStatusEnum.PENDING.getStatus())
                .expireAt(expireAt)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long userId, Long orderId) {
        PmOrderDO order = pmOrderMapper.selectById(orderId);
        if (order == null) {
            throw exception(ORDER_NOT_EXISTS);
        }
        if (!order.getUserId().equals(userId)) {
            throw exception(ORDER_NOT_EXISTS);
        }
        if (!OrderStatusEnum.PENDING.getStatus().equals(order.getStatus())) {
            throw exception(ORDER_CANNOT_CANCEL);
        }

        // 更新订单状态
        order.setStatus(OrderStatusEnum.CANCELLED.getStatus());
        order.setCancelReason("用户取消");
        pmOrderMapper.updateById(order);

        // 退还积分
        pointService.addPoints(userId, order.getWalletAddress(), order.getAmount(),
                PointTransactionTypeEnum.MARKET_CANCEL.getType(),
                PointBizTypeEnum.MARKET.getCode(), order.getId(),
                "取消订单退还", null);

        log.info("[cancelOrder][取消订单 orderId={}, amount={}]", orderId, order.getAmount());
    }

    @Override
    public PmOrderDO getOrder(Long id) {
        return pmOrderMapper.selectById(id);
    }

    @Override
    public PmOrderDO getOrderByOrderNo(String orderNo) {
        return pmOrderMapper.selectByOrderNo(orderNo);
    }

    @Override
    public List<PmOrderDO> getOrdersByUserId(Long userId) {
        return pmOrderMapper.selectByUserId(userId);
    }

    @Override
    public PageResult<PmOrderDO> getOrderPageByUserId(Long userId, PageParam pageParam) {
        return pmOrderMapper.selectPageByUserId(userId, pageParam);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processLimitOrders(Long marketId, java.util.Map<String, java.math.BigDecimal> outcomePrices) {
        List<PmOrderDO> pendingOrders = pmOrderMapper.selectPendingOrdersByMarketId(marketId);

        for (PmOrderDO order : pendingOrders) {
            // 根据订单的 outcome 获取对应的当前价格
            BigDecimal currentPrice = outcomePrices.get(order.getOutcome());
            if (currentPrice == null) {
                log.debug("[processLimitOrders][订单 {} 的选项 {} 没有价格数据，跳过]",
                        order.getId(), order.getOutcome());
                continue;
            }

            boolean shouldFill = false;

            // 买入限价单：当前价格 <= 挂单价格时成交
            if (OrderSideEnum.BUY.getSide().equals(order.getSide())) {
                shouldFill = currentPrice.compareTo(order.getPrice()) <= 0;
                log.debug(
                        "[processLimitOrders][买入订单 orderId={}, outcome={}, currentPrice={}, orderPrice={}, shouldFill={}]",
                        order.getId(), order.getOutcome(), currentPrice, order.getPrice(), shouldFill);
            }
            // 卖出限价单：当前价格 >= 挂单价格时成交
            else if (OrderSideEnum.SELL.getSide().equals(order.getSide())) {
                shouldFill = currentPrice.compareTo(order.getPrice()) >= 0;
                log.debug(
                        "[processLimitOrders][卖出订单 orderId={}, outcome={}, currentPrice={}, orderPrice={}, shouldFill={}]",
                        order.getId(), order.getOutcome(), currentPrice, order.getPrice(), shouldFill);
            }

            if (shouldFill) {
                fillLimitOrder(order, order.getPrice()); // MVP: 按挂单价成交
            }
        }
    }

    private void fillLimitOrder(PmOrderDO order, BigDecimal fillPrice) {
        // 1. 使用原子更新,确保只有一个线程能成功更新
        int updated = pmOrderMapper.atomicFillOrder(
                order.getId(),
                OrderStatusEnum.PENDING.getStatus(),
                order.getQuantity(),
                order.getAmount(),
                fillPrice,
                LocalDateTime.now());

        if (updated == 0) {
            // 订单已被其他线程处理(状态不是 PENDING)
            log.debug("[fillLimitOrder][订单 {} 已被处理,跳过]", order.getId());
            return;
        }

        // 2. 更新持仓和积分(如果失败,异常会自动抛出,事务自动回滚)
        if (OrderSideEnum.BUY.getSide().equals(order.getSide())) {
            // 买入成交:增加持仓(积分已在下单时扣减)
            pmPositionService.addPosition(order.getUserId(), order.getWalletAddress(),
                    order.getMarketId(), order.getOutcome(),
                    order.getQuantity(), fillPrice, order.getAmount());
        } else {
            // 卖出成交:先检查持仓是否仍然足够(防止重复卖出)
            cn.iocoder.yudao.module.market.dal.dataobject.position.PmPositionDO position = pmPositionService
                    .getPosition(order.getUserId(), order.getMarketId(), order.getOutcome());
            if (position == null || position.getQuantity().compareTo(order.getQuantity()) < 0) {
                log.warn("[fillLimitOrder][持仓不足,无法成交卖出订单 orderId={}, 当前持仓={}, 需要={}]",
                        order.getId(), position != null ? position.getQuantity() : BigDecimal.ZERO,
                        order.getQuantity());
                // 抛出异常,让事务自动回滚
                throw new RuntimeException("持仓不足,无法成交卖出订单");
            }

            // 计算返还金额(按成交价)
            Long sellAmount = fillPrice.multiply(order.getQuantity())
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(0, RoundingMode.FLOOR)
                    .longValue();

            // 返还积分
            pointService.addPoints(order.getUserId(), order.getWalletAddress(), sellAmount,
                    PointTransactionTypeEnum.MARKET_ORDER.getType(),
                    PointBizTypeEnum.MARKET.getCode(), order.getId(),
                    "预测市场限价卖出成交", null);

            // 减少持仓
            pmPositionService.reducePosition(order.getUserId(), order.getMarketId(),
                    order.getOutcome(), order.getQuantity(), fillPrice);
        }

        log.info("[fillLimitOrder][限价单成交 orderId={}, fillPrice={}]", order.getId(), fillPrice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processExpiredOrders() {
        List<PmOrderDO> pendingOrders = pmOrderMapper.selectPendingOrders();
        LocalDateTime now = LocalDateTime.now();

        for (PmOrderDO order : pendingOrders) {
            if (order.getExpireAt() != null && order.getExpireAt().isBefore(now)) {
                order.setStatus(OrderStatusEnum.EXPIRED.getStatus());
                order.setCancelReason("订单过期");
                pmOrderMapper.updateById(order);

                // 退还积分
                pointService.addPoints(order.getUserId(), order.getWalletAddress(), order.getAmount(),
                        PointTransactionTypeEnum.MARKET_CANCEL.getType(),
                        PointBizTypeEnum.MARKET.getCode(), order.getId(),
                        "订单过期退还", null);

                log.info("[processExpiredOrders][订单过期 orderId={}]", order.getId());
            }
        }
    }

    private String generateOrderNo() {
        return "PM" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    // ========== 管理端 ==========

    @Override
    public PageResult<PmOrderDO> getOrderPage(
            cn.iocoder.yudao.module.market.controller.admin.order.vo.OrderPageReqVO pageReqVO) {
        return pmOrderMapper.selectPage(pageReqVO);
    }

}
