package cn.iocoder.yudao.module.social.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 评论 Response VO")
@Data
public class CommentRespVO {

    @Schema(description = "评论 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "帖子 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long postId;

    @Schema(description = "用户 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long userId;

    @Schema(description = "用户钱包地址", example = "0x1234567890abcdef1234567890abcdef12345678")
    private String userAddress;

    @Schema(description = "父评论 ID", example = "0")
    private Long parentId;

    @Schema(description = "回复的用户 ID", example = "2")
    private Long replyUserId;

    @Schema(description = "评论内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "说得好！")
    private String content;

    @Schema(description = "点赞数", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Integer likeCount;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

}
