package cn.iocoder.yudao.module.market.dal.dataobject.comment;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 事件评论点赞 DO
 */
@TableName("pm_event_comment_like")
@KeySequence("pm_event_comment_like_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PmEventCommentLikeDO extends BaseDO {

    /**
     * 点赞 ID
     */
    @TableId
    private Long id;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 评论 ID
     */
    private Long commentId;

}
