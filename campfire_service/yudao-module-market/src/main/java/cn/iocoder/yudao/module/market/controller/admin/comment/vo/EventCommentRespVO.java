package cn.iocoder.yudao.module.market.controller.admin.comment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 事件评论 Response VO")
@Data
public class EventCommentRespVO {

    @Schema(description = "评论 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    private Long id;

    @Schema(description = "事件 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long eventId;

    @Schema(description = "事件标题", example = "2026年美国大选")
    private String eventTitle;

    @Schema(description = "用户 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    private Long userId;

    @Schema(description = "用户昵称", example = "张三")
    private String userNickname;

    @Schema(description = "父评论 ID", example = "0")
    private Long parentId;

    @Schema(description = "被回复用户 ID", example = "456")
    private Long replyUserId;

    @Schema(description = "被回复用户昵称", example = "李四")
    private String replyUserNickname;

    @Schema(description = "评论内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "这个事件很有趣")
    private String content;

    @Schema(description = "点赞数", requiredMode = Schema.RequiredMode.REQUIRED, example = "45")
    private Integer likeCount;

    @Schema(description = "回复数", requiredMode = Schema.RequiredMode.REQUIRED, example = "12")
    private Integer replyCount;

    @Schema(description = "状态: 0-正常 1-待审核 2-已删除", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
