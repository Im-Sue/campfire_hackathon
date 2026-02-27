package cn.iocoder.yudao.module.treasure.event.handler.impl;

import cn.iocoder.yudao.module.treasure.config.TreasureProperties;
import cn.iocoder.yudao.module.treasure.dal.dataobject.TreasurePoolDO;
import cn.iocoder.yudao.module.treasure.dal.dataobject.TreasureTicketDO;
import cn.iocoder.yudao.module.treasure.dal.dataobject.TreasureWinnerDO;
import cn.iocoder.yudao.module.treasure.dal.mysql.TreasurePoolMapper;
import cn.iocoder.yudao.module.treasure.dal.mysql.TreasureTicketMapper;
import cn.iocoder.yudao.module.treasure.dal.mysql.TreasureWinnerMapper;
import cn.iocoder.yudao.module.treasure.enums.PoolStatusEnum;
import cn.iocoder.yudao.module.treasure.event.handler.EventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.core.methods.response.Log;

import jakarta.annotation.Resource;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DrawCompleted 事件处理器
 *
 * @author Sue
 */
@Slf4j
@Component("DrawCompletedHandler")
public class DrawCompletedHandler implements EventHandler {

    @Resource
    private TreasurePoolMapper poolMapper;

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
        log.info("处理 DrawCompleted 事件: txHash={}", eventLog.getTransactionHash());

        // 解析事件参数
        // event DrawCompleted(uint256 indexed poolId, address[] winners, uint256 prizePerWinner)
        List<String> topics = eventLog.getTopics();
        String data = eventLog.getData();

        // poolId 是 indexed 参数
        BigInteger poolId = new BigInteger(topics.get(1).substring(2), 16);

        // winners 和 prizePerWinner 在 data 中
        @SuppressWarnings({"rawtypes", "unchecked"})
        List<TypeReference<Type>> outputParameters = (List) new ArrayList<TypeReference<?>>();
        outputParameters.add((TypeReference) new TypeReference<DynamicArray<Address>>() {}); // winners
        outputParameters.add((TypeReference) new TypeReference<Uint256>() {}); // prizePerWinner
        List<Type> params = FunctionReturnDecoder.decode(data, outputParameters);

        @SuppressWarnings("unchecked")
        DynamicArray<Address> winnerArray = (DynamicArray<Address>) params.get(0);
        List<String> winners = winnerArray.getValue()
                .stream()
                .map(Address::getValue)
                .collect(Collectors.toList());
        BigInteger prizePerWinner = (BigInteger) params.get(1).getValue();

        log.info("DrawCompleted 事件参数: poolId={}, winners={}, prizePerWinner={}",
                poolId, winners, prizePerWinner);

        String contractAddress = treasureConfigService.getContractAddress();
        Integer chainId = treasureConfigService.getChainId();

        // 更新奖池状态
        TreasurePoolDO pool = poolMapper.selectByPoolId(poolId.longValue(), contractAddress, chainId);
        if (pool == null) {
            log.error("奖池不存在: poolId={}", poolId);
            return;
        }

        pool.setStatus(PoolStatusEnum.SETTLED.getStatus());
        pool.setPrizePerWinner(prizePerWinner.toString());
        poolMapper.updateById(pool);

        // 更新中奖票号
        for (String winnerAddress : winners) {
            // 查找该用户的票号
            TreasureTicketDO ticket = ticketMapper.selectByUserAndPool(
                    winnerAddress, poolId.longValue(), contractAddress, chainId);

            if (ticket != null) {
                // 更新票号为中奖状态
                ticket.setIsWinner(true);
                ticket.setPrizeAmount(prizePerWinner.toString());
                ticketMapper.updateById(ticket);

                // 创建中奖记录
                TreasureWinnerDO winner = TreasureWinnerDO.builder()
                        .poolId(poolId.longValue())
                        .ticketId(ticket.getId())
                        .contractAddress(contractAddress)
                        .chainId(chainId)
                        .winnerAddress(winnerAddress)
                        .ticketIndex(ticket.getTicketIndex())
                        .prizeAmount(prizePerWinner.toString())
                        .isClaimed(false)
                        .build();

                winnerMapper.insert(winner);

                log.info("创建中奖记录: poolId={}, winner={}, ticketIndex={}",
                        poolId, winnerAddress, ticket.getTicketIndex());
            } else {
                log.warn("未找到中奖者的票号: poolId={}, winner={}", poolId, winnerAddress);
            }
        }

        log.info("DrawCompleted 事件处理完成: poolId={}, winners count={}", poolId, winners.size());
    }
}
