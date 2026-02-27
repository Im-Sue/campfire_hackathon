package cn.iocoder.yudao.module.social.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "用户 App - 评论 Response VO")
@Data
public class AppCommentRespVO {

    @Schema(description = "评论 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "帖子 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long postId;

    @Schema(description = "用户 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long userId;

    @Schema(description = "用户钱包地址", example = "0x1234...5678")
    private String userAddress;

    @Schema(description = "用户昵称", example = "张三")
    private String userNickname;

    @Schema(description = "用户头像URL", example = "https://example.com/avatar.jpg")
    private String userAvatar;

    @Schema(description = "父评论 ID", example = "0")
    private Long parentId;

    @Schema(description = "回复的用户 ID", example = "2")
    private Long replyUserId;

    @Schema(description = "回复的用户钱包地址", example = "0xabcd...efgh")
    private String replyUserAddress;

    @Schema(description = "回复的用户昵称", example = "李四")
    private String replyUserNickname;

    @Schema(description = "回复的用户头像URL", example = "https://example.com/avatar2.jpg")
    private String replyUserAvatar;

    @Schema(description = "回复的评论ID（用于标识回复的是哪条评论）", example = "10")
    private Long replyCommentId;

    @Schema(description = "评论内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "说得好！")
    private String content;

    @Schema(description = "点赞数", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Integer likeCount;

    @Schema(description = "是否已点赞", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean liked;

    @Schema(description = "回复数量（子评论数）", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Integer replyCount;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "回复预览列表（最多5条）")
    private List<AppCommentRespVO> replies;

}
