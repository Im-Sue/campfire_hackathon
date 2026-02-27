package cn.iocoder.yudao.module.market.dal.mysql.position;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.market.dal.dataobject.position.PmPositionDO;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户持仓 Mapper
 */
@Mapper
public interface PmPositionMapper extends BaseMapperX<PmPositionDO> {

    default PmPositionDO selectByUserMarketOutcome(Long userId, Long marketId, String outcome) {
        return selectOne(new LambdaQueryWrapperX<PmPositionDO>()
                .eq(PmPositionDO::getUserId, userId)
                .eq(PmPositionDO::getMarketId, marketId)
                .eq(PmPositionDO::getOutcome, outcome));
    }

    default List<PmPositionDO> selectByUserId(Long userId) {
        return selectList(new LambdaQueryWrapperX<PmPositionDO>()
                .eq(PmPositionDO::getUserId, userId)
                .and(wrapper -> wrapper
                        // 活跃持仓: 未结算且份数 > 0
                        .nested(w -> w.and(inner -> inner
                                .and(i -> i.isNull(PmPositionDO::getSettled).or().eq(PmPositionDO::getSettled, false))
                                .gt(PmPositionDO::getQuantity, BigDecimal.ZERO)))
                        // 或 已结算持仓
                        .or(w -> w.eq(PmPositionDO::getSettled, true)))
                .orderByDesc(PmPositionDO::getUpdateTime));
    }

    default PageResult<PmPositionDO> selectPageByUserId(Long userId, PageParam pageParam) {
        return selectPage(pageParam, new LambdaQueryWrapperX<PmPositionDO>()
                .eq(PmPositionDO::getUserId, userId)
                .and(wrapper -> wrapper
                        // 活跃持仓: 未结算且份数 > 0
                        .nested(w -> w.and(inner -> inner
                                .and(i -> i.isNull(PmPositionDO::getSettled).or().eq(PmPositionDO::getSettled, false))
                                .gt(PmPositionDO::getQuantity, BigDecimal.ZERO)))
                        // 或 已结算持仓
                        .or(w -> w.eq(PmPositionDO::getSettled, true)))
                .orderByDesc(PmPositionDO::getUpdateTime));
    }

    default List<PmPositionDO> selectByMarketId(Long marketId) {
        return selectList(new LambdaQueryWrapperX<PmPositionDO>()
                .eq(PmPositionDO::getMarketId, marketId)
                .gt(PmPositionDO::getQuantity, BigDecimal.ZERO));
    }

    default List<PmPositionDO> selectByMarketIdAndOutcome(Long marketId, String outcome) {
        return selectList(new LambdaQueryWrapperX<PmPositionDO>()
                .eq(PmPositionDO::getMarketId, marketId)
                .eq(PmPositionDO::getOutcome, outcome)
                .gt(PmPositionDO::getQuantity, BigDecimal.ZERO));
    }

}
