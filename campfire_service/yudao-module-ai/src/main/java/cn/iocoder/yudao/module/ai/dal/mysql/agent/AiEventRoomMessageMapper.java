package cn.iocoder.yudao.module.ai.dal.mysql.agent;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiEventRoomMessagePageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiEventRoomMessageDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * AI 事件房间讨论消息 Mapper
 *
 * @author campfire
 */
@Mapper
public interface AiEventRoomMessageMapper extends BaseMapperX<AiEventRoomMessageDO> {

    default PageResult<AiEventRoomMessageDO> selectPage(AiEventRoomMessagePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AiEventRoomMessageDO>()
                .eqIfPresent(AiEventRoomMessageDO::getRoomId, reqVO.getRoomId())
                .eqIfPresent(AiEventRoomMessageDO::getAgentId, reqVO.getAgentId())
                .eqIfPresent(AiEventRoomMessageDO::getRound, reqVO.getRound())
                .eqIfPresent(AiEventRoomMessageDO::getMessageType, reqVO.getMessageType())
                .orderByDesc(AiEventRoomMessageDO::getId));
    }

    default List<AiEventRoomMessageDO> selectListByRoomIdAndRound(Long roomId, Integer round) {
        return selectList(new LambdaQueryWrapperX<AiEventRoomMessageDO>()
                .eq(AiEventRoomMessageDO::getRoomId, roomId)
                .eq(AiEventRoomMessageDO::getRound, round)
                .orderByAsc(AiEventRoomMessageDO::getId));
    }

    default List<AiEventRoomMessageDO> selectListByRoomId(Long roomId) {
        return selectList(new LambdaQueryWrapperX<AiEventRoomMessageDO>()
                .eq(AiEventRoomMessageDO::getRoomId, roomId)
                .orderByAsc(AiEventRoomMessageDO::getCreateTime));
    }

    default List<AiEventRoomMessageDO> selectListByRoomIdAfterMessageId(Long roomId, Long afterMessageId) {
        return selectList(new LambdaQueryWrapperX<AiEventRoomMessageDO>()
                .eq(AiEventRoomMessageDO::getRoomId, roomId)
                .gt(afterMessageId != null, AiEventRoomMessageDO::getId, afterMessageId)
                .orderByAsc(AiEventRoomMessageDO::getId));
    }

}
