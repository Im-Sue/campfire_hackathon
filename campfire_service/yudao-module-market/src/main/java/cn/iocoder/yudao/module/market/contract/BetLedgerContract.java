package cn.iocoder.yudao.module.market.contract;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BetLedger 合约包装类
 * <p>
 * 预测市场下注链上记账合约的 Web3j 包装
 *
 * @author Sue
 */
public class BetLedgerContract extends Contract {

    public static final String BINARY = "";

    protected BetLedgerContract(String contractAddress, Web3j web3j, Credentials credentials,
                                ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    /**
     * 加载合约实例
     */
    public static BetLedgerContract load(String contractAddress, Web3j web3j,
                                         Credentials credentials,
                                         ContractGasProvider contractGasProvider) {
        return new BetLedgerContract(contractAddress, web3j, credentials, contractGasProvider);
    }

    // ============ 写入方法 ============

    /**
     * 批量记录下注哈希
     *
     * @param betHashes 下注哈希数组（每个 32 字节）
     * @return 交易回执
     */
    public RemoteFunctionCall<TransactionReceipt> recordBets(List<byte[]> betHashes) {
        // 将 byte[] 数组转为 Bytes32 类型的 DynamicArray
        List<Bytes32> hashList = betHashes.stream()
                .map(Bytes32::new)
                .collect(Collectors.toList());

        final Function function = new Function(
                "recordBets",
                Arrays.asList(new DynamicArray<>(Bytes32.class, hashList)),
                Collections.emptyList()
        );
        return executeRemoteCallTransaction(function);
    }

    // ============ 查询方法 ============

    /**
     * 查询下注是否已上链
     *
     * @param betHash 下注哈希（32 字节）
     * @return 是否已记录
     */
    public RemoteFunctionCall<Boolean> isBetRecorded(byte[] betHash) {
        final Function function = new Function(
                "isBetRecorded",
                Arrays.asList(new Bytes32(betHash)),
                Arrays.asList(new TypeReference<Bool>() {})
        );
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    /**
     * 获取批次计数器
     */
    public RemoteFunctionCall<BigInteger> batchCounter() {
        final Function function = new Function(
                "batchCounter",
                Collections.emptyList(),
                Arrays.asList(new TypeReference<Uint256>() {})
        );
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    /**
     * 获取合约所有者
     */
    public RemoteFunctionCall<String> owner() {
        final Function function = new Function(
                "owner",
                Collections.emptyList(),
                Arrays.asList(new TypeReference<Address>() {})
        );
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

}
