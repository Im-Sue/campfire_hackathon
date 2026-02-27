package cn.iocoder.yudao.module.ai.controller.app.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "用户 App - 发送弹幕 Request VO")
@Data
public class AppDanmakuSendReqVO {

    @Schema(description = "房间ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "房间ID不能为空")
    private Long roomId;

    @Schema(description = "弹幕内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "Alpha说得太对了！")
    @NotEmpty(message = "弹幕内容不能为空")
    private String content;

    @Schema(description = "弹幕颜色", example = "#FF6B6B")
    private String color;
}
