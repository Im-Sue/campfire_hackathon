package cn.iocoder.yudao.module.ai.dal.mysql.agent;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiAgentPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiAgentDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * AI Agent Mapper
 *
 * @author campfire
 */
@Mapper
public interface AiAgentMapper extends BaseMapperX<AiAgentDO> {

    default PageResult<AiAgentDO> selectPage(AiAgentPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AiAgentDO>()
                .likeIfPresent(AiAgentDO::getName, reqVO.getName())
                .eqIfPresent(AiAgentDO::getStatus, reqVO.getStatus())
                .eqIfPresent(AiAgentDO::getRiskLevel, reqVO.getRiskLevel())
                .orderByDesc(AiAgentDO::getId));
    }

    default List<AiAgentDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<AiAgentDO>()
                .eq(AiAgentDO::getStatus, status)
                .orderByAsc(AiAgentDO::getId));
    }

    default AiAgentDO selectByWalletUserId(Long walletUserId) {
        return selectOne(AiAgentDO::getWalletUserId, walletUserId);
    }

}
