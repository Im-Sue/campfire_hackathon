package cn.iocoder.yudao.module.market.dal.mysql.reward;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.market.controller.admin.reward.vo.RewardPageReqVO;
import cn.iocoder.yudao.module.market.dal.dataobject.reward.PmRewardDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户奖励记录 Mapper
 */
@Mapper
public interface PmRewardMapper extends BaseMapperX<PmRewardDO> {

    default PmRewardDO selectByPositionId(Long positionId) {
        return selectOne(PmRewardDO::getPositionId, positionId);
    }

    default List<PmRewardDO> selectByUserId(Long userId) {
        return selectList(new LambdaQueryWrapperX<PmRewardDO>()
                .eq(PmRewardDO::getUserId, userId)
                .orderByDesc(PmRewardDO::getCreateTime));
    }

    default PageResult<PmRewardDO> selectPageByUserId(Long userId, PageParam pageParam) {
        return selectPage(pageParam, new LambdaQueryWrapperX<PmRewardDO>()
                .eq(PmRewardDO::getUserId, userId)
                .orderByDesc(PmRewardDO::getCreateTime));
    }

    default List<PmRewardDO> selectPendingByUserId(Long userId) {
        return selectList(new LambdaQueryWrapperX<PmRewardDO>()
                .eq(PmRewardDO::getUserId, userId)
                .eq(PmRewardDO::getStatus, 0) // 待领取
                .orderByDesc(PmRewardDO::getCreateTime));
    }

    default List<PmRewardDO> selectBySettlementId(Long settlementId) {
        return selectList(PmRewardDO::getSettlementId, settlementId);
    }

    // ========== 管理端 ==========

    /**
     * 管理端分页查询奖励
     */
    default PageResult<PmRewardDO> selectPage(RewardPageReqVO pageReqVO) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<PmRewardDO>()
                .likeIfPresent(PmRewardDO::getWalletAddress, pageReqVO.getWalletAddress())
                .eqIfPresent(PmRewardDO::getUserId, pageReqVO.getUserId())
                .eqIfPresent(PmRewardDO::getMarketId, pageReqVO.getMarketId())
                .eqIfPresent(PmRewardDO::getStatus, pageReqVO.getStatus())
                .betweenIfPresent(PmRewardDO::getCreateTime, pageReqVO.getCreateTime())
                .orderByDesc(PmRewardDO::getCreateTime));
    }

    /**
     * 按状态统计奖励金额
     */
    @Select("SELECT COALESCE(SUM(reward_amount), 0) FROM pm_reward WHERE status = #{status} AND deleted = 0")
    Long sumAmountByStatus(@Param("status") Integer status);

    /**
     * 按状态统计奖励数量
     */
    @Select("SELECT COUNT(*) FROM pm_reward WHERE status = #{status} AND deleted = 0")
    Integer countByStatus(@Param("status") Integer status);

}
