package cn.iocoder.yudao.module.market.service.market;

import cn.iocoder.yudao.module.market.dal.dataobject.market.PmMarketDO;
import cn.iocoder.yudao.module.market.dal.mysql.market.PmMarketMapper;
import cn.iocoder.yudao.module.market.enums.MarketStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.market.enums.ErrorCodeConstants.*;

/**
 * 预测市场信息 Service 实现类
 */
@Service
@Validated
@Slf4j
public class PmMarketServiceImpl implements PmMarketService {

    @Resource
    private PmMarketMapper pmMarketMapper;

    @Override
    public Long createMarket(PmMarketDO market) {
        pmMarketMapper.insert(market);
        return market.getId();
    }

    @Override
    public void createMarketBatch(List<PmMarketDO> markets) {
        pmMarketMapper.insertBatch(markets);
    }

    @Override
    public void updateMarket(PmMarketDO market) {
        pmMarketMapper.updateById(market);
    }

    @Override
    public PmMarketDO getMarket(Long id) {
        return pmMarketMapper.selectById(id);
    }

    @Override
    public PmMarketDO getMarketByPolymarketId(String polymarketId) {
        return pmMarketMapper.selectByPolymarketId(polymarketId);
    }

    @Override
    public List<PmMarketDO> getMarketsByEventId(Long eventId) {
        List<PmMarketDO> markets = new java.util.ArrayList<>(pmMarketMapper.selectByEventId(eventId));

        // 二次排序：体育盘口类型，数据库层无法直接用 FIELD 函数
        markets.sort((a, b) -> {
            // 先按状态排序（已由数据库完成，这里保持一致）
            int statusCompare = Integer.compare(
                    a.getStatus() != null ? a.getStatus() : 99,
                    b.getStatus() != null ? b.getStatus() : 99);
            if (statusCompare != 0)
                return statusCompare;

            // 按盘口类型排序：moneyline > spreads > totals
            int typeCompare = Integer.compare(
                    getSportsMarketTypeOrder(a.getSportsMarketType()),
                    getSportsMarketTypeOrder(b.getSportsMarketType()));
            if (typeCompare != 0)
                return typeCompare;

            // 按盘口线值排序
            if (a.getLine() != null && b.getLine() != null) {
                int lineCompare = a.getLine().compareTo(b.getLine());
                if (lineCompare != 0)
                    return lineCompare;
            }

            // 按结束时间排序
            if (a.getEndDate() != null && b.getEndDate() != null) {
                int endDateCompare = a.getEndDate().compareTo(b.getEndDate());
                if (endDateCompare != 0)
                    return endDateCompare;
            }

            // 最后按 ID 排序
            return Long.compare(a.getId(), b.getId());
        });

        return markets;
    }

    /**
     * 获取体育盘口类型的排序权重
     * moneyline(独赢盘) > spreads(让分盘) > totals(大小分)
     */
    private int getSportsMarketTypeOrder(String type) {
        if (type == null || type.isEmpty())
            return 99;
        switch (type.toLowerCase()) {
            case "moneyline":
                return 1;
            case "spreads":
                return 2;
            case "totals":
                return 3;
            default:
                return 99;
        }
    }

    @Override
    public List<PmMarketDO> getTradingMarkets() {
        return pmMarketMapper.selectTradingMarkets();
    }

    @Override
    public List<PmMarketDO> getPendingSettlementMarkets() {
        return pmMarketMapper.selectPendingSettlementMarkets();
    }

    @Override
    public List<PmMarketDO> getSuspendedMarkets() {
        return pmMarketMapper.selectSuspendedMarkets();
    }

    @Override
    public void suspendMarket(Long id) {
        PmMarketDO market = validateMarketExists(id);

        if (!MarketStatusEnum.TRADING.getStatus().equals(market.getStatus())) {
            throw exception(MARKET_NOT_TRADING);
        }

        market.setStatus(MarketStatusEnum.SUSPENDED.getStatus());
        pmMarketMapper.updateById(market);

        log.info("[suspendMarket][市场 {} 封盘成功]", id);
    }

    @Override
    public void resumeMarket(Long id) {
        PmMarketDO market = validateMarketExists(id);

        if (!MarketStatusEnum.SUSPENDED.getStatus().equals(market.getStatus())) {
            throw exception(MARKET_SUSPENDED);
        }

        market.setStatus(MarketStatusEnum.TRADING.getStatus());
        pmMarketMapper.updateById(market);

        log.info("[resumeMarket][市场 {} 恢复交易]", id);
    }

    @Override
    public void setPendingSettlement(Long id, String winnerOutcome) {
        PmMarketDO market = validateMarketExists(id);

        market.setStatus(MarketStatusEnum.PENDING_SETTLEMENT.getStatus());
        market.setWinnerOutcome(winnerOutcome);
        pmMarketMapper.updateById(market);

        log.info("[setPendingSettlement][市场 {} 待结算，获胜选项: {}]", id, winnerOutcome);
    }

