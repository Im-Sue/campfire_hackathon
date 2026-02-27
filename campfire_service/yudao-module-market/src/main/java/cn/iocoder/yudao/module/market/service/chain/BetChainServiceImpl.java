package cn.iocoder.yudao.module.market.service.chain;

import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.market.contract.BetLedgerContract;
import cn.iocoder.yudao.module.market.dal.dataobject.chain.PmChainBatchDO;
import cn.iocoder.yudao.module.market.dal.dataobject.order.PmOrderDO;
import cn.iocoder.yudao.module.market.dal.mysql.chain.PmChainBatchMapper;
import cn.iocoder.yudao.module.market.dal.mysql.order.PmOrderMapper;
import cn.iocoder.yudao.module.market.enums.ChainBatchStatusEnum;
import cn.iocoder.yudao.module.market.enums.OrderChainStatusEnum;
import cn.iocoder.yudao.module.market.service.config.PmConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.utils.Numeric;

import jakarta.annotation.Resource;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 下注链上记账服务实现
 *
 * @author Sue
 */
@Service
@Slf4j
public class BetChainServiceImpl implements BetChainService {

    @Resource
    private PmOrderMapper pmOrderMapper;

    @Resource
    private PmChainBatchMapper chainBatchMapper;

    @Resource
    private PmConfigService pmConfigService;

    @Resource
    private Web3j web3j;

    @Resource
    private Credentials credentials;

    @Resource
    private ContractGasProvider gasProvider;

    @Override
    @TenantIgnore
    public void syncPendingOrders() {
        // 1. 读取配置
        int batchSize = pmConfigService.getInteger("chain.sync_batch_size", 50);
        int maxRetry = pmConfigService.getInteger("chain.max_retry_count", 3);
        String contractAddress = pmConfigService.getString("chain.contract_address", "");

        if (!StringUtils.hasText(contractAddress)) {
            log.warn("[syncPendingOrders][合约地址未配置(chain.contract_address 为空)，跳过链上同步]");
            return;
        }

        // 2. 查询待上链订单
        List<PmOrderDO> orders = pmOrderMapper.selectPendingChainOrders(batchSize);
        if (orders.isEmpty()) {
            log.info("[syncPendingOrders][无待上链订单]");
            return;
        }

        log.info("[syncPendingOrders][发现 {} 条待上链订单，合约地址={}]", orders.size(), contractAddress);

        // 3. 创建批次记录
        PmChainBatchDO batch = PmChainBatchDO.builder()
                .batchNo("CB" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase())
                .orderCount(orders.size())
                .status(ChainBatchStatusEnum.PENDING.getStatus())
                .retryCount(0)
                .build();
        chainBatchMapper.insert(batch);

        // 4. 标记订单为"上链中"
        List<Long> orderIds = orders.stream().map(PmOrderDO::getId).collect(Collectors.toList());
        pmOrderMapper.updateChainStatus(orderIds,
                OrderChainStatusEnum.SUBMITTING.getStatus(), batch.getId(), null);

        // 5. 计算 betHash 数组
        List<byte[]> betHashes = new ArrayList<>();
        for (PmOrderDO order : orders) {
            byte[] hash = calculateBetHash(order);
            betHashes.add(hash);
            log.debug("[syncPendingOrders][订单 {} betHash={}]", order.getId(), Numeric.toHexString(hash));
        }

        // 6. 调用合约
        try {
            BetLedgerContract contract = BetLedgerContract.load(
                    contractAddress, web3j, credentials, gasProvider);

            // 更新批次为"已提交"
            batch.setStatus(ChainBatchStatusEnum.SUBMITTED.getStatus());
            batch.setSubmittedAt(LocalDateTime.now());
            chainBatchMapper.updateById(batch);

            log.info("[syncPendingOrders][正在调用合约 recordBets, 批次={}, 数量={}]",
                    batch.getBatchNo(), betHashes.size());

            TransactionReceipt receipt = contract.recordBets(betHashes).send();
            String txHash = receipt.getTransactionHash();
            BigInteger blockNumber = receipt.getBlockNumber();

            log.info("[syncPendingOrders][上链成功, batchId={}, txHash={}, block={}]",
                    batch.getId(), txHash, blockNumber);

            // 7. 成功 → 更新状态
            pmOrderMapper.updateChainStatus(orderIds,
                    OrderChainStatusEnum.CONFIRMED.getStatus(), batch.getId(), txHash);
            batch.setStatus(ChainBatchStatusEnum.CONFIRMED.getStatus());
            batch.setTxHash(txHash);
            batch.setBlockNumber(blockNumber.longValue());
            batch.setConfirmedAt(LocalDateTime.now());
            chainBatchMapper.updateById(batch);

        } catch (Exception e) {
            log.error("[syncPendingOrders][上链失败, batchId={}, error={}]", batch.getId(), e.getMessage(), e);

            // 8. 失败 → 标记失败
            pmOrderMapper.updateChainStatus(orderIds,
                    OrderChainStatusEnum.FAILED.getStatus(), batch.getId(), null);
            batch.setStatus(ChainBatchStatusEnum.FAILED.getStatus());
            batch.setRetryCount(batch.getRetryCount() + 1);
            batch.setErrorMessage(truncateMessage(e.getMessage(), 500));
            chainBatchMapper.updateById(batch);
        }
    }

