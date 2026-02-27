package cn.iocoder.yudao.module.treasure.event.handler.impl;

import cn.iocoder.yudao.module.treasure.config.TreasureProperties;
import cn.iocoder.yudao.module.treasure.dal.dataobject.TreasurePoolDO;
import cn.iocoder.yudao.module.treasure.dal.mysql.TreasurePoolMapper;
import cn.iocoder.yudao.module.treasure.enums.PoolStatusEnum;
import cn.iocoder.yudao.module.treasure.event.handler.EventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.utils.Numeric;

import jakarta.annotation.Resource;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * DrawStarted 事件处理器
 *
 * @author Sue
 */
@Slf4j
@Component("DrawStartedHandler")
public class DrawStartedHandler implements EventHandler {

    @Resource
    private TreasurePoolMapper poolMapper;

    @Resource
    private TreasureProperties treasureProperties;

    @Resource
    private cn.iocoder.yudao.module.treasure.service.config.TreasureConfigService treasureConfigService;

    @Override
    public void handle(Log eventLog) throws Exception {
        log.info("处理 DrawStarted 事件: txHash={}", eventLog.getTransactionHash());

        // 解析事件参数
        // event DrawStarted(uint256 indexed poolId, bytes32 requestId)
        List<String> topics = eventLog.getTopics();
        String data = eventLog.getData();

        // poolId 是 indexed 参数
        BigInteger poolId = new BigInteger(topics.get(1).substring(2), 16);

        // requestId 在 data 中
        @SuppressWarnings({"rawtypes", "unchecked"})
        List<TypeReference<Type>> outputParameters = (List) Collections.singletonList(
                (TypeReference) new TypeReference<Bytes32>() {}
        );
        List<Type> params = FunctionReturnDecoder.decode(data, outputParameters);

        byte[] requestIdBytes = (byte[]) params.get(0).getValue();
        String requestId = Numeric.toHexString(requestIdBytes);

        log.info("DrawStarted 事件参数: poolId={}, requestId={}", poolId, requestId);

        String contractAddress = treasureConfigService.getContractAddress();
        Integer chainId = treasureConfigService.getChainId();

        // 更新奖池状态
        TreasurePoolDO pool = poolMapper.selectByPoolId(poolId.longValue(), contractAddress, chainId);
        if (pool == null) {
            log.error("奖池不存在: poolId={}", poolId);
            return;
        }

        pool.setStatus(PoolStatusEnum.DRAWING.getStatus());
        pool.setRandomnessRequestId(requestId);
        pool.setDrawTxHash(eventLog.getTransactionHash());
        pool.setDrawBlockNumber(eventLog.getBlockNumber().longValue());
        pool.setDrawTime(LocalDateTime.now());

        poolMapper.updateById(pool);

        log.info("DrawStarted 事件处理完成: poolId={}", poolId);
    }
}
