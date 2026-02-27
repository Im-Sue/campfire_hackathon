package cn.iocoder.yudao.module.social.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 社交关注 DO
 */
@TableName("social_follow")
@KeySequence("social_follow_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialFollowDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 关注者 ID
     */
    private Long userId;

    /**
     * 被关注者 ID
     */
    private Long followUserId;

}
