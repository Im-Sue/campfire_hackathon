package cn.iocoder.yudao.module.ai.dal.mysql.agent;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiAgentPeriodStatsDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * AI Agent 赛季统计 Mapper
 *
 * @author campfire
 */
@Mapper
public interface AiAgentPeriodStatsMapper extends BaseMapperX<AiAgentPeriodStatsDO> {

    default AiAgentPeriodStatsDO selectByAgentAndPeriod(Long agentId, Integer periodType, String periodKey) {
        return selectOne(new LambdaQueryWrapperX<AiAgentPeriodStatsDO>()
                .eq(AiAgentPeriodStatsDO::getAgentId, agentId)
                .eq(AiAgentPeriodStatsDO::getPeriodType, periodType)
                .eq(AiAgentPeriodStatsDO::getPeriodKey, periodKey));
    }

    default List<AiAgentPeriodStatsDO> selectListByPeriodOrderByProfit(Integer periodType, String periodKey, Integer limit) {
        return selectList(new LambdaQueryWrapperX<AiAgentPeriodStatsDO>()
                .eq(AiAgentPeriodStatsDO::getPeriodType, periodType)
                .eq(AiAgentPeriodStatsDO::getPeriodKey, periodKey)
                .orderByDesc(AiAgentPeriodStatsDO::getProfit)
                .last(limit != null, "LIMIT " + limit));
    }

    default List<AiAgentPeriodStatsDO> selectListByAgentId(Long agentId) {
        return selectList(new LambdaQueryWrapperX<AiAgentPeriodStatsDO>()
                .eq(AiAgentPeriodStatsDO::getAgentId, agentId)
                .orderByDesc(AiAgentPeriodStatsDO::getCreateTime));
    }

}
