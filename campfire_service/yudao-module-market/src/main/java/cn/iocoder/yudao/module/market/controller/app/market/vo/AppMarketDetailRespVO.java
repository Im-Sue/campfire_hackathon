package cn.iocoder.yudao.module.market.controller.app.market.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Schema(description = "用户 App - 市场详情 Response VO")
@Data
public class AppMarketDetailRespVO {

    @Schema(description = "市场编号", example = "1024")
    private Long id;

    @Schema(description = "关联事件 ID", example = "1")
    private Long eventId;

    @Schema(description = "Polymarket 市场 ID", example = "0x123abc")
    private String polymarketId;

    @Schema(description = "Condition ID", example = "0xabc123")
    private String conditionId;

    @Schema(description = "市场问题", example = "Will Bitcoin hit 100k?")
    private String question;

    @Schema(description = "分组标题", example = "Spread -1.5")
    private String groupItemTitle;

    @Schema(description = "选项列表", example = "[\"Yes\", \"No\"]")
    private List<String> outcomes;

    @Schema(description = "状态: 1-交易中 2-封盘 3-待结算 4-已结算", example = "1")
    private Integer status;

    @Schema(description = "开始时间")
    private LocalDateTime startDate;

    @Schema(description = "结束时间")
    private LocalDateTime endDate;

    // ========== 动态数据（从 Polymarket API 获取） ==========

    @Schema(description = "各选项价格", example = "{\"Yes\": \"0.65\", \"No\": \"0.35\"}")
    private Map<String, BigDecimal> outcomePrices;

    @Schema(description = "交易量 (USD)", example = "1250000.50")
    private BigDecimal volume;

    @Schema(description = "流动性", example = "50000")
    private BigDecimal liquidity;

    @Schema(description = "最佳买价", example = "0.64")
    private BigDecimal bestBid;

    @Schema(description = "最佳卖价", example = "0.66")
    private BigDecimal bestAsk;

    @Schema(description = "是否接受订单", example = "true")
    private Boolean acceptingOrders;

    @Schema(description = "是否启用订单簿（用于判断是否显示K线）", example = "true")
    private Boolean enableOrderBook;

}
