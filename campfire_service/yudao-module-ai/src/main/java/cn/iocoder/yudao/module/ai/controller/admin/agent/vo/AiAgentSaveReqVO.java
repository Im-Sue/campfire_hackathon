package cn.iocoder.yudao.module.ai.controller.admin.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - AI Agent 创建/更新 Request VO")
@Data
public class AiAgentSaveReqVO {

    @Schema(description = "Agent ID (更新时必填)", example = "1")
    private Long id;

    @Schema(description = "Agent名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "激进派分析师")
    @NotBlank(message = "Agent名称不能为空")
    private String name;

    @Schema(description = "头像URL", example = "https://example.com/avatar.png")
    private String avatar;

    @Schema(description = "关联的钱包用户ID (创建时必填)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long walletUserId;

    @Schema(description = "关联的角色ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "角色不能为空")
    private Long roleId;

    @Schema(description = "Agent简介", example = "擅长高风险高收益策略")
    private String description;

    @Schema(description = "性格描述", example = "激进、冒险、追求高收益")
    private String personality;

    @Schema(description = "风险偏好 1保守 2中性 3激进", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    @NotNull(message = "风险偏好不能为空")
    private Integer riskLevel;

    @Schema(description = "最小下注金额", example = "100")
    private Long minBetAmount;

    @Schema(description = "最大下注金额", example = "10000")
    private Long maxBetAmount;

    @Schema(description = "单次下注最大比例", example = "0.30")
    private BigDecimal maxBetRatio;

    @Schema(description = "初始积分 (创建时)", example = "100000")
    private Long initialPoints;

    @Schema(description = "状态", example = "1")
    private Integer status;

}
