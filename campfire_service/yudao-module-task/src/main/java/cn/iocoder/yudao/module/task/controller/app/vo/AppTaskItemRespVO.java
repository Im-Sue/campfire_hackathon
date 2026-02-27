package cn.iocoder.yudao.module.task.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 任务列表项 Response VO")
@Data
public class AppTaskItemRespVO {

    @Schema(description = "任务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "SIGN_IN")
    private String taskType;

    @Schema(description = "任务名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "每日签到")
    private String name;

    @Schema(description = "任务描述", example = "每日签到领取积分")
    private String description;

    @Schema(description = "触发方式：1自动 2点击完成 3点击跳转后回调", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer triggerMode;

    @Schema(description = "重置周期：1一次性 2每日 3无限次", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer resetCycle;

    @Schema(description = "奖励积分", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long rewardPoints;

    @Schema(description = "每日完成上限", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer dailyLimit;

    @Schema(description = "跳转URL", example = "https://t.me/your_group")
    private String redirectUrl;

    @Schema(description = "图标URL", example = "/icons/sign_in.png")
    private String iconUrl;

    @Schema(description = "任务名称(英文)", example = "Daily Check-in")
    private String nameEn;

    @Schema(description = "任务描述(英文)", example = "Check in every day to earn points")
    private String descriptionEn;

    @Schema(description = "任务图片URL", example = "https://xxx.com/task.png")
    private String imageUrl;

    // ===== 用户状态 =====

    @Schema(description = "今日已完成次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer todayCount;

    @Schema(description = "是否已完成（达到上限）", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean completed;

    @Schema(description = "是否可领取奖励", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean canClaim;

    @Schema(description = "待领取奖励积分", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Long pendingRewards;

    @Schema(description = "任务记录ID（用于领取奖励）", example = "12345")
    private Long recordId;

}
