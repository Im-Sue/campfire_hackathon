package cn.iocoder.yudao.module.ai.controller.app.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI Agent 持仓响应
 *
 * @author campfire
 */
@Schema(description = "用户端 - AI Agent 持仓响应")
@Data
public class AppAgentPositionRespVO {

    // ========== 持仓基础字段 ==========

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

    // ========== 结算相关（不包含奖励字段） ==========

    @Schema(description = "市场状态: 0-草稿 1-交易中 2-待结算 3-已结算", example = "1")
    private Integer marketStatus;

    @Schema(description = "结算状态: 0-待确认 1-已确认 2-已完成，null表示未结算", example = "2")
    private Integer settlementStatus;

    @Schema(description = "获胜选项（结算后有值）", example = "Yes")
    private String winnerOutcome;

    @Schema(description = "是否获胜（结算后有值）", example = "true")
    private Boolean isWinner;

    // ========== Agent 字段 ==========

    @Schema(description = "Agent ID", example = "1")
    private Long agentId;

    @Schema(description = "Agent 名称", example = "激进投资者 Blaze")
    private String agentName;

    @Schema(description = "Agent 头像", example = "https://example.com/avatar/blaze.jpg")
    private String agentAvatar;

    @Schema(description = "Agent 性格描述", example = "激进、果断、追求高收益")
    private String agentPersonality;

    @Schema(description = "Agent 风险偏好：1-保守 2-中性 3-激进", example = "3")
    private Integer agentRiskLevel;

}
