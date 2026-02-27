package cn.iocoder.yudao.module.ai.dal.mysql.agent;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiAgentChatMessageDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * AI Agent 对话消息 Mapper
 */
@Mapper
public interface AiAgentChatMessageMapper extends BaseMapperX<AiAgentChatMessageDO> {

    /**
     * 根据会话ID查询消息列表（按时间升序）
     *
     * @param sessionId 会话ID
     * @return 消息列表
     */
    default List<AiAgentChatMessageDO> selectListBySessionId(Long sessionId) {
        return selectList(new LambdaQueryWrapperX<AiAgentChatMessageDO>()
                .eq(AiAgentChatMessageDO::getSessionId, sessionId)
                .orderByAsc(AiAgentChatMessageDO::getCreateTime));
    }

    /**
     * 根据会话ID分页查询消息
     *
     * @param sessionId 会话ID
     * @param pageParam 分页参数
     * @return 分页结果
     */
    default PageResult<AiAgentChatMessageDO> selectPageBySessionId(Long sessionId, PageParam pageParam) {
        return selectPage(pageParam, new LambdaQueryWrapperX<AiAgentChatMessageDO>()
                .eq(AiAgentChatMessageDO::getSessionId, sessionId)
                .orderByDesc(AiAgentChatMessageDO::getCreateTime));
    }

    /**
     * 统计会话中的消息数量
     *
     * @param sessionId 会话ID
     * @return 消息数量
     */
    default Long selectCountBySessionId(Long sessionId) {
        return selectCount(AiAgentChatMessageDO::getSessionId, sessionId);
    }

    /**
     * 获取最近N条消息（用于构建上下文）
     *
     * @param sessionId 会话ID
     * @param limit     数量限制
     * @return 消息列表
     */
    default List<AiAgentChatMessageDO> selectRecentMessages(Long sessionId, int limit) {
        return selectList(new LambdaQueryWrapperX<AiAgentChatMessageDO>()
                .eq(AiAgentChatMessageDO::getSessionId, sessionId)
                .orderByDesc(AiAgentChatMessageDO::getCreateTime)
                .last("LIMIT " + limit));
    }

    /**
     * 统计用户今日发送的消息数量
     *
     * @param userId 用户ID
     * @return 今日消息数量
     */
    default Long selectTodayCountByUserId(Long userId) {
        java.time.LocalDateTime startOfDay = java.time.LocalDate.now().atStartOfDay();
        java.time.LocalDateTime endOfDay = startOfDay.plusDays(1);
        return selectCount(new LambdaQueryWrapperX<AiAgentChatMessageDO>()
                .eq(AiAgentChatMessageDO::getUserId, userId)
                .eq(AiAgentChatMessageDO::getType, "user")
                .ge(AiAgentChatMessageDO::getCreateTime, startOfDay)
                .lt(AiAgentChatMessageDO::getCreateTime, endOfDay));
    }

}
