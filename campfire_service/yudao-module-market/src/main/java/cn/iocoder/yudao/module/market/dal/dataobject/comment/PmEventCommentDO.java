package cn.iocoder.yudao.module.market.dal.dataobject.comment;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 事件评论 DO
 */
@TableName("pm_event_comment")
@KeySequence("pm_event_comment_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PmEventCommentDO extends BaseDO {

    /**
     * 评论 ID
     */
    @TableId
    private Long id;

    /**
     * 事件 ID
     */
    private Long eventId;

    /**
     * 评论用户 ID
     */
    private Long userId;

    /**
     * 父评论 ID (0 表示一级评论，否则指向一级评论)
     */
    private Long parentId;

    /**
     * 被回复的评论 ID
     * - 一级评论时为 null
     * - 回复一级评论时等于 parentId
     * - 回复子评论时指向被回复的子评论 ID
     */
    private Long replyCommentId;

    /**
     * 被回复的用户 ID
     */
    private Long replyUserId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 回复数（仅一级评论有效）
     */
    private Integer replyCount;

    /**
     * 状态：0-正常 1-待审核 2-已删除 3-审核拒绝
     */
    private Integer status;

}
