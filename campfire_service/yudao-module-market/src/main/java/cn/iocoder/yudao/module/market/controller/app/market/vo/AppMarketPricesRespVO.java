package cn.iocoder.yudao.module.market.controller.app.market.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Schema(description = "用户 App - 市场实时价格 Response VO")
@Data
public class AppMarketPricesRespVO {

    @Schema(description = "市场编号", example = "1024")
    private Long id;

    @Schema(description = "Polymarket 市场 ID", example = "516947")
    private String polymarketId;

    @Schema(description = "各选项买入价 (0-1)", example = "{\"Yes\": 0.66, \"No\": 0.36}")
    private Map<String, BigDecimal> outcomePrices;

    @Schema(description = "各选项卖出价 (0-1)", example = "{\"Yes\": 0.64, \"No\": 0.34}")
    private Map<String, BigDecimal> outcomeSellPrices;

    @Schema(description = "各选项买卖价 {选项: {bestBid, bestAsk}}")
    private Map<String, OutcomeBidAsk> outcomeBidAsk;

    @Schema(description = "是否接受订单", example = "true")
    private Boolean acceptingOrders;

    @Schema(description = "数据更新时间戳（毫秒）", example = "1704067200000")
    private Long updateTime;

    /**
     * 单个选项的买卖价
     */
    @Data
    @Schema(description = "选项买卖价")
    public static class OutcomeBidAsk {

        @Schema(description = "最佳买价（买入该选项的最高出价）", example = "0.64")
        private BigDecimal bestBid;

        @Schema(description = "最佳卖价（卖出该选项的最低要价）", example = "0.66")
        private BigDecimal bestAsk;

        @Schema(description = "买卖价差", example = "0.02")
        private BigDecimal spread;

    }

}
