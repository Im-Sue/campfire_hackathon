package cn.iocoder.yudao.module.treasure.service.contract.impl;

import cn.iocoder.yudao.module.treasure.config.TreasureProperties;
import cn.iocoder.yudao.module.treasure.contract.TreasurePoolContract;
import cn.iocoder.yudao.module.treasure.service.contract.TreasureContractService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.utils.Numeric;

import jakarta.annotation.Resource;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * TreasurePool 合约服务实现类
 *
 * @author Sue
 */
@Slf4j
@Service
public class TreasureContractServiceImpl implements TreasureContractService {

    @Resource
    private Web3j web3j;

    @Resource
    private Credentials credentials;

    @Resource
    private ContractGasProvider gasProvider;

    @Resource
    private TreasureProperties treasureProperties;

    @Resource
    private cn.iocoder.yudao.module.treasure.service.config.TreasureConfigService treasureConfigService;

    private TreasurePoolContract contract;

    /**
     * 获取合约实例（懒加载）
     */
    @Override
    public TreasurePoolContract getContract() {
        if (contract == null) {
            synchronized (this) {
                if (contract == null) {
                    String contractAddress = treasureConfigService.getContractAddress();
                    contract = TreasurePoolContract.load(contractAddress, web3j, credentials, gasProvider);
                    log.info("TreasurePool 合约已加载: {}", contractAddress);
                }
            }
        }
        return contract;
    }

    @Override
    public TreasurePoolContract.Pool getPool(BigInteger poolId) throws Exception {
        log.info("查询奖池信息: poolId={}", poolId);
        try {
            // 手动编码 getPool(uint256) 函数调用
            Function function = new Function("getPool",
                    Arrays.asList(new Uint256(poolId)),
                    Collections.emptyList());
            String encodedFunction = FunctionEncoder.encode(function);

            String contractAddress = treasureConfigService.getContractAddress();

            // 发送 eth_call
            EthCall response = web3j.ethCall(
                    Transaction.createEthCallTransaction(
                            credentials.getAddress(), contractAddress, encodedFunction),
                    DefaultBlockParameterName.LATEST).send();

            if (response.hasError()) {
                throw new RuntimeException("eth_call 失败: " + response.getError().getMessage());
            }

            String hex = response.getValue();
            if (hex == null || hex.equals("0x") || hex.length() < 66) {
                throw new RuntimeException("eth_call 返回空数据，奖池可能不存在: poolId=" + poolId);
            }

            // 解析 ABI 编码的响应数据
            // getPool 返回一个 struct (dynamic tuple)
            // ABI 编码布局:
            //   [0]  offset to tuple data (= 0x20 = 32)
            //   --- tuple 数据开始 ---
            //   [1]  id (uint256)
            //   [2]  price (uint256)
            //   [3]  totalShares (uint256)
            //   [4]  soldShares (uint256)
            //   [5]  winnerCount (uint256)
            //   [6]  endTime (uint256)
            //   [7]  status (uint8, padded to 32 bytes)
            //   [8]  randomnessRequestId (bytes32)
            //   [9]  prizePerWinner (uint256)
            //   [10] initialPrize (uint256)
            //   [11] offset to winners array (relative to tuple start)
            //   --- winners array data ---
            //   [12] winners.length
            //   [13+] winners[0], winners[1], ...

            byte[] data = Numeric.hexStringToByteArray(hex.substring(2)); // 去掉 "0x"

            // slot 0: offset to tuple start (should be 32)
            int tupleOffset = Numeric.toBigInt(data, 0, 32).intValue();

            // 从 tuple 开始位置读取各字段
            int pos = tupleOffset;
            BigInteger id = Numeric.toBigInt(data, pos, 32); pos += 32;
            BigInteger price = Numeric.toBigInt(data, pos, 32); pos += 32;
            BigInteger totalShares = Numeric.toBigInt(data, pos, 32); pos += 32;
            BigInteger soldShares = Numeric.toBigInt(data, pos, 32); pos += 32;
            BigInteger winnerCount = Numeric.toBigInt(data, pos, 32); pos += 32;
            BigInteger endTime = Numeric.toBigInt(data, pos, 32); pos += 32;
            BigInteger status = Numeric.toBigInt(data, pos, 32); pos += 32;

            // bytes32 randomnessRequestId
            byte[] randomnessRequestId = new byte[32];
            System.arraycopy(data, pos, randomnessRequestId, 0, 32); pos += 32;

            BigInteger prizePerWinner = Numeric.toBigInt(data, pos, 32); pos += 32;

            // initialPrize (uint256)
            BigInteger initialPrize = Numeric.toBigInt(data, pos, 32); pos += 32;

            // offset to winners array (relative to tuple start)
            int winnersRelativeOffset = Numeric.toBigInt(data, pos, 32).intValue();
            int winnersAbsoluteOffset = tupleOffset + winnersRelativeOffset;

            // 解析 winners 动态数组
            int winnersLength = Numeric.toBigInt(data, winnersAbsoluteOffset, 32).intValue();
            List<String> winners = new ArrayList<>();
            int winnersDataStart = winnersAbsoluteOffset + 32;
            for (int i = 0; i < winnersLength; i++) {
                // address 右对齐到 32 bytes，取最后 20 bytes
                byte[] addrBytes = new byte[20];
                System.arraycopy(data, winnersDataStart + i * 32 + 12, addrBytes, 0, 20);
                winners.add("0x" + Numeric.toHexStringNoPrefixZeroPadded(
                        Numeric.toBigInt(addrBytes, 0, 20), 40));
            }

            log.info("查询奖池信息成功: poolId={}, status={}, soldShares={}, initialPrize={}, winners={}",
                    id, status, soldShares, initialPrize, winners.size());

            return new TreasurePoolContract.Pool(
                    id, price, totalShares, soldShares, winnerCount,
                    endTime, status, randomnessRequestId, prizePerWinner, initialPrize, winners);

        } catch (Exception e) {
            log.error("查询奖池信息失败: poolId={}", poolId, e);
            throw new RuntimeException("查询奖池信息失败: " + e.getMessage(), e);
        }
    }

