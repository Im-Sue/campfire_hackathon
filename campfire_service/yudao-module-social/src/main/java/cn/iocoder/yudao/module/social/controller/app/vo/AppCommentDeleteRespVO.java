package cn.iocoder.yudao.module.social.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "用户 App - 删除社交评论 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppCommentDeleteRespVO {

    @Schema(description = "帖子的评论数（一级评论+回复总数）", example = "10")
    private Integer commentCount;

    @Schema(description = "帖子的回复总数（不含一级评论）", example = "7")
    private Integer postReplyCount;

    @Schema(description = "父评论的回复数（仅删除回复时返回）", example = "3")
    private Integer replyCount;

}
