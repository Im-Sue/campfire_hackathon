package cn.iocoder.yudao.module.market.service.event;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 事件市场过滤服务
 * 
 * 用于过滤出有效的市场（有交易量和流动性的市场）
 */
public interface EventMarketFilterService {

    /**
     * 获取指定事件的有效市场 ID 列表（Polymarket ID）
     * 按交易量降序排列
     * 从缓存读取，缓存未命中则调用 Polymarket API 并缓存
     *
     * @param polymarketEventId Polymarket 事件 ID
     * @return 有效市场的 Polymarket ID 列表（按交易量降序）
     */
    List<String> getValidMarketIds(String polymarketEventId);

    /**
     * 批量获取多个事件的有效市场 ID
     * 使用并发调用 Polymarket API，提高性能
     * 返回的市场列表按交易量降序排列
     *
     * @param polymarketEventIds Polymarket 事件 ID 列表
     * @return Map: polymarketEventId -> 有效市场 Polymarket ID 列表（按交易量降序）
     */
    Map<String, List<String>> batchGetValidMarketIds(List<String> polymarketEventIds);

    /**
     * 清除指定事件的市场缓存
     *
     * @param polymarketEventId Polymarket 事件 ID
     */
    void invalidateCache(String polymarketEventId);

    /**
     * 获取事件的交易量（从缓存）
     *
     * @param polymarketEventId Polymarket 事件 ID
     * @return 交易量，缓存未命中返回 null
     */
    BigDecimal getEventVolume(String polymarketEventId);

    /**
     * 批量获取事件的交易量（从缓存）
     *
     * @param polymarketEventIds Polymarket 事件 ID 列表
     * @return Map: polymarketEventId -> volume
     */
    Map<String, BigDecimal> batchGetEventVolumes(List<String> polymarketEventIds);

}
