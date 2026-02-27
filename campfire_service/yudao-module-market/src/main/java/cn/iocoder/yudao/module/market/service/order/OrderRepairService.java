package cn.iocoder.yudao.module.market.service.order;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.market.dal.dataobject.order.PmOrderDO;
import cn.iocoder.yudao.module.market.dal.dataobject.position.PmPositionDO;
import cn.iocoder.yudao.module.market.dal.mysql.order.PmOrderMapper;
import cn.iocoder.yudao.module.market.enums.OrderSideEnum;
import cn.iocoder.yudao.module.market.enums.OrderStatusEnum;
import cn.iocoder.yudao.module.market.service.position.PmPositionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * 订单数据修复服务
 * 
 * 用于修复已成交但持仓未创建的订单
 */
@Service
@Slf4j
public class OrderRepairService {

    @Resource
    private PmOrderMapper pmOrderMapper;

    @Resource
    private PmPositionService pmPositionService;

    /**
     * 修复所有不一致的订单
     * 
     * @return 修复的订单数量
     */
    @Transactional(rollbackFor = Exception.class)
    public int repairAllInconsistentOrders() {
        log.info("[repairAllInconsistentOrders][开始修复不一致订单]");

        // 查询所有已成交的买入订单
        List<PmOrderDO> filledBuyOrders = pmOrderMapper.selectList(
                new LambdaQueryWrapperX<PmOrderDO>()
                        .eq(PmOrderDO::getStatus, OrderStatusEnum.FILLED.getStatus())
                        .eq(PmOrderDO::getSide, OrderSideEnum.BUY.getSide())
                        .isNotNull(PmOrderDO::getFilledQuantity)
                        .gt(PmOrderDO::getFilledQuantity, BigDecimal.ZERO));

        log.info("[repairAllInconsistentOrders][找到 {} 个已成交的买入订单]", filledBuyOrders.size());

        int repairedCount = 0;
        int skippedCount = 0;
        int failedCount = 0;

        for (PmOrderDO order : filledBuyOrders) {
            try {
                boolean repaired = repairSingleOrder(order);
                if (repaired) {
                    repairedCount++;
                } else {
                    skippedCount++;
                }
            } catch (Exception e) {
                failedCount++;
                log.error("[repairAllInconsistentOrders][修复订单失败 orderId={}, orderNo={}]",
                        order.getId(), order.getOrderNo(), e);
            }
        }

        log.info("[repairAllInconsistentOrders][修复完成: 成功={}, 跳过={}, 失败={}]",
                repairedCount, skippedCount, failedCount);

        return repairedCount;
    }

    /**
     * 修复单个订单
     * 
     * @param order 订单
     * @return true=已修复, false=无需修复
     */
    private boolean repairSingleOrder(PmOrderDO order) {
        // 检查是否存在持仓
        PmPositionDO position = pmPositionService.getPosition(
                order.getUserId(),
                order.getMarketId(),
                order.getOutcome());

        // 情况1: 持仓不存在,需要创建
        if (position == null) {
            log.warn("[repairSingleOrder][订单已成交但持仓不存在 orderId={}, orderNo={}, userId={}, marketId={}, outcome={}]",
                    order.getId(), order.getOrderNo(), order.getUserId(), order.getMarketId(), order.getOutcome());

            pmPositionService.addPosition(
                    order.getUserId(),
                    order.getWalletAddress(),
                    order.getMarketId(),
                    order.getOutcome(),
                    order.getFilledQuantity(),
                    order.getFilledPrice(),
                    order.getFilledAmount());

            log.info("[repairSingleOrder][已创建持仓 orderId={}, quantity={}]",
                    order.getId(), order.getFilledQuantity());
            return true;
        }

        // 情况2: 持仓存在但份数为0,可能是被卖光后的记录,需要检查
        if (position.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
            log.warn("[repairSingleOrder][订单已成交但持仓份数为0 orderId={}, orderNo={}, positionId={}]",
                    order.getId(), order.getOrderNo(), position.getId());

            // 检查是否有后续的卖出订单
            List<PmOrderDO> sellOrders = pmOrderMapper.selectList(
                    new LambdaQueryWrapperX<PmOrderDO>()
                            .eq(PmOrderDO::getUserId, order.getUserId())
                            .eq(PmOrderDO::getMarketId, order.getMarketId())
                            .eq(PmOrderDO::getOutcome, order.getOutcome())
                            .eq(PmOrderDO::getSide, OrderSideEnum.SELL.getSide())
                            .eq(PmOrderDO::getStatus, OrderStatusEnum.FILLED.getStatus())
                            .gt(PmOrderDO::getFilledAt, order.getFilledAt()));

            if (!sellOrders.isEmpty()) {
                log.info("[repairSingleOrder][持仓为0是因为后续有卖出订单,无需修复 orderId={}]", order.getId());
                return false;
            }

            // 没有卖出订单,说明持仓应该存在,需要修复
            log.warn("[repairSingleOrder][持仓为0但没有卖出订单,可能是数据异常,补充持仓 orderId={}]", order.getId());

            // 更新持仓份数
            position.setQuantity(order.getFilledQuantity());
            position.setAvgPrice(order.getFilledPrice());
            position.setTotalCost(order.getFilledAmount());
            pmPositionService.addPosition(
                    order.getUserId(),
                    order.getWalletAddress(),
                    order.getMarketId(),
                    order.getOutcome(),
                    order.getFilledQuantity(),
                    order.getFilledPrice(),
                    order.getFilledAmount());

            log.info("[repairSingleOrder][已更新持仓 orderId={}, quantity={}]",
                    order.getId(), order.getFilledQuantity());
            return true;
        }

        // 情况3: 持仓正常,无需修复
        return false;
    }

    /**
     * 检查特定订单是否需要修复
     * 
     * @param orderNo 订单号
     * @return 检查结果描述
     */
    public String checkOrder(String orderNo) {
        PmOrderDO order = pmOrderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return "订单不存在: " + orderNo;
        }

        if (!OrderStatusEnum.FILLED.getStatus().equals(order.getStatus())) {
            return String.format("订单状态不是已成交: %s (status=%d)", orderNo, order.getStatus());
        }

        if (!OrderSideEnum.BUY.getSide().equals(order.getSide())) {
            return String.format("订单不是买入订单: %s (side=%s)", orderNo, order.getSide());
        }

        PmPositionDO position = pmPositionService.getPosition(
                order.getUserId(),
                order.getMarketId(),
                order.getOutcome());

        if (position == null) {
            return String.format("❌ 需要修复: 订单已成交但持仓不存在\n" +
                    "  订单号: %s\n" +
                    "  用户ID: %d\n" +
                    "  市场ID: %d\n" +
                    "  选项: %s\n" +
                    "  份数: %s\n" +
                    "  价格: %s\n" +
                    "  金额: %d",
                    orderNo, order.getUserId(), order.getMarketId(), order.getOutcome(),
                    order.getFilledQuantity(), order.getFilledPrice(), order.getFilledAmount());
        }

        if (position.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
            return String.format("⚠️ 持仓份数为0,需要进一步检查\n" +
                    "  订单号: %s\n" +
                    "  持仓ID: %d\n" +
                    "  建议: 检查是否有后续卖出订单",
                    orderNo, position.getId());
        }

        return String.format("✅ 订单和持仓数据一致\n" +
                "  订单号: %s\n" +
                "  持仓ID: %d\n" +
                "  持仓份数: %s",
                orderNo, position.getId(), position.getQuantity());
    }
}
