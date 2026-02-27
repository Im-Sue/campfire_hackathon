package cn.iocoder.yudao.module.market.controller.app.position.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "用户 App - 持仓 Response VO")
@Data
public class AppPositionRespVO {

    @Schema(description = "持仓编号", required = true, example = "1024")
    private Long id;

    @Schema(description = "市场编号", required = true, example = "1")
    private Long marketId;

    @Schema(description = "市场问题/名称", example = "Maduro out in 2025?")
    private String marketQuestion;

    @Schema(description = "事件编号", example = "1")
    private Long eventId;

    @Schema(description = "选项", required = true, example = "Yes")
    private String outcome;

    @Schema(description = "持仓份数", required = true, example = "100.5")
    private BigDecimal quantity;

    @Schema(description = "持仓均价（积分，原价×100）", required = true, example = "50")
    private Long avgPrice;

    @Schema(description = "总成本（积分）", required = true, example = "5000")
    private Long totalCost;

    @Schema(description = "已实现盈亏（积分）", example = "1000")
    private Long realizedPnl;

    @Schema(description = "更新时间", required = true)
    private LocalDateTime updateTime;

    // ========== 动态数据（来自缓存） ==========

    @Schema(description = "当前价格（积分，原价×100）", example = "65")
    private Long currentPrice;

    @Schema(description = "当前市值（积分）", example = "6500")
    private Long currentValue;

    @Schema(description = "未实现盈亏（积分）", example = "1500")
    private Long unrealizedPnl;

    @Schema(description = "是否已结算", example = "false")
    private Boolean settled;

    // ========== 结算相关（用于前端显示领取按钮） ==========

    @Schema(description = "市场状态: 0-草稿 1-交易中 2-待结算 3-已结算", example = "1")
    private Integer marketStatus;

    @Schema(description = "结算状态: 0-待确认 1-已确认 2-已完成，null表示未结算", example = "2")
    private Integer settlementStatus;

    @Schema(description = "获胜选项（结算后有值）", example = "Yes")
    private String winnerOutcome;

    @Schema(description = "是否获胜（结算后有值）", example = "true")
    private Boolean isWinner;

    @Schema(description = "奖励ID（有奖励时返回）", example = "100")
    private Long rewardId;

    @Schema(description = "奖励状态: 0-待领取 1-已领取 2-失败，null表示无奖励", example = "0")
    private Integer rewardStatus;

    @Schema(description = "奖励金额（积分）", example = "10000")
    private Long rewardAmount;

}
