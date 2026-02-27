package cn.iocoder.yudao.module.ai.dal.dataobject.agent;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * AI Agent C端对话消息 DO
 *
 * @author campfire
 */
@TableName(value = "ai_agent_chat_message", autoResultMap = true)
@KeySequence("ai_agent_chat_message_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAgentChatMessageDO extends BaseDO {

    /**
     * 消息编号
     */
    @TableId
    private Long id;

    /**
     * 会话编号
     *
     * 关联 {@link AiAgentChatSessionDO#getId()}
     */
    private Long sessionId;

    /**
     * 用户编号
     *
     * 关联 MemberUserDO 的 id 字段（C端用户）
     */
    private Long userId;

    /**
     * Agent编号
     *
     * 关联 {@link AiAgentDO#getId()}
     */
    private Long agentId;

    /**
     * 消息类型
     *
     * user - 用户消息
     * assistant - Agent回复
     */
    private String type;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 推理过程（如有）
     */
    private String reasoningContent;

    /**
     * 工具调用记录
     *
     * 记录本次回复中调用了哪些工具及结果
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> toolCalls;

    /**
     * 关联事件ID（如有上下文）
     */
    private Long contextEventId;

    /**
     * 关联市场ID（如有上下文）
     */
    private Long contextMarketId;

    /**
     * Token消耗
     */
    private Integer tokensUsed;

}
