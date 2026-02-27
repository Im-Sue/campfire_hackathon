package cn.iocoder.yudao.module.market.controller.admin.reward.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 奖励 Response VO")
@Data
public class RewardRespVO {

    @Schema(description = "奖励编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "用户 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long userId;

    @Schema(description = "钱包地址", example = "0x1234...")
    private String walletAddress;

    @Schema(description = "市场 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long marketId;

    @Schema(description = "结算 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long settlementId;

    @Schema(description = "持仓 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long positionId;

    @Schema(description = "选项", requiredMode = Schema.RequiredMode.REQUIRED, example = "Yes")
    private String outcome;

    @Schema(description = "份数", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.5")
    private BigDecimal quantity;

    @Schema(description = "奖励积分", requiredMode = Schema.RequiredMode.REQUIRED, example = "10000")
    private Long rewardAmount;

    @Schema(description = "状态: 0-待领取 1-已领取 2-失败", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "领取时间")
    private LocalDateTime claimedAt;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    // 关联信息
    @Schema(description = "市场问题", example = "Will BTC reach 100K?")
    private String marketQuestion;

}
