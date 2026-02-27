package cn.iocoder.yudao.module.ai.dal.mysql.agent;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiAgentBalanceSnapshotDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI Agent 余额快照 Mapper
 *
 * @author campfire
 */
@Mapper
public interface AiAgentBalanceSnapshotMapper extends BaseMapperX<AiAgentBalanceSnapshotDO> {

    default List<AiAgentBalanceSnapshotDO> selectListByAgentId(Long agentId) {
        return selectList(new LambdaQueryWrapperX<AiAgentBalanceSnapshotDO>()
                .eq(AiAgentBalanceSnapshotDO::getAgentId, agentId)
                .orderByDesc(AiAgentBalanceSnapshotDO::getSnapshotTime));
    }

    default List<AiAgentBalanceSnapshotDO> selectListByAgentIdAndTimeRange(Long agentId,
            LocalDateTime startTime, LocalDateTime endTime) {
        return selectList(new LambdaQueryWrapperX<AiAgentBalanceSnapshotDO>()
                .eq(AiAgentBalanceSnapshotDO::getAgentId, agentId)
                .ge(startTime != null, AiAgentBalanceSnapshotDO::getSnapshotTime, startTime)
                .le(endTime != null, AiAgentBalanceSnapshotDO::getSnapshotTime, endTime)
                .orderByAsc(AiAgentBalanceSnapshotDO::getSnapshotTime));
    }

    default List<AiAgentBalanceSnapshotDO> selectListByAgentIdsAndTimeRange(List<Long> agentIds,
            LocalDateTime startTime, LocalDateTime endTime) {
        return selectList(new LambdaQueryWrapperX<AiAgentBalanceSnapshotDO>()
                .in(AiAgentBalanceSnapshotDO::getAgentId, agentIds)
                .ge(startTime != null, AiAgentBalanceSnapshotDO::getSnapshotTime, startTime)
                .le(endTime != null, AiAgentBalanceSnapshotDO::getSnapshotTime, endTime)
                .orderByAsc(AiAgentBalanceSnapshotDO::getSnapshotTime));
    }

}
