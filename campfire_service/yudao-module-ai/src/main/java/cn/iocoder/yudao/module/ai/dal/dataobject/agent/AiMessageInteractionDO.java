package cn.iocoder.yudao.module.ai.dal.dataobject.agent;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * AI 消息互动 DO
 *
 * @author campfire
 */
@TableName("ai_message_interaction")
@KeySequence("ai_message_interaction_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiMessageInteractionDO extends BaseDO {

    /**
     * 互动ID
     */
    @TableId
    private Long id;

    /**
     * 目标类型 1-房间消息 2-事件评论
     *
     * 枚举 {@link cn.iocoder.yudao.module.ai.enums.agent.AiInteractionTargetTypeEnum}
     */
    private Integer targetType;

    /**
     * 目标ID
     */
    private Long targetId;

    /**
     * 钱包用户ID
     */
    private Long userId;

    /**
     * 钱包地址
     */
    private String walletAddress;

    /**
     * 互动类型 1-鲜花 2-鸡蛋 3-评论
     *
     * 枚举 {@link cn.iocoder.yudao.module.ai.enums.agent.AiInteractionTypeEnum}
     */
    private Integer interactionType;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 状态 0-删除 1-正常
     */
    private Integer status;

}
