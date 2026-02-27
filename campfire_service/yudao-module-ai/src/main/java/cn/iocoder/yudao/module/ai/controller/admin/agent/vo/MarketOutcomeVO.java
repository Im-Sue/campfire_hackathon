package cn.iocoder.yudao.module.ai.controller.admin.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 市场选项价格 VO")
@Data
public class MarketOutcomeVO {

    @Schema(description = "选项名称", example = "Yes")
    private String outcomeName;

    @Schema(description = "选项索引", example = "0")
    private Integer outcomeIndex;

    @Schema(description = "最佳买价", example = "0.65")
    private BigDecimal bestBid;

    @Schema(description = "最佳卖价", example = "0.66")
    private BigDecimal bestAsk;

    @Schema(description = "中间价", example = "0.655")
    private BigDecimal midPrice;

}