    @Override
    public void setSettled(Long id) {
        PmMarketDO market = validateMarketExists(id);

        market.setStatus(MarketStatusEnum.SETTLED.getStatus());
        market.setSettledAt(LocalDateTime.now());
        pmMarketMapper.updateById(market);

        log.info("[setSettled][市场 {} 已结算]", id);
    }

    private PmMarketDO validateMarketExists(Long id) {
        PmMarketDO market = pmMarketMapper.selectById(id);
        if (market == null) {
            throw exception(MARKET_NOT_EXISTS);
        }
        return market;
    }

    @Resource
    private cn.iocoder.yudao.module.market.service.price.PmPriceService priceService;

    @Override
    public java.util.Map<String, java.math.BigDecimal> getMarketPrices(Long id) {
        // log.info("[getMarketPrices][Service层 - 开始获取市场价格 marketId={}]", id);
        java.util.Map<String, java.math.BigDecimal> prices = new java.util.LinkedHashMap<>();

        // 先获取市场信息，检查状态
        PmMarketDO market = pmMarketMapper.selectById(id);
        if (market == null) {
            log.warn("[getMarketPrices][市场不存在 marketId={}]", id);
            return prices;
        }
        // log.info("[getMarketPrices][Service层 - 市场信息 status={}, winnerOutcome={}, outcomes={}, clobTokenIds={}]",
        //         market.getStatus(), market.getWinnerOutcome(), market.getOutcomes(), market.getClobTokenIds());

        // 如果市场已结算或待结算，直接返回结算价格，不调用 CLOB API
        if (MarketStatusEnum.SETTLED.getStatus().equals(market.getStatus())
                || MarketStatusEnum.PENDING_SETTLEMENT.getStatus().equals(market.getStatus())) {
            // log.info("[getMarketPrices][Service层 - 市场已结算/待结算，返回结算价格]");
            return getSettledPrices(market);
        }

        // 通过统一价格服务获取所有选项价格
        // log.info("[getMarketPrices][Service层 - 调用PriceService.getAllPrices]");
        java.util.Map<Integer, cn.iocoder.yudao.module.market.service.price.PriceInfo> allPrices = priceService
                .getAllPrices(id);
        // log.info("[getMarketPrices][Service层 - PriceService返回结果 allPrices.size={}, allPrices={}]",
        //         allPrices.size(), allPrices);

        for (cn.iocoder.yudao.module.market.service.price.PriceInfo info : allPrices.values()) {
            String outcomeName = info.getOutcomeName();
            if (outcomeName == null) {
                outcomeName = info.getOutcomeIndex() == 0 ? "Yes"
                        : (info.getOutcomeIndex() == 1 ? "No" : "Outcome" + info.getOutcomeIndex());
            }

            // 使用 bestAsk 作为展示价格（用户买入时的价格，与 Polymarket 官网一致）
            java.math.BigDecimal displayPrice = info.getBestAsk();
            if (displayPrice == null) {
                displayPrice = info.getBestBid(); // 回退到 bestBid
            }

            prices.put(outcomeName, displayPrice);
        }

        // log.info("[getMarketPrices][获取价格 marketId={}, prices={}]", id, prices);
        return prices;
    }

    /**
     * 获取已结算市场的价格（获胜方 1.00，其他 0.00）
     * 如果 winnerOutcome 未知，返回 null 价格表示不可用
     */
    private java.util.Map<String, java.math.BigDecimal> getSettledPrices(PmMarketDO market) {
        java.util.Map<String, java.math.BigDecimal> prices = new java.util.LinkedHashMap<>();

        java.util.List<String> outcomes = market.getOutcomes();
        String winnerOutcome = market.getWinnerOutcome();

        if (outcomes == null || outcomes.isEmpty()) {
            log.warn("[getSettledPrices][市场无 outcomes marketId={}]", market.getId());
            return prices;
        }

        // 如果 winnerOutcome 为空，说明数据还未同步，返回 null 价格
        if (winnerOutcome == null || winnerOutcome.isEmpty()) {
            log.debug("[getSettledPrices][已结算市场但 winnerOutcome 未知 marketId={}]", market.getId());
            for (String outcome : outcomes) {
                prices.put(outcome, null);
            }
            return prices;
        }

        for (String outcome : outcomes) {
            if (outcome.equals(winnerOutcome)) {
                prices.put(outcome, java.math.BigDecimal.ONE);
            } else {
                prices.put(outcome, java.math.BigDecimal.ZERO);
            }
        }

        log.debug("[getSettledPrices][已结算市场价格 marketId={}, winnerOutcome={}, prices={}]",
                market.getId(), winnerOutcome, prices);
        return prices;
    }

    @Override
    public java.util.Map<Long, PmMarketDO> getMarketMap(java.util.Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return java.util.Collections.emptyMap();
        }
        List<PmMarketDO> markets = pmMarketMapper.selectBatchIds(ids);
        return markets.stream().collect(java.util.stream.Collectors.toMap(PmMarketDO::getId, m -> m));
    }

}
