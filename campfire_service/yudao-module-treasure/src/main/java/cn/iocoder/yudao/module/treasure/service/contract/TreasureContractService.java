package cn.iocoder.yudao.module.treasure.service.contract;

import cn.iocoder.yudao.module.treasure.contract.TreasurePoolContract;

import java.math.BigInteger;

/**
 * TreasurePool 合约服务接口
 *
 * @author Sue
 */
public interface TreasureContractService {

    /**
     * 获取合约实例
     */
    TreasurePoolContract getContract();

    /**
     * 获取奖池信息
     *
     * @param poolId 奖池ID
     * @return 奖池信息
     */
    TreasurePoolContract.Pool getPool(BigInteger poolId) throws Exception;

    /**
     * 获取用户票号
     *
     * @param userAddress 用户地址
     * @param poolId      奖池ID
     * @return 票号索引（0表示未参与）
     */
    BigInteger getUserTicket(String userAddress, BigInteger poolId) throws Exception;

    /**
     * 检查用户是否中奖
     *
     * @param userAddress 用户地址
     * @param poolId      奖池ID
     * @return 是否中奖
     */
    Boolean isWinner(String userAddress, BigInteger poolId) throws Exception;

    /**
     * 获取票号展示码
     *
     * @param poolId 奖池ID
     * @param index  票号索引
     * @return 展示码
     */
    byte[] getDisplayCode(BigInteger poolId, BigInteger index) throws Exception;

    /**
     * 获取合约所有者
     */
    String getOwner() throws Exception;

    /**
     * 获取平台手续费率
     */
    BigInteger getPlatformFeeRate() throws Exception;

    /**
     * 获取奖池计数器
     */
    BigInteger getPoolCounter() throws Exception;

    /**
     * 创建奖池（仅管理员）
     *
     * @param price       单价（wei）
     * @param totalShares 总份数
     * @param duration    持续时间（秒）
     * @param winnerCount 中奖名额
     * @return 交易哈希
     */
    String createPool(BigInteger price, BigInteger totalShares, BigInteger duration, BigInteger winnerCount, BigInteger initialPrize) throws Exception;

    /**
     * 触发开奖（仅管理员）
     *
     * @param poolId 奖池ID
     * @return 交易哈希
     */
    String executeDraw(BigInteger poolId) throws Exception;

    /**
     * 解决随机数请求 - 调用 MockVRF.resolveRandomness()
     * 在 executeDraw 之后调用，完成两步式开奖
     *
     * @param requestId VRF 请求 ID（从 DrawStarted 事件获取）
     * @return 交易哈希
     */
    String resolveRandomness(byte[] requestId) throws Exception;
}
