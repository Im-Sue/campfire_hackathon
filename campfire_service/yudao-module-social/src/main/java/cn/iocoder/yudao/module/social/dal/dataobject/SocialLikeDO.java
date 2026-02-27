package cn.iocoder.yudao.module.social.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.social.enums.LikeTargetTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 社交点赞 DO
 */
@TableName("social_like")
@KeySequence("social_like_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialLikeDO extends BaseDO {

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
     * 目标类型
     *
     * 枚举 {@link LikeTargetTypeEnum}
     */
    private Integer targetType;

    /**
     * 目标 ID (帖子 ID 或评论 ID)
     */
    private Long targetId;

}
