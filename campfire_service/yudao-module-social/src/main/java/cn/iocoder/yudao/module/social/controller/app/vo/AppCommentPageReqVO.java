package cn.iocoder.yudao.module.social.controller.app.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import jakarta.validation.constraints.NotNull;

@Schema(description = "用户 App - 评论分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppCommentPageReqVO extends PageParam {

    @Schema(description = "帖子 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "帖子 ID 不能为空")
    private Long postId;

}
