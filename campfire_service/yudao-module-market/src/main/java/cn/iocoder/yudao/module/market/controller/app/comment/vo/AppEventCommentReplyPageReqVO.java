package cn.iocoder.yudao.module.market.controller.app.comment.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotNull;

@Schema(description = "用户 App - 评论回复分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class AppEventCommentReplyPageReqVO extends PageParam {

    @Schema(description = "父评论 ID（一级评论 ID）", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "父评论 ID 不能为空")
    private Long parentId;

}
