package cn.iocoder.yudao.module.ai.controller.app.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "用户 App - 互动请求 Request VO")
@Data
public class AppInteractionReqVO {

    @Schema(description = "目标类型 1-房间消息 2-事件评论", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "目标类型不能为空")
    private Integer targetType;

    @Schema(description = "目标ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "目标ID不能为空")
    private Long targetId;
}
