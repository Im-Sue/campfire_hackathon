package cn.iocoder.yudao.module.market.controller.app.event.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Schema(description = "用户 App - 市场简要信息 VO（用于 Event 嵌套展示）")
@Data
public class AppMarketSimpleVO {

    @Schema(description = "市场编号", required = true, example = "1024")
    private Long id;

    @Schema(description = "Polymarket 市场 ID", example = "0x123abc")
    private String polymarketId;

    @Schema(description = "市场问题", required = true, example = "JD Vance wins?")
    private String question;

    @Schema(description = "分组标题", example = "Spread -1.5")
    private String groupItemTitle;

    @Schema(description = "选项列表", example = "[\"Yes\", \"No\"]")
    private List<String> outcomes;

    @Schema(description = "状态: 1-交易中 2-封盘 3-待结算 4-已结算", example = "1")
    private Integer status;

    // ========== 动态价格数据（来自缓存） ==========

    @Schema(description = "各选项价格", example = "{\"Yes\": \"0.31\", \"No\": \"0.69\"}")
    private Map<String, BigDecimal> outcomePrices;

}
