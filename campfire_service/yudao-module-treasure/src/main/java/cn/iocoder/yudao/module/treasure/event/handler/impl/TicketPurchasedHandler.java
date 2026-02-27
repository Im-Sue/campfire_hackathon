package cn.iocoder.yudao.module.treasure.event.handler.impl;

import cn.iocoder.yudao.module.treasure.config.TreasureProperties;
import cn.iocoder.yudao.module.treasure.dal.dataobject.TreasurePoolDO;
import cn.iocoder.yudao.module.treasure.dal.dataobject.TreasureTicketDO;
import cn.iocoder.yudao.module.treasure.dal.mysql.TreasurePoolMapper;
import cn.iocoder.yudao.module.treasure.dal.mysql.TreasureTicketMapper;
import cn.iocoder.yudao.module.treasure.event.handler.EventHandler;
import cn.iocoder.yudao.module.treasure.service.contract.TreasureContractService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.response.Log;

import jakarta.annotation.Resource;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

/**
 * TicketPurchased 事件处理器
 *
 * @author Sue
 */
@Slf4j
@Component("TicketPurchasedHandler")
public class TicketPurchasedHandler implements EventHandler {

    @Resource
    private TreasureTicketMapper ticketMapper;

    @Resource
    private TreasurePoolMapper poolMapper;

    @Resource
    private TreasureContractService contractService;

    @Resource
    private TreasureProperties treasureProperties;

    @Resource
    private cn.iocoder.yudao.module.treasure.service.config.TreasureConfigService treasureConfigService;

    @Resource
    private Web3j web3j;

    @Override
    public void handle(Log eventLog) throws Exception {
        log.info("处理 TicketPurchased 事件: txHash={}", eventLog.getTransactionHash());

        // 解析事件参数
        // event TicketPurchased(uint256 indexed poolId, address indexed buyer, uint256 ticketIndex)
        List<String> topics = eventLog.getTopics();
        String data = eventLog.getData();

        // poolId 和 buyer 是 indexed 参数
        BigInteger poolId = new BigInteger(topics.get(1).substring(2), 16);
        String buyerAddress = "0x" + topics.get(2).substring(26); // 去掉前面的填充0

        // ticketIndex 在 data 中
        @SuppressWarnings({"rawtypes", "unchecked"})
        List<TypeReference<Type>> outputParameters = (List) Collections.singletonList(
                (TypeReference) new TypeReference<Uint256>() {}
        );
        List<Type> params = FunctionReturnDecoder.decode(data, outputParameters);

        BigInteger ticketIndex = (BigInteger) params.get(0).getValue();

        log.info("TicketPurchased 事件参数: poolId={}, buyer={}, ticketIndex={}",
                poolId, buyerAddress, ticketIndex);

        String contractAddress = treasureConfigService.getContractAddress();
        Integer chainId = treasureConfigService.getChainId();

        // 检查是否已存在
        TreasureTicketDO existingTicket = ticketMapper.selectByPoolIdAndIndex(
                poolId.longValue(), ticketIndex.intValue(), contractAddress, chainId);
        if (existingTicket != null) {
            log.warn("票号已存在，跳过: poolId={}, ticketIndex={}", poolId, ticketIndex);
            return;
        }

        // 获取区块时间戳
        BigInteger blockNumber = eventLog.getBlockNumber();
        BigInteger blockTimestamp = web3j.ethGetBlockByNumber(new DefaultBlockParameterNumber(blockNumber), false)
                .send().getBlock().getTimestamp();
        LocalDateTime purchaseTime = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(blockTimestamp.longValue()),
                ZoneId.systemDefault());

        // 计算展示码
        byte[] displayCodeBytes = contractService.getDisplayCode(poolId, ticketIndex);
        String displayCode = formatDisplayCode(displayCodeBytes);

        // 创建票号记录
        TreasureTicketDO ticket = TreasureTicketDO.builder()
                .poolId(poolId.longValue())
                .ticketIndex(ticketIndex.intValue())
                .contractAddress(contractAddress)
                .chainId(chainId)
                .ownerAddress(buyerAddress)
                .displayCode(displayCode)
                .isWinner(false)
                .isClaimed(false)
                .purchaseTxHash(eventLog.getTransactionHash())
                .purchaseBlockNumber(blockNumber.longValue())
                .purchaseTime(purchaseTime)
                .build();

        ticketMapper.insert(ticket);

        // 更新奖池的已售份数
        TreasurePoolDO pool = poolMapper.selectByPoolId(poolId.longValue(), contractAddress, chainId);
        if (pool != null) {
            pool.setSoldShares(pool.getSoldShares() + 1);
            poolMapper.updateById(pool);
        }

        log.info("TicketPurchased 事件处理完成: poolId={}, ticketIndex={}", poolId, ticketIndex);
    }

    /**
     * 格式化展示码为 XXXX-XXXX
     */
    private String formatDisplayCode(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "0000-0000";
        }
        StringBuilder hex = new StringBuilder();
        for (byte oneByte : bytes) {
            hex.append(String.format("%02X", oneByte));
        }
        String normalized = hex.substring(0, Math.min(8, hex.length()));
        if (normalized.length() < 8) {
            normalized = String.format("%-8s", normalized).replace(' ', '0');
        }
        return normalized.substring(0, 4) + "-" + normalized.substring(4, 8);
    }
}
