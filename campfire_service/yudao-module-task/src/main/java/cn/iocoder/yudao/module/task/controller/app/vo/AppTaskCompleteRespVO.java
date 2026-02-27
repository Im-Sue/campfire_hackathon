package cn.iocoder.yudao.module.task.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 完成任务 Response VO")
@Data
public class AppTaskCompleteRespVO {

    @Schema(description = "是否成功", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean success;

    @Schema(description = "奖励积分", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Long rewardPoints;

    @Schema(description = "提示消息", example = "任务完成，请领取奖励")
    private String message;

    @Schema(description = "任务记录ID", example = "12345")
    private Long recordId;

}
