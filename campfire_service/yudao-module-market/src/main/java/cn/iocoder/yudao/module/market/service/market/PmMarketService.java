package cn.iocoder.yudao.module.market.service.market;

import cn.iocoder.yudao.module.market.dal.dataobject.market.PmMarketDO;

import java.util.List;

/**
 * 预测市场信息 Service 接口
 */
public interface PmMarketService {

    /**
     * 创建市场
     *
     * @param market 市场
     * @return 市场编号
     */
    Long createMarket(PmMarketDO market);

    /**
     * 批量创建市场
     *
     * @param markets 市场列表
     */
    void createMarketBatch(List<PmMarketDO> markets);

    /**
     * 更新市场
     *
     * @param market 市场
     */
    void updateMarket(PmMarketDO market);

    /**
     * 获取市场
     *
     * @param id 市场编号
     * @return 市场
     */
    PmMarketDO getMarket(Long id);

    /**
     * 根据 Polymarket ID 获取市场
     *
     * @param polymarketId Polymarket ID
     * @return 市场
     */
    PmMarketDO getMarketByPolymarketId(String polymarketId);

    /**
     * 获取事件下的所有市场
     *
     * @param eventId 事件编号
     * @return 市场列表
     */
    List<PmMarketDO> getMarketsByEventId(Long eventId);

    /**
     * 获取所有交易中的市场
     *
     * @return 市场列表
     */
    List<PmMarketDO> getTradingMarkets();

    /**
     * 获取所有待结算的市场
     *
     * @return 市场列表
     */
    List<PmMarketDO> getPendingSettlementMarkets();

    /**
     * 获取所有已封盘的市场
     *
     * @return 市场列表
     */
    List<PmMarketDO> getSuspendedMarkets();

    /**
     * 封盘市场
     *
     * @param id 市场编号
     */
    void suspendMarket(Long id);

    /**
     * 恢复市场交易
     *
     * @param id 市场编号
     */
    void resumeMarket(Long id);

    /**
     * 设置市场为待结算状态
     *
     * @param id            市场编号
     * @param winnerOutcome 获胜选项
     */
    void setPendingSettlement(Long id, String winnerOutcome);

    /**
     * 设置市场为已结算状态
     *
     * @param id 市场编号
     */
    void setSettled(Long id);

    /**
     * 获取市场价格（优先从缓存获取，否则从 Polymarket API 获取）
     *
     * @param id 市场编号
     * @return 价格映射 {yes: 价格, no: 价格}
     */
    java.util.Map<String, java.math.BigDecimal> getMarketPrices(Long id);

    /**
     * 批量获取市场信息
     *
     * @param ids 市场编号集合
     * @return 市场编号 -> 市场信息 映射
     */
    java.util.Map<Long, PmMarketDO> getMarketMap(java.util.Collection<Long> ids);

}
