package cn.iocoder.yudao.module.ai.controller.app.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "用户 App - 互动评论请求 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class AppInteractionCommentReqVO extends AppInteractionReqVO {

    @Schema(description = "评论内容 (30字以内)", requiredMode = Schema.RequiredMode.REQUIRED, example = "分析得很专业")
    @NotEmpty(message = "评论内容不能为空")
    private String content;

}
