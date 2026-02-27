package cn.iocoder.yudao.module.ai.dal.mysql.agent;

import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiEventRoomOrderDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * AI 事件房间订单关联 Mapper
 *
 * @author campfire
 */
@Mapper
public interface AiEventRoomOrderMapper extends BaseMapper<AiEventRoomOrderDO> {

    /**
     * 根据房间ID查询订单列表
     */
    default List<AiEventRoomOrderDO> selectListByRoomId(Long roomId) {
        return selectList(new LambdaQueryWrapper<AiEventRoomOrderDO>()
                .eq(AiEventRoomOrderDO::getRoomId, roomId));
    }

    /**
     * 根据Agent ID查询订单列表
     */
    default List<AiEventRoomOrderDO> selectListByAgentId(Long agentId) {
        return selectList(new LambdaQueryWrapper<AiEventRoomOrderDO>()
                .eq(AiEventRoomOrderDO::getAgentId, agentId));
    }

    /**
     * 根据房间ID和轮次查询订单列表
     */
    default List<AiEventRoomOrderDO> selectListByRoomIdAndRound(Long roomId, Integer round) {
        return selectList(new LambdaQueryWrapper<AiEventRoomOrderDO>()
                .eq(AiEventRoomOrderDO::getRoomId, roomId)
                .eq(AiEventRoomOrderDO::getRound, round));
    }

    /**
     * 根据房间ID和Agent ID统计订单数量
     */
    default Integer countByRoomIdAndAgentId(Long roomId, Long agentId) {
        return Math.toIntExact(selectCount(new LambdaQueryWrapper<AiEventRoomOrderDO>()
                .eq(AiEventRoomOrderDO::getRoomId, roomId)
                .eq(AiEventRoomOrderDO::getAgentId, agentId)));
    }

    /**
     * 根据房间ID统计订单总数
     */
    default Integer countByRoomId(Long roomId) {
        return Math.toIntExact(selectCount(new LambdaQueryWrapper<AiEventRoomOrderDO>()
                .eq(AiEventRoomOrderDO::getRoomId, roomId)));
    }

    /**
     * 根据房间ID统计订单总金额
     */
    default Long sumAmountByRoomId(Long roomId) {
        List<AiEventRoomOrderDO> orders = selectList(new LambdaQueryWrapper<AiEventRoomOrderDO>()
                .eq(AiEventRoomOrderDO::getRoomId, roomId)
                .select(AiEventRoomOrderDO::getOrderAmount));
        return orders.stream()
                .filter(o -> o.getOrderAmount() != null)
                .mapToLong(AiEventRoomOrderDO::getOrderAmount)
                .sum();
    }

}

