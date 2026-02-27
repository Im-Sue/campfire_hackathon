package cn.iocoder.yudao.module.treasure.dal.mysql;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.treasure.controller.admin.ticket.vo.TreasureTicketPageReqVO;
import cn.iocoder.yudao.module.treasure.dal.dataobject.TreasureTicketDO;
import org.apache.ibatis.annotations.Mapper;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.util.List;

/**
 * 夺宝票号 Mapper
 *
 * @author Sue
 */
@Mapper
public interface TreasureTicketMapper extends BaseMapperX<TreasureTicketDO> {

    /**
     * 分页查询票号
     */
    default PageResult<TreasureTicketDO> selectPage(TreasureTicketPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<TreasureTicketDO>()
                .eqIfPresent(TreasureTicketDO::getPoolId, reqVO.getPoolId())
                .eqIfPresent(TreasureTicketDO::getOwnerAddress, reqVO.getOwnerAddress())
                .eqIfPresent(TreasureTicketDO::getIsWinner, reqVO.getIsWinner())
                .eqIfPresent(TreasureTicketDO::getIsClaimed, reqVO.getIsClaimed())
                .eqIfPresent(TreasureTicketDO::getContractAddress, reqVO.getContractAddress())
                .eqIfPresent(TreasureTicketDO::getChainId, reqVO.getChainId())
                .orderByDesc(TreasureTicketDO::getPurchaseTime));
    }

    /**
     * 根据奖池ID和票号索引查询
     */
    default TreasureTicketDO selectByPoolIdAndIndex(Long poolId, Integer ticketIndex, String contractAddress, Integer chainId) {
        return selectOne(new LambdaQueryWrapperX<TreasureTicketDO>()
                .eq(TreasureTicketDO::getPoolId, poolId)
                .eq(TreasureTicketDO::getTicketIndex, ticketIndex)
                .eq(TreasureTicketDO::getContractAddress, contractAddress)
                .eq(TreasureTicketDO::getChainId, chainId));
    }

    /**
     * 根据奖池ID查询所有票号
     */
    default List<TreasureTicketDO> selectByPoolId(Long poolId, String contractAddress, Integer chainId) {
        return selectList(new LambdaQueryWrapperX<TreasureTicketDO>()
                .eq(TreasureTicketDO::getPoolId, poolId)
                .eq(TreasureTicketDO::getContractAddress, contractAddress)
                .eq(TreasureTicketDO::getChainId, chainId)
                .orderByAsc(TreasureTicketDO::getTicketIndex));
    }

    /**
     * 根据用户地址查询票号列表
     */
    default List<TreasureTicketDO> selectByOwnerAddress(String ownerAddress) {
        return selectList(new LambdaQueryWrapperX<TreasureTicketDO>()
                .eq(TreasureTicketDO::getOwnerAddress, ownerAddress)
                .orderByDesc(TreasureTicketDO::getPurchaseTime));
    }

    /**
     * 根据用户ID查询票号列表
     */
    default List<TreasureTicketDO> selectByUserId(Long userId) {
        return selectList(new LambdaQueryWrapperX<TreasureTicketDO>()
                .eq(TreasureTicketDO::getUserId, userId)
                .orderByDesc(TreasureTicketDO::getPurchaseTime));
    }

    /**
     * 查询用户在指定奖池的票号
     */
    default TreasureTicketDO selectByUserAndPool(String ownerAddress, Long poolId, String contractAddress, Integer chainId) {
        return selectOne(new LambdaQueryWrapperX<TreasureTicketDO>()
                .eq(TreasureTicketDO::getOwnerAddress, ownerAddress)
                .eq(TreasureTicketDO::getPoolId, poolId)
                .eq(TreasureTicketDO::getContractAddress, contractAddress)
                .eq(TreasureTicketDO::getChainId, chainId));
    }

    /**
     * 查询指定奖池的中奖票号列表
     */
    default List<TreasureTicketDO> selectWinnersByPoolId(Long poolId, String contractAddress, Integer chainId) {
        return selectList(new LambdaQueryWrapperX<TreasureTicketDO>()
                .eq(TreasureTicketDO::getPoolId, poolId)
                .eq(TreasureTicketDO::getContractAddress, contractAddress)
                .eq(TreasureTicketDO::getChainId, chainId)
                .eq(TreasureTicketDO::getIsWinner, true)
                .orderByAsc(TreasureTicketDO::getTicketIndex));
    }

    /**
     * 查询用户的中奖票号列表
     */
    default List<TreasureTicketDO> selectWinnersByUser(String ownerAddress) {
        return selectList(new LambdaQueryWrapperX<TreasureTicketDO>()
                .eq(TreasureTicketDO::getOwnerAddress, ownerAddress)
                .eq(TreasureTicketDO::getIsWinner, true)
                .orderByDesc(TreasureTicketDO::getPurchaseTime));
    }

    /**
     * 查询用户未领取的中奖票号列表
     */
    default List<TreasureTicketDO> selectUnclaimedWinnersByUser(String ownerAddress) {
        return selectList(new LambdaQueryWrapperX<TreasureTicketDO>()
                .eq(TreasureTicketDO::getOwnerAddress, ownerAddress)
                .eq(TreasureTicketDO::getIsWinner, true)
                .eq(TreasureTicketDO::getIsClaimed, false)
                .orderByDesc(TreasureTicketDO::getPurchaseTime));
    }

    /**
     * 根据奖池ID查询所有票号（简化版，不需要合约参数）
     */
    default List<TreasureTicketDO> selectSimpleByPoolId(Long poolId) {
        return selectList(new LambdaQueryWrapperX<TreasureTicketDO>()
                .eq(TreasureTicketDO::getPoolId, poolId)
                .orderByAsc(TreasureTicketDO::getTicketIndex));
    }

    /**
     * 批量查询用户在多个奖池的参与情况
     */
    default List<TreasureTicketDO> selectByOwnerAddressAndPoolIds(String ownerAddress, List<Long> poolIds) {
        return selectList(new LambdaQueryWrapperX<TreasureTicketDO>()
                .eq(TreasureTicketDO::getOwnerAddress, ownerAddress)
                .in(TreasureTicketDO::getPoolId, poolIds));
    }

    /**
     * 分页查询用户参与的票号记录（按购买时间倒序）
     */
    default PageResult<TreasureTicketDO> selectPageByOwnerAddress(String ownerAddress, PageParam pageParam) {
        return selectPage(pageParam, new LambdaQueryWrapperX<TreasureTicketDO>()
                .eq(TreasureTicketDO::getOwnerAddress, ownerAddress)
                .orderByDesc(TreasureTicketDO::getPurchaseTime));
    }
}
