package cn.iocoder.yudao.module.market.service.sync;

import cn.iocoder.yudao.module.market.api.dto.PolymarketEventDTO;

import java.util.List;

/**
 * Polymarket 同步服务
 * 
 * 负责从 Polymarket API 获取事件数据并导入到本地数据库
 */
public interface PolymarketSyncService {

    /**
     * 浏览 Polymarket 事件列表（支持分页）
     * 
     * @param category 分类: politics, sports, crypto
     * @param pageNo   页码（从1开始）
     * @param pageSize 每页条数
     * @return Polymarket 事件列表（不保存到本地）
     */
    List<PolymarketEventDTO> browseEvents(String category, int pageNo, int pageSize);

    /**
     * 导入 Polymarket 事件到本地数据库
     * 
     * 会同时导入该事件下的所有 Markets
     * 
     * @param polymarketEventId Polymarket Event ID
     * @return 本地 Event ID
     */
    Long importEvent(String polymarketEventId);

    /**
     * 检查事件是否已导入
     * 
     * @param polymarketEventId Polymarket Event ID
     * @return 是否已导入
     */
    boolean isEventImported(String polymarketEventId);

    /**
     * 向已存在的事件增量添加市场（由 WebSocket new_market 消息触发）
     * 
     * @param polymarketEventId  Polymarket Event ID
     * @param polymarketMarketId Polymarket Market ID (市场的 id 字段)
     * @param conditionId        Market Condition ID
     * @param question           市场问题
     * @param outcomes           选项列表
     * @param tokenIds           Token IDs (用于 WS 订阅)
     * @return 本地 Market ID，如果事件不存在或市场已存在则返回 null
     */
    Long addMarketToEvent(String polymarketEventId, String polymarketMarketId,
            String conditionId, String question,
            List<String> outcomes, List<String> tokenIds);

    /**
     * 搜索 Polymarket 事件
     *
     * @param keyword 搜索关键字
     * @param limit   返回数量限制
     * @return 事件列表（标记是否已导入）
     */
    List<PolymarketEventDTO> searchEvents(String keyword, int limit);

}
