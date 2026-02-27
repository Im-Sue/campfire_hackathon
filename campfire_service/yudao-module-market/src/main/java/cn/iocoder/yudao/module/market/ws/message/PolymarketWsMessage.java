package cn.iocoder.yudao.module.market.ws.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Polymarket WebSocket 消息
 * 
 * 兼容新旧两种格式：
 * - 旧格式：使用 event 字段
 * - 新格式（2025-09-15 后）：使用 event_type 字段和 price_changes 数组
 */
@Data
public class PolymarketWsMessage {

    /**
     * 消息类型（旧格式）
     */
    private String event;

    /**
     * 消息类型（新格式）: price_change, last_trade_price, best_bid_ask, market_resolved,
     * book
     */
    @JsonProperty("event_type")
    private String eventType;

    /**
     * 资产 ID (Token ID)
     */
    @JsonProperty("asset_id")
    private String assetId;

    /**
     * 市场 ID
     */
    @JsonProperty("market")
    private String market;

    /**
     * 时间戳
     */
    private String timestamp;

    /**
     * 价格变化数组（新格式 price_change）
     */
    @JsonProperty("price_changes")
    private List<PriceChange> priceChanges;

    // ========== 旧格式字段（兼容） ==========
    // 注意：PriceData price 字段已移除，因为新 API 使用 price_changes 数组

    /**
     * 成交数据（旧格式）
     */
    private TradeData trade;

    /**
     * 结算数据
     */
    private SettlementData settlement;

    // ========== new_market 消息字段 ==========

    /**
     * 市场 ID（new_market 消息中的 id 字段）
     */
    private String id;

    /**
     * 市场问题
     */
    private String question;

    /**
     * 市场 slug
     */
    private String slug;

    /**
     * 市场描述
     */
    private String description;

    /**
     * 资产 IDs (Token IDs)
     */
    @JsonProperty("assets_ids")
    private List<String> assetsIds;

    /**
     * 选项列表
     */
    private List<String> outcomes;

    /**
     * 事件信息（new_market 消息中的嵌套对象）
     */
    @JsonProperty("event_message")
    private EventMessage eventMessage;

    // ========== last_trade_price / best_bid_ask 消息字段 ==========

    /**
     * 价格（字符串格式，用于 last_trade_price 消息）
     * 注意：与 PriceData price 会有字段名冲突，Jackson 会优先映射到此字段（String 类型）
     */
    private String price;

    /**
     * 成交数量
     */
    private String size;

    /**
     * 交易方向: BUY/SELL
     */
    private String side;

    /**
     * 手续费率（基点）
     */
    @JsonProperty("fee_rate_bps")
    private String feeRateBps;

    /**
     * 交易哈希
     */
    @JsonProperty("transaction_hash")
    private String transactionHash;

    // ========== best_bid_ask 消息字段 ==========

    /**
     * 最佳买入价
     */
    @JsonProperty("best_bid")
    private String bestBid;

    /**
     * 最佳卖出价
     */
    @JsonProperty("best_ask")
    private String bestAsk;

    /**
     * 价差
     */
    private String spread;

    // ========== 兼容方法 ==========

    /**
     * 获取有效的事件类型（兼容新旧格式）
     */
    public String getEffectiveEventType() {
        if (eventType != null && !eventType.isEmpty()) {
            return eventType;
        }
        return event;
    }

    // ========== 内部类 ==========

    /**
     * 价格变化项（新格式）
     */
    @Data
    public static class PriceChange {
        @JsonProperty("asset_id")
        private String assetId;

        private String price;
        private String size;
        private String side;
        private String hash;

        @JsonProperty("best_bid")
        private String bestBid;

        @JsonProperty("best_ask")
        private String bestAsk;
    }

    /**
     * 价格数据（旧格式）
     */
    @Data
    public static class PriceData {
        private String bid;
        private String ask;
        private String mid;
        private String last;
    }

    /**
     * 成交数据（旧格式）
     */
    @Data
    public static class TradeData {
        private String price;
        private String size;
        private String side;
    }

    /**
     * 结算数据
     */
    @Data
    public static class SettlementData {
        @JsonProperty("winner_outcome")
        private String winnerOutcome;
    }

    /**
     * 事件信息（new_market 消息中的嵌套对象）
     */
    @Data
    public static class EventMessage {
        /**
         * 事件 ID
         */
        private String id;

        /**
         * 事件代码
         */
        private String ticker;

        /**
         * 事件 slug
         */
        private String slug;

        /**
         * 事件标题
         */
        private String title;

        /**
         * 事件描述
         */
        private String description;
    }

}
