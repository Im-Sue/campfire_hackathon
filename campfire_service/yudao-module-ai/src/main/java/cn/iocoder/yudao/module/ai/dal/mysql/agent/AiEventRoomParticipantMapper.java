package cn.iocoder.yudao.module.ai.dal.mysql.agent;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiEventRoomParticipantDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * AI 事件房间参与者 Mapper
 *
 * @author campfire
 */
@Mapper
public interface AiEventRoomParticipantMapper extends BaseMapperX<AiEventRoomParticipantDO> {

    default List<AiEventRoomParticipantDO> selectListByRoomId(Long roomId) {
        return selectList(new LambdaQueryWrapperX<AiEventRoomParticipantDO>()
                .eq(AiEventRoomParticipantDO::getRoomId, roomId)
                .orderByAsc(AiEventRoomParticipantDO::getId));
    }

    default AiEventRoomParticipantDO selectByRoomIdAndAgentId(Long roomId, Long agentId) {
        return selectOne(new LambdaQueryWrapperX<AiEventRoomParticipantDO>()
                .eq(AiEventRoomParticipantDO::getRoomId, roomId)
                .eq(AiEventRoomParticipantDO::getAgentId, agentId));
    }

    default List<AiEventRoomParticipantDO> selectListByAgentId(Long agentId) {
        return selectList(AiEventRoomParticipantDO::getAgentId, agentId);
    }

}
