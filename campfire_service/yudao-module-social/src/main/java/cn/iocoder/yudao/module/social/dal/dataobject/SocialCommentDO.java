package cn.iocoder.yudao.module.social.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 社交评论 DO
 */
@TableName("social_comment")
@KeySequence("social_comment_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialCommentDO extends BaseDO {

    /**
     * 评论 ID
     */
    @TableId
    private Long id;

    /**
     * 帖子 ID
     */
    private Long postId;

    /**
     * 评论用户 ID
     */
    private Long userId;

    /**
     * 父评论 ID (0 表示一级评论)
     */
    private Long parentId;

    /**
     * 回复的用户 ID
     */
    private Long replyUserId;

    /**
     * 回复的评论 ID（用于标识回复的是哪条评论）
     * - 一级评论时为 null
     * - 回复一级评论时等于 parentId
     * - 回复子评论时指向被回复的子评论 ID
     */
    private Long replyCommentId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 状态 0-正常 1-待审核 2-已删除
     */
    private Integer status;

}
