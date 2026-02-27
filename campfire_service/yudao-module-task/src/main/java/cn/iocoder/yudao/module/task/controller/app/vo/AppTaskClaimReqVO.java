package cn.iocoder.yudao.module.task.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Schema(description = "用户 App - 领取奖励 Request VO")
@Data
public class AppTaskClaimReqVO {

    @Schema(description = "任务记录ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "12345")
    @NotNull(message = "任务记录ID不能为空")
    private Long recordId;

}
