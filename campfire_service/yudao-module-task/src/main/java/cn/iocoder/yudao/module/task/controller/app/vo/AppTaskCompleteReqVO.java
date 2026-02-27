package cn.iocoder.yudao.module.task.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "用户 App - 完成任务 Request VO")
@Data
public class AppTaskCompleteReqVO {

    @Schema(description = "任务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "JOIN_TG")
    @NotBlank(message = "任务类型不能为空")
    private String taskType;

}
