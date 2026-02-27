package cn.iocoder.yudao.module.ai.dal.mysql.agent;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiRoomDanmakuDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 竞赛弹幕 Mapper
 *
 * @author campfire
 */
@Mapper
public interface AiRoomDanmakuMapper extends BaseMapperX<AiRoomDanmakuDO> {

    default List<AiRoomDanmakuDO> selectListByRoomIdAndAfterTime(Long roomId, LocalDateTime afterTime) {
        return selectList(new LambdaQueryWrapperX<AiRoomDanmakuDO>()
                .eqIfPresent(AiRoomDanmakuDO::getRoomId, roomId)
                .gtIfPresent(AiRoomDanmakuDO::getCreateTime, afterTime)
                .orderByAsc(AiRoomDanmakuDO::getCreateTime));
    }

}
