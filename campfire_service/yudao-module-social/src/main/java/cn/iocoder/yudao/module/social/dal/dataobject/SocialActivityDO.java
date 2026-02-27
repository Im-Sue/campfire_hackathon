package cn.iocoder.yudao.module.social.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.social.enums.ActivityTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 社交互动记录 DO
 */
@TableName("social_activity")
@KeySequence("social_activity_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialActivityDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 互动类型
     *
     * 枚举 {@link ActivityTypeEnum}
     */
    private Integer type;

    /**
     * 发起者用户 ID
     */
    private Long actorUserId;

    /**
     * 接收者用户 ID
     */
    private Long targetUserId;

    /**
     * 目标 ID（帖子/评论 ID）
     */
    private Long targetId;

}
