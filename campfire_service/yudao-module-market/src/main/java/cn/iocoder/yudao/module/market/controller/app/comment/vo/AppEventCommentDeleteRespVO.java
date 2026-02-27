package cn.iocoder.yudao.module.market.controller.app.comment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "用户 App - 删除事件评论 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppEventCommentDeleteRespVO {

    @Schema(description = "事件的总评论数", example = "42")
    private Integer totalCommentCount;

    @Schema(description = "父评论的回复数（仅删除回复时返回）", example = "5")
    private Integer replyCount;

}
