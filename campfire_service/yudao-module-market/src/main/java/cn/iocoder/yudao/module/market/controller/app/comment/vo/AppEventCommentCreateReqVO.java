package cn.iocoder.yudao.module.market.controller.app.comment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "用户 App - 发表事件评论 Request VO")
@Data
public class AppEventCommentCreateReqVO {

    @Schema(description = "事件 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "事件 ID 不能为空")
    private Long eventId;

    @Schema(description = "父评论 ID (回复时传入被回复评论的 ID)", example = "100")
    private Long parentId;

    @Schema(description = "评论内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "这个事件很有趣")
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容不能超过 500 字符")
    private String content;

}
