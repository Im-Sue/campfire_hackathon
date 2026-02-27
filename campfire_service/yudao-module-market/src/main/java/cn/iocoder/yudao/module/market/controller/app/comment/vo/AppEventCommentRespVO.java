package cn.iocoder.yudao.module.market.controller.app.comment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户 App - 事件评论 Response VO")
@Data
public class AppEventCommentRespVO {

    @Schema(description = "评论 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    private Long id;

    @Schema(description = "事件 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long eventId;

    @Schema(description = "评论内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "这个事件很有趣")
    private String content;

    // ========== 用户信息 ==========

    @Schema(description = "用户 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    private Long userId;

    @Schema(description = "用户昵称", example = "张三")
    private String userNickname;

    @Schema(description = "用户头像 URL", example = "https://example.com/avatar.jpg")
    private String userAvatar;

    @Schema(description = "用户钱包地址", example = "0x1234...5678")
    private String userAddress;

    // ========== 回复信息（仅二级评论） ==========

    @Schema(description = "父评论 ID", example = "100")
    private Long parentId;

    @Schema(description = "被回复的评论 ID", example = "100")
    private Long replyCommentId;

    @Schema(description = "被回复用户 ID", example = "456")
    private Long replyUserId;

    @Schema(description = "被回复用户昵称", example = "李四")
    private String replyUserNickname;

    @Schema(description = "被回复用户头像 URL", example = "https://example.com/avatar2.jpg")
    private String replyUserAvatar;

    @Schema(description = "被回复用户钱包地址", example = "0xabcd...efgh")
    private String replyUserAddress;

    // ========== 统计信息 ==========

    @Schema(description = "点赞数", requiredMode = Schema.RequiredMode.REQUIRED, example = "45")
    private Integer likeCount;

    @Schema(description = "回复数（仅一级评论）", requiredMode = Schema.RequiredMode.REQUIRED, example = "12")
    private Integer replyCount;

    @Schema(description = "是否已点赞", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean liked;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "回复预览列表（最多5条）")
    private java.util.List<AppEventCommentRespVO> replies;

}
