package cn.iocoder.yudao.module.treasure.contract;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Bytes8;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TreasurePool 合约包装类
 * <p>
 * 自动生成的 Web3j 合约包装类
 *
 * @author Sue
 */
public class TreasurePoolContract extends Contract {

    public static final String BINARY = ""; // 部署时不需要 bytecode

    // ========== 事件定义 ==========

    public static final Event POOL_CREATED_EVENT = new Event("PoolCreated",
            Arrays.asList(
                    new TypeReference<Uint256>(true) {}, // poolId (indexed)
                    new TypeReference<Uint256>() {},      // price
                    new TypeReference<Uint256>() {},      // totalShares
                    new TypeReference<Uint256>() {},      // winnerCount
                    new TypeReference<Uint256>() {},      // endTime
                    new TypeReference<Uint256>() {}       // initialPrize
            ));

    public static final Event TICKET_PURCHASED_EVENT = new Event("TicketPurchased",
            Arrays.asList(
                    new TypeReference<Uint256>(true) {}, // poolId (indexed)
                    new TypeReference<Address>(true) {}, // buyer (indexed)
                    new TypeReference<Uint256>() {}      // ticketIndex
            ));

    public static final Event DRAW_STARTED_EVENT = new Event("DrawStarted",
            Arrays.asList(
                    new TypeReference<Uint256>(true) {}, // poolId (indexed)
                    new TypeReference<Bytes32>() {}      // requestId
            ));

    public static final Event DRAW_COMPLETED_EVENT = new Event("DrawCompleted",
            Arrays.asList(
                    new TypeReference<Uint256>(true) {},           // poolId (indexed)
                    new TypeReference<DynamicArray<Address>>() {}, // winners
                    new TypeReference<Uint256>() {}                // prizePerWinner
            ));

    public static final Event PRIZE_CLAIMED_EVENT = new Event("PrizeClaimed",
            Arrays.asList(
                    new TypeReference<Uint256>(true) {}, // poolId (indexed)
                    new TypeReference<Address>(true) {}, // winner (indexed)
                    new TypeReference<Uint256>() {}      // amount
            ));

    // ========== 构造函数 ==========

