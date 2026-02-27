package cn.iocoder.yudao.module.ai.dal.mysql.agent;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiEventRoomPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiEventRoomDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * AI 事件讨论房间 Mapper
 *
 * @author campfire
 */
@Mapper
public interface AiEventRoomMapper extends BaseMapperX<AiEventRoomDO> {

    default PageResult<AiEventRoomDO> selectPage(AiEventRoomPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AiEventRoomDO>()
                .eqIfPresent(AiEventRoomDO::getEventId, reqVO.getEventId())
                .eqIfPresent(AiEventRoomDO::getStatus, reqVO.getStatus())
                .orderByDesc(AiEventRoomDO::getId));
    }

    default AiEventRoomDO selectByEventId(Long eventId) {
        return selectOne(AiEventRoomDO::getEventId, eventId);
    }

    default List<AiEventRoomDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<AiEventRoomDO>()
                .eq(AiEventRoomDO::getStatus, status)
                .orderByAsc(AiEventRoomDO::getId));
    }

}
