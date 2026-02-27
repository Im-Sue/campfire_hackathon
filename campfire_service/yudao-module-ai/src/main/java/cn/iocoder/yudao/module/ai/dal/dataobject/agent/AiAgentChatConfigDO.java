package cn.iocoder.yudao.module.ai.dal.dataobject.agent;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * AI Agent 对话配置 DO
 *
 * 系统级别配置，包括配额、成本等
 */
@TableName("ai_agent_chat_config")
@KeySequence("ai_agent_chat_config_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAgentChatConfigDO extends BaseDO {

    /**
     * 配置编号
     */
    @TableId
    private Long id;

    /**
     * 配置键
     */
    private String configKey;

    /**
     * 配置值
     */
    private String configValue;

    /**
     * 配置描述
     */
    private String description;

    // ========== 配置键常量 ==========

    /** 每日免费配额 */
    public static final String KEY_DAILY_FREE_QUOTA = "daily_free_quota";
    /** 每次对话成本（积分） */
    public static final String KEY_CHAT_COST = "chat_cost";
    /** 最大上下文轮数 */
    public static final String KEY_MAX_CONTEXT_ROUNDS = "max_context_rounds";
    /** 单条消息最大长度 */
    public static final String KEY_MAX_MESSAGE_LENGTH = "max_message_length";

}
