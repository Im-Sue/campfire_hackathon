package cn.iocoder.yudao.module.market.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Polymarket Event DTO
 * 
 * 用于接收 Polymarket Gamma API 返回的事件数据
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolymarketEventDTO {

    private String id;
    private String ticker;
    private String slug;
    private String title;
    private String description;

    @JsonProperty("image")
    private String imageUrl;

    private String category;

    /**
     * 是否已导入到本地（非 API 返回字段，由业务层设置）
     */
    private Boolean imported;

    /**
     * 标签列表 - Polymarket 返回 [{id, label, slug}, ...]
     */
    private List<Map<String, Object>> tags;

    @JsonProperty("negRisk")
    private Boolean negRisk;

    /**
     * 使用 String 避免日期解析问题，后续手动转换
     */
    @JsonProperty("startDate")
    private String startDate;

    @JsonProperty("endDate")
    private String endDate;

    /**
     * 成交量
     */
    private BigDecimal volume;

    /**
     * 流动性
     */
    private BigDecimal liquidity;

    /**
     * 包含的市场列表
     */
    private List<MarketDTO> markets;

    // ========== 体育专有字段 ==========

    @JsonProperty("gameId")
    private Integer gameId;

    @JsonProperty("homeTeamName")
    private String homeTeamName;

    @JsonProperty("awayTeamName")
    private String awayTeamName;

    private String score;
    private String period;
    private Boolean live;
    private Boolean ended;
    private Boolean closed;

    @JsonProperty("seriesSlug")
    private String seriesSlug;

    @JsonProperty("seriesId")
    private String seriesId;

    @JsonProperty("eventDate")
    private String eventDate;

    @JsonProperty("eventWeek")
    private Integer eventWeek;

    /**
     * Polymarket Market DTO
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MarketDTO {
        private String id;

        @JsonProperty("conditionId")
        private String conditionId;

        private String question;
        private String description;

        @JsonProperty("groupItemTitle")
        private String groupItemTitle;

        /**
         * outcomes - Polymarket 返回 JSON 字符串 "[\"Yes\", \"No\"]"
         */
        private String outcomes;

        @JsonProperty("outcomePrices")
        private String outcomePrices;

        @JsonProperty("clobTokenIds")
        private String clobTokenIds;

        private BigDecimal volume;
        private BigDecimal liquidity;

        @JsonProperty("bestAsk")
        private BigDecimal bestAsk;

        @JsonProperty("bestBid")
        private BigDecimal bestBid;

        @JsonProperty("startDate")
        private String startDate;

        @JsonProperty("endDate")
        private String endDate;

        private Boolean closed;
        private Boolean active;

        @JsonProperty("acceptingOrders")
        private Boolean acceptingOrders;

        @JsonProperty("enableOrderBook")
        private Boolean enableOrderBook;

        // 体育盘口
        @JsonProperty("sportsMarketType")
        private String sportsMarketType;

        private BigDecimal line;

        @JsonProperty("gameStartTime")
        private String gameStartTime;

        @JsonProperty("negRisk")
        private Boolean negRisk;

        @JsonProperty("image")
        private String imageUrl;
    }

}
