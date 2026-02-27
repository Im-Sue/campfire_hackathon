package cn.iocoder.yudao.module.ai.dal.mysql.agent;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiAgentChatSessionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI Agent C端对话会话 Mapper
 *
 * @author campfire
 */
@Mapper
public interface AiAgentChatSessionMapper extends BaseMapperX<AiAgentChatSessionDO> {

    /**
     * 获取用户与某Agent的最近一次会话
     */
    default AiAgentChatSessionDO selectLastSession(Long userId, Long agentId) {
        return selectOne(new LambdaQueryWrapperX<AiAgentChatSessionDO>()
                .eq(AiAgentChatSessionDO::getUserId, userId)
                .eq(AiAgentChatSessionDO::getAgentId, agentId)
                .orderByDesc(AiAgentChatSessionDO::getLastMessageTime)
                .last("LIMIT 1"));
    }

    /**
     * 获取用户与某Agent的所有会话列表
     */
    default List<AiAgentChatSessionDO> selectListByUserAndAgent(Long userId, Long agentId) {
        return selectList(new LambdaQueryWrapperX<AiAgentChatSessionDO>()
                .eq(AiAgentChatSessionDO::getUserId, userId)
                .eqIfPresent(AiAgentChatSessionDO::getAgentId, agentId)
                .orderByDesc(AiAgentChatSessionDO::getLastMessageTime));
    }

    /**
     * 更新会话消息统计
     */
    default void updateMessageStats(Long sessionId, Integer messageCount, java.time.LocalDateTime lastMessageTime) {
        update(AiAgentChatSessionDO.builder()
                        .messageCount(messageCount)
                        .lastMessageTime(lastMessageTime)
                        .build(),
                new LambdaQueryWrapperX<AiAgentChatSessionDO>()
                        .eq(AiAgentChatSessionDO::getId, sessionId));
    }

}
