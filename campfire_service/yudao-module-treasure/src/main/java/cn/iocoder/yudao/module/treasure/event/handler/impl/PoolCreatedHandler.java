package cn.iocoder.yudao.module.treasure.event.handler.impl;

import cn.iocoder.yudao.module.treasure.config.TreasureProperties;
import cn.iocoder.yudao.module.treasure.contract.TreasurePoolContract;
import cn.iocoder.yudao.module.treasure.dal.dataobject.TreasurePoolDO;
import cn.iocoder.yudao.module.treasure.dal.mysql.TreasurePoolMapper;
import cn.iocoder.yudao.module.treasure.enums.PoolStatusEnum;
import cn.iocoder.yudao.module.treasure.event.handler.EventHandler;
import cn.iocoder.yudao.module.treasure.service.contract.TreasureContractService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.core.methods.response.Log;

import jakarta.annotation.Resource;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * PoolCreated 事件处理器
 *
 * @author Sue
 */
@Slf4j
@Component("PoolCreatedHandler")
public class PoolCreatedHandler implements EventHandler {

    @Resource
    private TreasurePoolMapper poolMapper;

    @Resource
    private TreasureContractService contractService;

    @Resource
    private TreasureProperties treasureProperties;

    @Resource
    private cn.iocoder.yudao.module.treasure.service.config.TreasureConfigService treasureConfigService;

    @Override
    public void handle(Log eventLog) throws Exception {
        log.info("处理 PoolCreated 事件: txHash={}", eventLog.getTransactionHash());

        // 解析事件参数
        // event PoolCreated(uint256 indexed poolId, uint256 price, uint256 totalShares, uint256 winnerCount, uint256 endTime)
        List<String> topics = eventLog.getTopics();
        String data = eventLog.getData();

        // poolId 是 indexed 参数，在 topics[1] 中
        BigInteger poolId = new BigInteger(topics.get(1).substring(2), 16);

        // 其他参数在 data 中
        @SuppressWarnings({"rawtypes", "unchecked"})
        List<TypeReference<Type>> outputParameters = (List) new ArrayList<TypeReference<?>>();
        outputParameters.add((TypeReference) new TypeReference<Uint256>() {}); // price
        outputParameters.add((TypeReference) new TypeReference<Uint256>() {}); // totalShares
        outputParameters.add((TypeReference) new TypeReference<Uint256>() {}); // winnerCount
        outputParameters.add((TypeReference) new TypeReference<Uint256>() {}); // endTime
        outputParameters.add((TypeReference) new TypeReference<Uint256>() {}); // initialPrize
        List<Type> params = FunctionReturnDecoder.decode(data, outputParameters);

        BigInteger price = (BigInteger) params.get(0).getValue();
        BigInteger totalShares = (BigInteger) params.get(1).getValue();
        BigInteger winnerCount = (BigInteger) params.get(2).getValue();
        BigInteger endTime = (BigInteger) params.get(3).getValue();
        BigInteger initialPrize = (BigInteger) params.get(4).getValue();

        log.info("PoolCreated 事件参数: poolId={}, price={}, totalShares={}, winnerCount={}, endTime={}, initialPrize={}",
                poolId, price, totalShares, winnerCount, endTime, initialPrize);

        // 检查是否已存在
        String contractAddress = treasureConfigService.getContractAddress();
        Integer chainId = treasureConfigService.getChainId();

        TreasurePoolDO existingPool = poolMapper.selectByPoolId(poolId.longValue(), contractAddress, chainId);
        if (existingPool != null) {
            log.warn("奖池已存在，跳过: poolId={}", poolId);
            return;
        }

        // 转换时间戳为 LocalDateTime
        LocalDateTime endDateTime = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(endTime.longValue()),
                ZoneId.systemDefault()
        );

        // 创建奖池记录
        TreasurePoolDO pool = TreasurePoolDO.builder()
                .poolId(poolId.longValue())
                .contractAddress(contractAddress)
                .chainId(chainId)
                .price(price.toString())
                .totalShares(totalShares.intValue())
                .soldShares(0)
                .winnerCount(winnerCount.intValue())
                .endTime(endDateTime)
                .status(PoolStatusEnum.ACTIVE.getStatus())
                .initialPrize(initialPrize.toString())
                .build();

        poolMapper.insert(pool);

        log.info("PoolCreated 事件处理完成: poolId={}", poolId);
    }
}