    @Override
    public BigInteger getUserTicket(String userAddress, BigInteger poolId) throws Exception {
        log.info("查询用户票号: userAddress={}, poolId={}", userAddress, poolId);
        try {
            return getContract().getUserTicket(userAddress, poolId).send();
        } catch (Exception e) {
            log.error("查询用户票号失败: userAddress={}, poolId={}", userAddress, poolId, e);
            throw new RuntimeException("查询用户票号失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Boolean isWinner(String userAddress, BigInteger poolId) throws Exception {
        log.info("检查用户是否中奖: userAddress={}, poolId={}", userAddress, poolId);
        try {
            return getContract().isWinner(userAddress, poolId).send();
        } catch (Exception e) {
            log.error("检查用户是否中奖失败: userAddress={}, poolId={}", userAddress, poolId, e);
            throw new RuntimeException("检查用户是否中奖失败: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] getDisplayCode(BigInteger poolId, BigInteger index) throws Exception {
        log.info("获取票号展示码: poolId={}, index={}", poolId, index);
        try {
            return getContract().getDisplayCode(poolId, index).send();
        } catch (Exception e) {
            log.error("获取票号展示码失败: poolId={}, index={}", poolId, index, e);
            throw new RuntimeException("获取票号展示码失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getOwner() throws Exception {
        log.info("查询合约所有者");
        try {
            return getContract().owner().send();
        } catch (Exception e) {
            log.error("查询合约所有者失败", e);
            throw new RuntimeException("查询合约所有者失败: " + e.getMessage(), e);
        }
    }

    @Override
    public BigInteger getPlatformFeeRate() throws Exception {
        log.info("查询平台手续费率");
        try {
            return getContract().platformFeeRate().send();
        } catch (Exception e) {
            log.error("查询平台手续费率失败", e);
            throw new RuntimeException("查询平台手续费率失败: " + e.getMessage(), e);
        }
    }

    @Override
    public BigInteger getPoolCounter() throws Exception {
        log.info("查询奖池计数器");
        try {
            return getContract().poolCounter().send();
        } catch (Exception e) {
            log.error("查询奖池计数器失败", e);
            throw new RuntimeException("查询奖池计数器失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String createPool(BigInteger price, BigInteger totalShares, BigInteger duration, BigInteger winnerCount, BigInteger initialPrize) throws Exception {
        log.info("创建奖池: price={}, totalShares={}, duration={}, winnerCount={}, initialPrize={}", price, totalShares, duration, winnerCount, initialPrize);
        try {
            TransactionReceipt receipt = getContract().createPool(price, totalShares, duration, winnerCount, initialPrize).send();
            String txHash = receipt.getTransactionHash();
            log.info("创建奖池成功: txHash={}", txHash);
            return txHash;
        } catch (Exception e) {
            log.error("创建奖池失败: price={}, totalShares={}, duration={}, winnerCount={}, initialPrize={}", price, totalShares, duration, winnerCount, initialPrize, e);
            throw new RuntimeException("创建奖池失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String executeDraw(BigInteger poolId) throws Exception {
        log.info("===== 触发开奖开始 (两步式): poolId={} =====", poolId);
        try {
            // 0. 先查询链上真实状态，避免数据库与链上状态不一致导致 revert
            TreasurePoolContract.Pool chainPool = getPool(poolId);
            int chainStatus = chainPool.status.intValue();
            log.info("[开奖诊断] 链上奖池状态: poolId={}, chainStatus={} (0=Active,1=Locked,2=Drawing,3=Settled)", poolId, chainStatus);

            // 如果链上已经 Settled，说明之前已开奖完成，直接返回
            if (chainStatus == 3) {
                log.info("[开奖诊断] 链上已 Settled，跳过开奖: poolId={}", poolId);
                return "ALREADY_SETTLED";
            }

            // 如果链上是 Drawing，说明 Step1 已成功但 Step2 (resolveRandomness) 失败了
            // 直接跳到 Step2
            if (chainStatus == 2) {
                log.info("[开奖诊断] 链上已 Drawing，跳过 Step1，直接执行 resolveRandomness: poolId={}", poolId);
                byte[] requestId = chainPool.randomnessRequestId;
                log.info("[Step 2 恢复] requestId: 0x{}", Numeric.toHexStringNoPrefix(requestId));
                String resolveHash = resolveRandomness(requestId);
                log.info("===== 开奖完成 (恢复模式): poolId={}, resolveTx={} =====", poolId, resolveHash);
                return "RECOVERED_" + resolveHash;
            }

            // chainStatus == 0 (Active) 或 1 (Locked)，正常走完整流程
            // 1. 获取签名者信息
            String signerAddress = credentials.getAddress();
            log.info("[开奖诊断] 签名者地址: {}", signerAddress);

            // 2. 查询当前余额
            BigInteger balance = web3j.ethGetBalance(signerAddress,
                    DefaultBlockParameterName.LATEST).send().getBalance();
            log.info("[开奖诊断] 当前余额: {} wei ({} MON)", balance,
                    new java.math.BigDecimal(balance).divide(java.math.BigDecimal.TEN.pow(18), 6, java.math.RoundingMode.DOWN));

            // 3. 获取 VRF 费用
            String vrfFeeStr = treasureConfigService.getVrfFee();
            BigInteger vrfFee = new BigInteger(vrfFeeStr);
            log.info("[开奖诊断] VRF 费用: {} wei", vrfFee);

            // ===== Step 1: 调用 TreasurePool.executeDraw → pool 状态变为 Drawing =====
            log.info("[Step 1] 调用 TreasurePool.executeDraw(poolId={})...", poolId);
            TransactionReceipt drawReceipt = getContract().executeDraw(poolId, vrfFee).send();
            String drawTxHash = drawReceipt.getTransactionHash();
            log.info("[Step 1] executeDraw 交易成功: txHash={}", drawTxHash);

            // 从 DrawStarted 事件提取 requestId
            byte[] requestId = null;
            for (org.web3j.protocol.core.methods.response.Log eventLog : drawReceipt.getLogs()) {
                if (eventLog.getTopics().size() >= 1) {
                    String eventSig = eventLog.getTopics().get(0);
                    String drawStartedSig = org.web3j.abi.EventEncoder.encode(
                            new org.web3j.abi.datatypes.Event("DrawStarted",
                                    Arrays.asList(
                                            new org.web3j.abi.TypeReference<org.web3j.abi.datatypes.generated.Uint256>(true) {},
                                            new org.web3j.abi.TypeReference<org.web3j.abi.datatypes.generated.Bytes32>() {}
                                    )));
                    if (eventSig.equals(drawStartedSig)) {
                        String data = eventLog.getData();
                        requestId = Numeric.hexStringToByteArray(data.substring(2, 66));
                        log.info("[Step 1] 提取到 requestId: 0x{}", Numeric.toHexStringNoPrefix(requestId));
                        break;
                    }
                }
            }

            if (requestId == null) {
                log.error("[Step 1] 未能从 DrawStarted 事件中提取 requestId!");
                throw new RuntimeException("executeDraw 成功但未找到 DrawStarted 事件中的 requestId");
            }

            // ===== Step 2: 调用 MockVRF.resolveRandomness → pool 状态变为 Settled =====
            String resolveHash = resolveRandomness(requestId);
            log.info("===== 开奖完成: poolId={}, drawTx={}, resolveTx={} =====", poolId, drawTxHash, resolveHash);
            return drawTxHash;

        } catch (Exception e) {
            log.error("===== 触发开奖失败: poolId={}, 错误: {} =====", poolId, e.getMessage(), e);
            throw new RuntimeException("触发开奖失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String resolveRandomness(byte[] requestId) throws Exception {
        log.info("[Step 2] 调用 MockVRF.resolveRandomness(requestId=0x{})...",
                Numeric.toHexStringNoPrefix(requestId));
        try {
            String mockVrfAddress = treasureConfigService.getString(
                    cn.iocoder.yudao.module.treasure.enums.TreasureConfigConstants.MOCK_VRF_ADDRESS);
            if (mockVrfAddress == null || mockVrfAddress.isEmpty()) {
                throw new RuntimeException("未配置 MockVRF 合约地址 (contract.mock_vrf_address)");
            }

            // 编码 resolveRandomness(bytes32) 函数调用
            Function function = new Function(
                    "resolveRandomness",
                    Arrays.asList(new org.web3j.abi.datatypes.generated.Bytes32(requestId)),
                    Collections.emptyList());
            String encodedFunction = FunctionEncoder.encode(function);

            // 获取 gas 参数
            BigInteger gasPrice = gasProvider.getGasPrice();
            BigInteger gasLimit = gasProvider.getGasLimit();
            BigInteger nonce = web3j.ethGetTransactionCount(credentials.getAddress(),
                    DefaultBlockParameterName.PENDING).send().getTransactionCount();

            // 构造并签名交易
            org.web3j.crypto.RawTransaction rawTransaction = org.web3j.crypto.RawTransaction.createTransaction(
                    nonce, gasPrice, gasLimit, mockVrfAddress, BigInteger.ZERO, encodedFunction);

            // 获取 chainId
            long chainId = treasureConfigService.getLong(
                    cn.iocoder.yudao.module.treasure.enums.TreasureConfigConstants.CHAIN_ID);

            byte[] signedMessage = org.web3j.crypto.TransactionEncoder.signMessage(rawTransaction, chainId, credentials);
            String hexValue = Numeric.toHexString(signedMessage);

            // 发送交易
            org.web3j.protocol.core.methods.response.EthSendTransaction ethSendTx =
                    web3j.ethSendRawTransaction(hexValue).send();

            if (ethSendTx.hasError()) {
                throw new RuntimeException("发送 resolveRandomness 交易失败: " + ethSendTx.getError().getMessage());
            }

            String txHash = ethSendTx.getTransactionHash();
            log.info("[Step 2] resolveRandomness 交易已发送: txHash={}", txHash);

            // 等待交易确认
            int maxRetries = 30;
            TransactionReceipt receipt = null;
            for (int i = 0; i < maxRetries; i++) {
                Thread.sleep(2000);
                var optReceipt = web3j.ethGetTransactionReceipt(txHash).send().getTransactionReceipt();
                if (optReceipt.isPresent()) {
                    receipt = optReceipt.get();
                    break;
                }
                log.info("[Step 2] 等待确认... ({}/{})", i + 1, maxRetries);
            }

            if (receipt == null) {
                throw new RuntimeException("resolveRandomness 交易超时未确认: " + txHash);
            }

            if ("0x0".equals(receipt.getStatus())) {
                throw new RuntimeException("resolveRandomness 交易 reverted: " + txHash +
                        ", gasUsed=" + receipt.getGasUsed());
            }

            log.info("[Step 2] resolveRandomness 确认成功: txHash={}, gasUsed={}", txHash, receipt.getGasUsed());
            return txHash;

        } catch (Exception e) {
            log.error("[Step 2] resolveRandomness 失败: {}", e.getMessage(), e);
            throw new RuntimeException("resolveRandomness 失败: " + e.getMessage(), e);
        }
    }
}

