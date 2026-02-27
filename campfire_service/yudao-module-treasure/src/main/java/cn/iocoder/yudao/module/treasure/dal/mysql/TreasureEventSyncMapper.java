package cn.iocoder.yudao.module.treasure.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.treasure.dal.dataobject.TreasureEventSyncDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 夺宝事件同步 Mapper
 *
 * 区块链事件是全局的，不属于特定租户，因此所有方法都使用 @TenantIgnore 忽略租户检查
 *
 * @author Sue
 */
@Mapper
public interface TreasureEventSyncMapper extends BaseMapperX<TreasureEventSyncDO> {

    /**
     * 根据交易哈希和日志索引查询
     */
    @TenantIgnore
    default TreasureEventSyncDO selectByTxHashAndLogIndex(String txHash, Integer logIndex, String contractAddress, Integer chainId) {
        return selectOne(new LambdaQueryWrapperX<TreasureEventSyncDO>()
                .eq(TreasureEventSyncDO::getTxHash, txHash)
                .eq(TreasureEventSyncDO::getLogIndex, logIndex)
                .eq(TreasureEventSyncDO::getContractAddress, contractAddress)
                .eq(TreasureEventSyncDO::getChainId, chainId));
    }

    /**
     * 根据同步状态查询事件列表
     */
    @TenantIgnore
    default List<TreasureEventSyncDO> selectByStatus(Integer syncStatus) {
        return selectList(new LambdaQueryWrapperX<TreasureEventSyncDO>()
                .eq(TreasureEventSyncDO::getSyncStatus, syncStatus)
                .orderByAsc(TreasureEventSyncDO::getBlockNumber));
    }

    /**
     * 查询待处理的事件列表（按区块高度排序）
     */
    @TenantIgnore
    default List<TreasureEventSyncDO> selectPendingEvents(Integer limit) {
        return selectList(new LambdaQueryWrapperX<TreasureEventSyncDO>()
                .eq(TreasureEventSyncDO::getSyncStatus, 0) // Pending
                .orderByAsc(TreasureEventSyncDO::getBlockNumber)
                .last("LIMIT " + limit));
    }

    /**
     * 查询失败的事件列表（重试次数小于最大值）
     */
    @TenantIgnore
    default List<TreasureEventSyncDO> selectFailedEventsForRetry(Integer maxRetryCount, Integer limit) {
        return selectList(new LambdaQueryWrapperX<TreasureEventSyncDO>()
                .eq(TreasureEventSyncDO::getSyncStatus, 3) // Failed
                .lt(TreasureEventSyncDO::getRetryCount, maxRetryCount)
                .orderByAsc(TreasureEventSyncDO::getBlockNumber)
                .last("LIMIT " + limit));
    }

    /**
     * 根据事件类型查询事件列表
     */
    @TenantIgnore
    default List<TreasureEventSyncDO> selectByEventType(String eventType) {
        return selectList(new LambdaQueryWrapperX<TreasureEventSyncDO>()
                .eq(TreasureEventSyncDO::getEventType, eventType)
                .orderByDesc(TreasureEventSyncDO::getBlockNumber));
    }

    /**
     * 查询指定合约的最新同步区块高度
     */
    @TenantIgnore
    default Long selectMaxBlockNumber(String contractAddress, Integer chainId) {
        TreasureEventSyncDO event = selectOne(new LambdaQueryWrapperX<TreasureEventSyncDO>()
                .eq(TreasureEventSyncDO::getContractAddress, contractAddress)
                .eq(TreasureEventSyncDO::getChainId, chainId)
                .orderByDesc(TreasureEventSyncDO::getBlockNumber)
                .last("LIMIT 1"));
        return event != null ? event.getBlockNumber() : null;
    }

    /**
     * 根据区块范围查询事件列表
     */
    @TenantIgnore
    default List<TreasureEventSyncDO> selectByBlockRange(String contractAddress, Integer chainId, Long fromBlock, Long toBlock) {
        return selectList(new LambdaQueryWrapperX<TreasureEventSyncDO>()
                .eq(TreasureEventSyncDO::getContractAddress, contractAddress)
                .eq(TreasureEventSyncDO::getChainId, chainId)
                .ge(TreasureEventSyncDO::getBlockNumber, fromBlock)
                .le(TreasureEventSyncDO::getBlockNumber, toBlock)
                .orderByAsc(TreasureEventSyncDO::getBlockNumber));
    }
}
