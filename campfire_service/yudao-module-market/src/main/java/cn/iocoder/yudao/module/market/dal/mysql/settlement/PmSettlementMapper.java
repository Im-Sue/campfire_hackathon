package cn.iocoder.yudao.module.market.dal.mysql.settlement;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.market.dal.dataobject.settlement.PmSettlementDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 市场结算记录 Mapper
 */
@Mapper
public interface PmSettlementMapper extends BaseMapperX<PmSettlementDO> {

    default PmSettlementDO selectByMarketId(Long marketId) {
        return selectOne(PmSettlementDO::getMarketId, marketId);
    }

    default List<PmSettlementDO> selectByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<PmSettlementDO>()
                .eq(PmSettlementDO::getStatus, status)
                .orderByDesc(PmSettlementDO::getCreateTime));
    }

    default List<PmSettlementDO> selectPendingSettlements() {
        return selectList(new LambdaQueryWrapperX<PmSettlementDO>()
                .eq(PmSettlementDO::getStatus, 0)); // 待确认
    }

    default cn.iocoder.yudao.framework.common.pojo.PageResult<PmSettlementDO> selectPage(
            cn.iocoder.yudao.module.market.controller.admin.settlement.vo.SettlementPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PmSettlementDO>()
                .eqIfPresent(PmSettlementDO::getMarketId, reqVO.getMarketId())
                .eqIfPresent(PmSettlementDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(PmSettlementDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PmSettlementDO::getCreateTime));
    }

}
