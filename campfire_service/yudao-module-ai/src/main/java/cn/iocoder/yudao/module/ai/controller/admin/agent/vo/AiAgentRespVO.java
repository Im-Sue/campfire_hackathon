package cn.iocoder.yudao.module.ai.controller.admin.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - AI Agent Response VO")
@Data
public class AiAgentRespVO {

    @Schema(description = "Agent ID", example = "1")
    private Long id;

    @Schema(description = "Agent名称", example = "激进派分析师")
    private String name;

    @Schema(description = "头像URL", example = "https://example.com/avatar.png")
    private String avatar;

    @Schema(description = "关联的钱包用户ID", example = "100")
    private Long walletUserId;

    @Schema(description = "钱包地址", example = "0x1234567890abcdef")
    private String walletAddress;

    @Schema(description = "关联的角色ID", example = "1")
    private Long roleId;

    @Schema(description = "角色名称", example = "激进分析师")
    private String roleName;

    @Schema(description = "Agent简介", example = "擅长高风险高收益策略")
    private String description;

    @Schema(description = "性格描述", example = "激进、冒险、追求高收益")
    private String personality;

    @Schema(description = "风险偏好 1保守 2中性 3激进", example = "3")
    private Integer riskLevel;

    @Schema(description = "最小下注金额", example = "100")
    private Long minBetAmount;

    @Schema(description = "最大下注金额", example = "10000")
    private Long maxBetAmount;

    @Schema(description = "单次下注最大比例", example = "0.30")
    private BigDecimal maxBetRatio;

    @Schema(description = "当前积分余额", example = "95000")
    private Long balance;

    @Schema(description = "参与事件数", example = "15")
    private Integer totalEvents;

    @Schema(description = "获胜次数", example = "10")
    private Integer winCount;

    @Schema(description = "胜率", example = "66.67")
    private BigDecimal winRate;

    @Schema(description = "累计盈亏", example = "25000")
    private Long totalProfit;

    @Schema(description = "创建者类型 1系统创建 2用户创建", example = "1")
    private Integer creatorType;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
