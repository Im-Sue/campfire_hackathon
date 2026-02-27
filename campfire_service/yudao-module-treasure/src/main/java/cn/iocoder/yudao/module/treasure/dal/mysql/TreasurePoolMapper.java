package cn.iocoder.yudao.module.treasure.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.treasure.controller.admin.pool.vo.TreasurePoolPageReqVO;
import cn.iocoder.yudao.module.treasure.dal.dataobject.TreasurePoolDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 夺宝奖池 Mapper
 *
 * @author Sue
 */
@Mapper
public interface TreasurePoolMapper extends BaseMapperX<TreasurePoolDO> {

    /**
     * 分页查询奖池
     */
    default PageResult<TreasurePoolDO> selectPage(TreasurePoolPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<TreasurePoolDO>()
                .eqIfPresent(TreasurePoolDO::getPoolId, reqVO.getPoolId())
                .eqIfPresent(TreasurePoolDO::getStatus, reqVO.getStatus())
                .eqIfPresent(TreasurePoolDO::getContractAddress, reqVO.getContractAddress())
                .eqIfPresent(TreasurePoolDO::getChainId, reqVO.getChainId())
                .orderByDesc(TreasurePoolDO::getPoolId));
    }

    /**
     * 根据链上奖池ID查询
     */
    default TreasurePoolDO selectByPoolId(Long poolId, String contractAddress, Integer chainId) {
        return selectOne(new LambdaQueryWrapperX<TreasurePoolDO>()
                .eq(TreasurePoolDO::getPoolId, poolId)
                .eq(TreasurePoolDO::getContractAddress, contractAddress)
                .eq(TreasurePoolDO::getChainId, chainId));
    }

    /**
     * 根据状态查询奖池列表
     */
    default List<TreasurePoolDO> selectByStatus(Integer status) {
        return selectList(TreasurePoolDO::getStatus, status);
    }

    /**
     * 查询进行中的奖池列表
     */
    default List<TreasurePoolDO> selectActivePoolsOrderByEndTime() {
        return selectList(new LambdaQueryWrapperX<TreasurePoolDO>()
                .eq(TreasurePoolDO::getStatus, 0) // Active
                .orderByAsc(TreasurePoolDO::getEndTime));
    }

    /**
     * 查询已结束但未开奖的奖池列表
     */
    default List<TreasurePoolDO> selectEndedButNotDrawnPools(LocalDateTime now) {
        return selectList(new LambdaQueryWrapperX<TreasurePoolDO>()
                .in(TreasurePoolDO::getStatus, 0, 1, 2) // Active、Locked、Drawing（Drawing 是 resolveRandomness 失败卡住的）
                .le(TreasurePoolDO::getEndTime, now));
    }

    /**
     * 查询开奖中的奖池列表
     */
    default List<TreasurePoolDO> selectDrawingPools() {
        return selectList(new LambdaQueryWrapperX<TreasurePoolDO>()
                .eq(TreasurePoolDO::getStatus, 2)); // Drawing
    }

    /**
     * 根据VRF请求ID查询奖池
     */
    default TreasurePoolDO selectByRandomnessRequestId(String requestId) {
        return selectOne(TreasurePoolDO::getRandomnessRequestId, requestId);
    }

    /**
     * 查询指定合约的所有奖池
     */
    default List<TreasurePoolDO> selectByContract(String contractAddress, Integer chainId) {
        return selectList(new LambdaQueryWrapperX<TreasurePoolDO>()
                .eq(TreasurePoolDO::getContractAddress, contractAddress)
                .eq(TreasurePoolDO::getChainId, chainId)
                .orderByDesc(TreasurePoolDO::getPoolId));
    }

    /**
     * 查询第一个活跃奖池（status=0）
     */
    default TreasurePoolDO selectFirstActivePool() {
        return selectOne(new LambdaQueryWrapperX<TreasurePoolDO>()
                .eq(TreasurePoolDO::getStatus, 0)
                .orderByDesc(TreasurePoolDO::getPoolId)
                .last("LIMIT 1"));
    }

    /**
     * 分页查询历史奖池（status≠0），按endTime倒序
     */
    default PageResult<TreasurePoolDO> selectHistoryPage(TreasurePoolPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<TreasurePoolDO>()
                .ne(TreasurePoolDO::getStatus, 0)
                .orderByDesc(TreasurePoolDO::getEndTime));
    }
}