    /**
     * 计算下注哈希
     * <p>
     * betHash = keccak256(abi.encodePacked(orderId, walletAddress, marketId, outcome, amount, filledAt))
     */
    private byte[] calculateBetHash(PmOrderDO order) {
        // 按照 Solidity abi.encodePacked 规则拼接
        byte[] orderIdBytes = Numeric.toBytesPadded(BigInteger.valueOf(order.getId()), 32);

        // walletAddress: 20 字节（packed）
        byte[] addressBytes;
        if (StringUtils.hasText(order.getWalletAddress())) {
            addressBytes = Numeric.hexStringToByteArray(order.getWalletAddress());
        } else {
            addressBytes = new byte[20]; // 空地址
        }

        byte[] marketIdBytes = Numeric.toBytesPadded(BigInteger.valueOf(order.getMarketId()), 32);
        byte[] outcomeBytes = order.getOutcome().getBytes(StandardCharsets.UTF_8);

        // filledAmount 可能为 null
        long amount = order.getFilledAmount() != null ? order.getFilledAmount() : 0L;
        byte[] amountBytes = Numeric.toBytesPadded(BigInteger.valueOf(amount), 32);

        // filledAt 转时间戳
        long timestamp = 0L;
        if (order.getFilledAt() != null) {
            timestamp = order.getFilledAt().toEpochSecond(ZoneOffset.UTC);
        }
        byte[] timestampBytes = Numeric.toBytesPadded(BigInteger.valueOf(timestamp), 32);

        // 拼接所有字节
        byte[] packed = concatBytes(orderIdBytes, addressBytes, marketIdBytes,
                outcomeBytes, amountBytes, timestampBytes);

        // keccak256
        return Hash.sha3(packed);
    }

    /**
     * 拼接多个字节数组
     */
    private byte[] concatBytes(byte[]... arrays) {
        int totalLen = 0;
        for (byte[] arr : arrays) {
            totalLen += arr.length;
        }
        byte[] result = new byte[totalLen];
        int offset = 0;
        for (byte[] arr : arrays) {
            System.arraycopy(arr, 0, result, offset, arr.length);
            offset += arr.length;
        }
        return result;
    }

    /**
     * 截断错误信息
     */
    private String truncateMessage(String message, int maxLen) {
        if (message == null) {
            return null;
        }
        return message.length() > maxLen ? message.substring(0, maxLen) : message;
    }

}
