package cn.iocoder.yudao.module.treasure.event;

import org.web3j.protocol.core.methods.response.Log;

import java.math.BigInteger;
import java.util.List;

/**
 * Treasure 事件监听器接口
 *
 * @author Sue
 */
public interface TreasureEventListener {

    /**
     * 启动事件监听
     */
    void start();

    /**
     * 停止事件监听
     */
    void stop();

    /**
     * 扫描指定区块范围的事件
     *
     * @param fromBlock 起始区块
     * @param toBlock   结束区块
     * @return 事件日志列表
     */
    List<Log> scanEvents(BigInteger fromBlock, BigInteger toBlock) throws Exception;

    /**
     * 处理事件日志
     *
     * @param log 事件日志
     */
    void processEvent(Log log) throws Exception;

    /**
     * 获取最新已同步的区块高度
     *
     * @return 区块高度
     */
    BigInteger getLastSyncedBlock();

    /**
     * 更新最新已同步的区块高度
     *
     * @param blockNumber 区块高度
     */
    void updateLastSyncedBlock(BigInteger blockNumber);

    /**
     * 触发一次补偿回放
     */
    void replayOnce();
}
