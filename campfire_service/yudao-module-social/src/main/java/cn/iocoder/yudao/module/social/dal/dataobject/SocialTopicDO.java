package cn.iocoder.yudao.module.social.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 社交话题 DO
 */
@TableName("social_topic")
@KeySequence("social_topic_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialTopicDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 话题名称（如: #预测市场）
     */
    private String name;

    /**
     * 热度分数
     */
    private Integer heatScore;

    /**
     * 状态：0正常 1禁用
     */
    private Integer status;

}
