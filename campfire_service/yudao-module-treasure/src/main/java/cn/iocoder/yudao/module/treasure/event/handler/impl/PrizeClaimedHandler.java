package cn.iocoder.yudao.module.treasure.event.handler.impl;

import cn.iocoder.yudao.module.treasure.config.TreasureProperties;
import cn.iocoder.yudao.module.treasure.dal.dataobject.TreasureTicketDO;
import cn.iocoder.yudao.module.treasure.dal.dataobject.TreasureWinnerDO;
import cn.iocoder.yudao.module.treasure.dal.mysql.TreasureTicketMapper;
import cn.iocoder.yudao.module.treasure.dal.mysql.TreasureWinnerMapper;
import cn.iocoder.yudao.module.treasure.event.handler.EventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.core.methods.response.Log;

import jakarta.annotation.Resource;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * PrizeClaimed 事件处理器
 *
 * @author Sue
 */
@Slf4j
@Component("PrizeClaimedHandler")
public class PrizeClaimedHandler implements EventHandler {

    @Resource
    private TreasureTicketMapper ticketMapper;

    @Resource
    private TreasureWinnerMapper winnerMapper;

    @Resource
    private TreasureProperties treasureProperties;

    @Resource
    private cn.iocoder.yudao.module.treasure.service.config.TreasureConfigService treasureConfigService;

    @Override
    public void handle(Log eventLog) throws Exception {
        log.info("处理 PrizeClaimed 事件: txHash={}", eventLog.getTransactionHash());

        // 解析事件参数
        // event PrizeClaimed(uint256 indexed poolId, address indexed winner, uint256 amount)
        List<String> topics = eventLog.getTopics();
        String data = eventLog.getData();

        // poolId 和 winner 是 indexed 参数
        BigInteger poolId = new BigInteger(topics.get(1).substring(2), 16);
        String winnerAddress = "0x" + topics.get(2).substring(26); // 去掉前面的填充0

        // amount 在 data 中
        @SuppressWarnings({"rawtypes", "unchecked"})
        List<TypeReference<Type>> outputParameters = (List) Collections.singletonList(
                (TypeReference) new TypeReference<Uint256>() {}
        );
        List<Type> params = FunctionReturnDecoder.decode(data, outputParameters);

        BigInteger amount = (BigInteger) params.get(0).getValue();

        log.info("PrizeClaimed 事件参数: poolId={}, winner={}, amount={}",
                poolId, winnerAddress, amount);

        String contractAddress = treasureConfigService.getContractAddress();
        Integer chainId = treasureConfigService.getChainId();
        LocalDateTime claimTime = LocalDateTime.now();

        // 更新票号的领奖状态
        TreasureTicketDO ticket = ticketMapper.selectByUserAndPool(
                winnerAddress, poolId.longValue(), contractAddress, chainId);

        if (ticket != null) {
            ticket.setIsClaimed(true);
            ticket.setClaimTxHash(eventLog.getTransactionHash());
            ticket.setClaimTime(claimTime);
            ticketMapper.updateById(ticket);

            // 更新中奖记录的领奖状态
            TreasureWinnerDO winner = winnerMapper.selectByPoolIdAndTicketId(
                    poolId.longValue(), ticket.getId(), contractAddress, chainId);

            if (winner != null) {
                winner.setIsClaimed(true);
                winner.setClaimTxHash(eventLog.getTransactionHash());
                winner.setClaimTime(claimTime);
                winnerMapper.updateById(winner);
            }

            log.info("PrizeClaimed 事件处理完成: poolId={}, winner={}", poolId, winnerAddress);
        } else {
            log.warn("未找到中奖者的票号: poolId={}, winner={}", poolId, winnerAddress);
        }
    }
}
