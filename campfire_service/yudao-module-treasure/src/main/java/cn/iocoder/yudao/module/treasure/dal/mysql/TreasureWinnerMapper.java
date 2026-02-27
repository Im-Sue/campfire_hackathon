package cn.iocoder.yudao.module.treasure.dal.mysql;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.treasure.controller.admin.winner.vo.TreasureWinnerPageReqVO;
import cn.iocoder.yudao.module.treasure.dal.dataobject.TreasureWinnerDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 夺宝中奖记录 Mapper
 *
 * @author Sue
 */
@Mapper
public interface TreasureWinnerMapper extends BaseMapperX<TreasureWinnerDO> {

    /**
     * 分页查询中奖记录
     */
    default PageResult<TreasureWinnerDO> selectPage(TreasureWinnerPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<TreasureWinnerDO>()
                .eqIfPresent(TreasureWinnerDO::getPoolId, reqVO.getPoolId())
                .eqIfPresent(TreasureWinnerDO::getWinnerAddress, reqVO.getWinnerAddress())
                .eqIfPresent(TreasureWinnerDO::getIsClaimed, reqVO.getIsClaimed())
                .eqIfPresent(TreasureWinnerDO::getContractAddress, reqVO.getContractAddress())
                .eqIfPresent(TreasureWinnerDO::getChainId, reqVO.getChainId())
                .orderByDesc(TreasureWinnerDO::getCreateTime));
    }

    /**
     * 根据奖池ID查询中奖记录
     */
    default List<TreasureWinnerDO> selectByPoolId(Long poolId, String contractAddress, Integer chainId) {
        return selectList(new LambdaQueryWrapperX<TreasureWinnerDO>()
                .eq(TreasureWinnerDO::getPoolId, poolId)
                .eq(TreasureWinnerDO::getContractAddress, contractAddress)
                .eq(TreasureWinnerDO::getChainId, chainId)
                .orderByAsc(TreasureWinnerDO::getTicketIndex));
    }

    /**
     * 根据中奖者地址查询中奖记录
     */
    default List<TreasureWinnerDO> selectByWinnerAddress(String winnerAddress) {
        return selectList(new LambdaQueryWrapperX<TreasureWinnerDO>()
                .eq(TreasureWinnerDO::getWinnerAddress, winnerAddress)
                .orderByDesc(TreasureWinnerDO::getCreateTime));
    }

    /**
     * 根据用户ID查询中奖记录
     */
    default List<TreasureWinnerDO> selectByUserId(Long userId) {
        return selectList(new LambdaQueryWrapperX<TreasureWinnerDO>()
                .eq(TreasureWinnerDO::getUserId, userId)
                .orderByDesc(TreasureWinnerDO::getCreateTime));
    }

    /**
     * 查询用户未领取的中奖记录
     */
    default List<TreasureWinnerDO> selectUnclaimedByUser(String winnerAddress) {
        return selectList(new LambdaQueryWrapperX<TreasureWinnerDO>()
                .eq(TreasureWinnerDO::getWinnerAddress, winnerAddress)
                .eq(TreasureWinnerDO::getIsClaimed, false)
                .orderByDesc(TreasureWinnerDO::getCreateTime));
    }

    /**
     * 根据票号ID查询中奖记录
     */
    default TreasureWinnerDO selectByTicketId(Long ticketId) {
        return selectOne(TreasureWinnerDO::getTicketId, ticketId);
    }

    /**
     * 查询指定奖池和票号的中奖记录
     */
    default TreasureWinnerDO selectByPoolIdAndTicketId(Long poolId, Long ticketId, String contractAddress, Integer chainId) {
        return selectOne(new LambdaQueryWrapperX<TreasureWinnerDO>()
                .eq(TreasureWinnerDO::getPoolId, poolId)
                .eq(TreasureWinnerDO::getTicketId, ticketId)
                .eq(TreasureWinnerDO::getContractAddress, contractAddress)
                .eq(TreasureWinnerDO::getChainId, chainId));
    }

    /**
     * 根据奖池ID查询中奖记录（简化版，不需要合约参数）
     */
    default List<TreasureWinnerDO> selectSimpleByPoolId(Long poolId) {
        return selectList(new LambdaQueryWrapperX<TreasureWinnerDO>()
                .eq(TreasureWinnerDO::getPoolId, poolId)
                .orderByAsc(TreasureWinnerDO::getTicketIndex));
    }
}
