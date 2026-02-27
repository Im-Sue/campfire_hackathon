package cn.iocoder.yudao.module.social.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "用户 App - 发表评论 Request VO")
@Data
public class AppCommentCreateReqVO {

    @Schema(description = "帖子 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "帖子 ID 不能为空")
    private Long postId;

    @Schema(description = "父评论 ID (回复评论时传入)", example = "1")
    private Long parentId;

    @Schema(description = "评论内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "说得好！")
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容不能超过 500 字符")
    private String content;

}
