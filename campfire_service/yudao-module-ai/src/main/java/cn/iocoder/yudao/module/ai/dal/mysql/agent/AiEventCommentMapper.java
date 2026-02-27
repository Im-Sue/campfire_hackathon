package cn.iocoder.yudao.module.ai.dal.mysql.agent;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiEventCommentPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiEventCommentDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * AI Agent 事件评论 Mapper
 *
 * @author campfire
 */
@Mapper
public interface AiEventCommentMapper extends BaseMapperX<AiEventCommentDO> {

    default AiEventCommentDO selectByEventAndAgent(Long eventId, Long agentId) {
        return selectOne(new LambdaQueryWrapperX<AiEventCommentDO>()
                .eq(AiEventCommentDO::getEventId, eventId)
                .eq(AiEventCommentDO::getAgentId, agentId));
    }

    default List<AiEventCommentDO> selectByEventId(Long eventId, Integer status) {
        return selectList(new LambdaQueryWrapperX<AiEventCommentDO>()
                .eq(AiEventCommentDO::getEventId, eventId)
                .eqIfPresent(AiEventCommentDO::getStatus, status)
                .orderByDesc(AiEventCommentDO::getCreateTime));
    }

    default PageResult<AiEventCommentDO> selectPage(AiEventCommentPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AiEventCommentDO>()
                .eqIfPresent(AiEventCommentDO::getEventId, reqVO.getEventId())
                .eqIfPresent(AiEventCommentDO::getAgentId, reqVO.getAgentId())
                .eqIfPresent(AiEventCommentDO::getStatus, reqVO.getStatus())
                .orderByDesc(AiEventCommentDO::getCreateTime));
    }

    default int deleteByEventId(Long eventId) {
        return delete(new LambdaQueryWrapperX<AiEventCommentDO>()
                .eq(AiEventCommentDO::getEventId, eventId));
    }

    default int deleteByEventAndAgent(Long eventId, Long agentId) {
        return delete(new LambdaQueryWrapperX<AiEventCommentDO>()
                .eq(AiEventCommentDO::getEventId, eventId)
                .eq(AiEventCommentDO::getAgentId, agentId));
    }
}
