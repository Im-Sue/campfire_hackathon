package cn.iocoder.yudao.module.market.controller.admin.settlement.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 结算 Response VO")
@Data
public class SettlementRespVO {

    @Schema(description = "结算编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "市场 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long marketId;

    @Schema(description = "Polymarket 市场 ID", example = "0x...")
    private String polymarketId;

    @Schema(description = "获胜选项", requiredMode = Schema.RequiredMode.REQUIRED, example = "Yes")
    private String winnerOutcome;

    @Schema(description = "来源", requiredMode = Schema.RequiredMode.REQUIRED, example = "POLYMARKET")
    private String source;

    @Schema(description = "状态: 0-待确认 1-已确认 2-已完成", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "确认管理员 ID")
    private Long confirmedBy;

    @Schema(description = "确认时间")
    private LocalDateTime confirmedAt;

    @Schema(description = "完成时间")
    private LocalDateTime completedAt;

    @Schema(description = "总持仓数")
    private Integer totalPositions;

    @Schema(description = "获胜持仓数")
    private Integer winningPositions;

    @Schema(description = "失败持仓数")
    private Integer losingPositions;

    @Schema(description = "总奖励积分")
    private Long totalReward;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    // ========== 关联信息 ==========

    @Schema(description = "事件 ID", example = "1")
    private Long eventId;

    @Schema(description = "事件名称", example = "BTC Price Prediction")
    private String eventTitle;

    @Schema(description = "市场名称/问题", example = "Will BTC reach 100K?")
    private String marketQuestion;

}