    protected TreasurePoolContract(String contractAddress, Web3j web3j, Credentials credentials,
                                    ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    protected TreasurePoolContract(String contractAddress, Web3j web3j, TransactionManager transactionManager,
                                    ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    // ========== 静态加载方法 ==========

    public static TreasurePoolContract load(String contractAddress, Web3j web3j, Credentials credentials,
                                             ContractGasProvider contractGasProvider) {
        return new TreasurePoolContract(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static TreasurePoolContract load(String contractAddress, Web3j web3j, TransactionManager transactionManager,
                                             ContractGasProvider contractGasProvider) {
        return new TreasurePoolContract(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    // ========== 只读函数 ==========

    /**
     * 获取奖池信息
     * 注意: 由于 Web3j 的 DynamicStruct + DynamicArray 解码存在类型擦除问题，
     * 实际调用请使用 TreasureContractServiceImpl.getPool() 的原始 eth_call 解码实现。
     */
    public RemoteFunctionCall<Pool> getPool(BigInteger poolId) {
        final Function function = new Function(FUNC_GETPOOL,
                Arrays.asList(new Uint256(poolId)),
                Arrays.asList(new TypeReference<Pool>() {}));
        return executeRemoteCallSingleValueReturn(function, Pool.class);
    }

    /**
     * 获取用户票号
     */
    public RemoteFunctionCall<BigInteger> getUserTicket(String user, BigInteger poolId) {
        final Function function = new Function(FUNC_GETUSERTICKET,
                Arrays.asList(new Address(user), new Uint256(poolId)),
                Arrays.asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    /**
     * 检查用户是否中奖
     */
    public RemoteFunctionCall<Boolean> isWinner(String user, BigInteger poolId) {
        final Function function = new Function(FUNC_ISWINNER,
                Arrays.asList(new Address(user), new Uint256(poolId)),
                Arrays.asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    /**
     * 获取票号展示码
     */
    public RemoteFunctionCall<byte[]> getDisplayCode(BigInteger poolId, BigInteger index) {
        final Function function = new Function(FUNC_GETDISPLAYCODE,
                Arrays.asList(new Uint256(poolId), new Uint256(index)),
                Arrays.asList(new TypeReference<Bytes8>() {}));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    /**
     * 获取合约所有者
     */
    public RemoteFunctionCall<String> owner() {
        final Function function = new Function(FUNC_OWNER,
                Arrays.asList(),
                Arrays.asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    /**
     * 获取平台手续费率
     */
    public RemoteFunctionCall<BigInteger> platformFeeRate() {
        final Function function = new Function(FUNC_PLATFORMFEERATE,
                Arrays.asList(),
                Arrays.asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    /**
     * 获取平台手续费接收地址
     */
    public RemoteFunctionCall<String> platformFeeReceiver() {
        final Function function = new Function(FUNC_PLATFORMFEERECEIVER,
                Arrays.asList(),
                Arrays.asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    /**
     * 获取 Switchboard VRF 地址
     */
    public RemoteFunctionCall<String> switchboardVRF() {
        final Function function = new Function(FUNC_SWITCHBOARDVRF,
                Arrays.asList(),
                Arrays.asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    /**
     * 获取奖池计数器
     */
    public RemoteFunctionCall<BigInteger> poolCounter() {
        final Function function = new Function(FUNC_POOLCOUNTER,
                Arrays.asList(),
                Arrays.asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    // ========== 写入函数 ==========

    /**
     * 用户参与夺宝
     */
    public RemoteFunctionCall<TransactionReceipt> joinPool(BigInteger poolId, BigInteger weiValue) {
        final Function function = new Function(
                FUNC_JOINPOOL,
                Arrays.asList(new Uint256(poolId)),
                Collections.emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    /**
     * 用户领取奖金
     */
    public RemoteFunctionCall<TransactionReceipt> claimPrize(BigInteger poolId) {
        final Function function = new Function(
                FUNC_CLAIMPRIZE,
                Arrays.asList(new Uint256(poolId)),
                Collections.emptyList());
        return executeRemoteCallTransaction(function);
    }

    /**
     * 创建奖池（仅所有者）
     */
    public RemoteFunctionCall<TransactionReceipt> createPool(BigInteger price, BigInteger totalShares,
                                                              BigInteger duration, BigInteger winnerCount,
                                                              BigInteger initialPrize) {
        final Function function = new Function(
                FUNC_CREATEPOOL,
                Arrays.asList(new Uint256(price), new Uint256(totalShares),
                        new Uint256(duration), new Uint256(winnerCount)),
                Collections.emptyList());
        return executeRemoteCallTransaction(function, initialPrize);
    }

    /**
     * 触发开奖（仅所有者）
     */
    public RemoteFunctionCall<TransactionReceipt> executeDraw(BigInteger poolId, BigInteger vrfFee) {
        final Function function = new Function(
                FUNC_EXECUTEDRAW,
                Arrays.asList(new Uint256(poolId)),
                Collections.emptyList());
        return executeRemoteCallTransaction(function, vrfFee);
    }

    // ========== 函数名称常量 ==========

    public static final String FUNC_GETPOOL = "getPool";
    public static final String FUNC_GETUSERTICKET = "getUserTicket";
    public static final String FUNC_ISWINNER = "isWinner";
    public static final String FUNC_GETDISPLAYCODE = "getDisplayCode";
    public static final String FUNC_OWNER = "owner";
    public static final String FUNC_PLATFORMFEERATE = "platformFeeRate";
    public static final String FUNC_PLATFORMFEERECEIVER = "platformFeeReceiver";
    public static final String FUNC_SWITCHBOARDVRF = "switchboardVRF";
    public static final String FUNC_POOLCOUNTER = "poolCounter";
    public static final String FUNC_JOINPOOL = "joinPool";
    public static final String FUNC_CLAIMPRIZE = "claimPrize";
    public static final String FUNC_CREATEPOOL = "createPool";
    public static final String FUNC_EXECUTEDRAW = "executeDraw";

    // ========== 内部类：Pool 结构体 ==========

    public static class Pool extends DynamicStruct {
        public BigInteger id;
        public BigInteger price;
        public BigInteger totalShares;
        public BigInteger soldShares;
        public BigInteger winnerCount;
        public BigInteger endTime;
        public BigInteger status;
        public byte[] randomnessRequestId;
        public BigInteger prizePerWinner;
        public BigInteger initialPrize;
        public List<String> winners;

        public Pool(BigInteger id, BigInteger price, BigInteger totalShares, BigInteger soldShares,
                    BigInteger winnerCount, BigInteger endTime, BigInteger status, byte[] randomnessRequestId,
                    BigInteger prizePerWinner, BigInteger initialPrize, List<String> winners) {
            super(new Uint256(id), new Uint256(price), new Uint256(totalShares), new Uint256(soldShares),
                    new Uint256(winnerCount), new Uint256(endTime), new Uint8(status), new Bytes32(randomnessRequestId),
                    new Uint256(prizePerWinner), new Uint256(initialPrize), new DynamicArray<>(Address.class,
                            org.web3j.abi.Utils.typeMap(winners, Address.class)));
            this.id = id;
            this.price = price;
            this.totalShares = totalShares;
            this.soldShares = soldShares;
            this.winnerCount = winnerCount;
            this.endTime = endTime;
            this.status = status;
            this.randomnessRequestId = randomnessRequestId;
            this.prizePerWinner = prizePerWinner;
            this.initialPrize = initialPrize;
            this.winners = winners;
        }

        public Pool(Uint256 id, Uint256 price, Uint256 totalShares, Uint256 soldShares,
                    Uint256 winnerCount, Uint256 endTime, Uint8 status, Bytes32 randomnessRequestId,
                    Uint256 prizePerWinner, Uint256 initialPrize, DynamicArray<Address> winners) {
            super(id, price, totalShares, soldShares, winnerCount, endTime, status, randomnessRequestId,
                    prizePerWinner, initialPrize, winners);
            this.id = id.getValue();
            this.price = price.getValue();
            this.totalShares = totalShares.getValue();
            this.soldShares = soldShares.getValue();
            this.winnerCount = winnerCount.getValue();
            this.endTime = endTime.getValue();
            this.status = status.getValue();
            this.randomnessRequestId = randomnessRequestId.getValue();
            this.prizePerWinner = prizePerWinner.getValue();
            this.initialPrize = initialPrize.getValue();
            this.winners = winners.getValue().stream()
                    .map(Address::getValue)
                    .collect(Collectors.toList());
        }
    }
}
