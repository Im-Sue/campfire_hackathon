package cn.iocoder.yudao.module.market.controller.admin.reward.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 奖励统计 Response VO")
@Data
public class RewardStatisticsVO {

    @Schema(description = "待领取总额", requiredMode = Schema.RequiredMode.REQUIRED, example = "100000")
    private Long pendingAmount;

    @Schema(description = "待领取笔数", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
    private Integer pendingCount;

    @Schema(description = "已领取总额", requiredMode = Schema.RequiredMode.REQUIRED, example = "500000")
    private Long claimedAmount;

    @Schema(description = "已领取笔数", requiredMode = Schema.RequiredMode.REQUIRED, example = "200")
    private Integer claimedCount;

}
