package cn.iocoder.yudao.module.ai.dal.dataobject.agent;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI Agent C端对话会话 DO
 *
 * 用户与AI Agent的对话会话，采用混合模式：24小时内自动继续，超时或用户主动则新建
 *
 * @author campfire
 */
@TableName("ai_agent_chat_session")
@KeySequence("ai_agent_chat_session_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAgentChatSessionDO extends BaseDO {

    /**
     * 会话编号
     */
    @TableId
    private Long id;

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
     * 会话标题
     *
     * 默认"新对话"，首条消息后自动截取
     */
    private String title;

    /**
     * 消息数量
     */
    private Integer messageCount;

    /**
     * 最后消息时间
     *
     * 用于判断会话是否超时（混合模式24小时检查）
     */
    private LocalDateTime lastMessageTime;

}
