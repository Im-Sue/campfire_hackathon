package cn.iocoder.yudao.module.treasure.service.pool.impl;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.treasure.controller.admin.pool.vo.TreasurePoolCreateRespVO;
import cn.iocoder.yudao.module.treasure.config.TreasureProperties;
import cn.iocoder.yudao.module.treasure.controller.admin.pool.vo.TreasurePoolPageReqVO;
import cn.iocoder.yudao.module.treasure.dal.dataobject.TreasurePoolDO;
import cn.iocoder.yudao.module.treasure.dal.mysql.TreasurePoolMapper;
import cn.iocoder.yudao.module.treasure.enums.PoolStatusEnum;
import cn.iocoder.yudao.module.treasure.service.config.TreasureConfigService;
import cn.iocoder.yudao.module.treasure.service.contract.TreasureContractService;
import cn.iocoder.yudao.module.treasure.service.pool.TreasurePoolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 奖池管理 Service 实现类
 *
 * @author Sue
 */
@Slf4j
@Service
public class TreasurePoolServiceImpl implements TreasurePoolService {

    @Resource
    private TreasurePoolMapper poolMapper;

    @Resource
    private TreasureContractService contractService;

    @Resource
    private TreasureProperties treasureProperties;

    @Resource
    private TreasureConfigService treasureConfigService;

    @Override
    public TreasurePoolCreateRespVO createPool(String price, Integer totalShares, Integer duration, Integer winnerCount, String initialPrize) throws Exception {
        log.info("创建奖池: price={}, totalShares={}, duration={}, winnerCount={}, initialPrize={}", price, totalShares, duration, winnerCount, initialPrize);

        if (winnerCount > totalShares) {
            throw new IllegalArgumentException("中奖名额不能大于总份数");
        }
        if (winnerCount > 10) {
            throw new IllegalArgumentException("中奖名额不能超过 10");
        }

        // 将 MON 单位转换为 wei（1 MON = 10^18 wei）
        BigDecimal priceMon = new BigDecimal(price);
        if (priceMon.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("单价必须大于 0");
        }
        BigInteger priceWei = priceMon.multiply(BigDecimal.TEN.pow(18)).toBigInteger();

        // 转换初始奖金（MON → wei）
        BigInteger initialPrizeWei = BigInteger.ZERO;
        if (initialPrize != null && !initialPrize.isBlank()) {
            BigDecimal initialPrizeMon = new BigDecimal(initialPrize);
            if (initialPrizeMon.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("初始奖金不能为负数");
            }
            initialPrizeWei = initialPrizeMon.multiply(BigDecimal.TEN.pow(18)).toBigInteger();
        }

        String txHash = contractService.createPool(priceWei,
                BigInteger.valueOf(totalShares),
                BigInteger.valueOf(duration),
                BigInteger.valueOf(winnerCount),
                initialPrizeWei);

        // 通过当前计数器获取最新 poolId
        BigInteger poolCounter = contractService.getPoolCounter();
        Long poolId = poolCounter.longValue();

        // 立即同步一份链上数据到本地，保证列表可见
        syncPoolFromChain(poolCounter, txHash);

        return TreasurePoolCreateRespVO.builder()
                .poolId(poolId)
                .txHash(txHash)
                .build();
    }

    @Override
    public PageResult<TreasurePoolDO> getPoolPage(TreasurePoolPageReqVO pageReqVO) {
        return poolMapper.selectPage(pageReqVO);
    }

    @Override
    public TreasurePoolDO getPool(Long id) {
        return poolMapper.selectById(id);
    }

    @Override
    public TreasurePoolDO getPoolByPoolId(Long poolId) {
        String contractAddress = treasureConfigService.getContractAddress();
        Integer chainId = treasureConfigService.getChainId();
        return poolMapper.selectByPoolId(poolId, contractAddress, chainId);
    }

    @Override
    public TreasurePoolDO syncPoolFromChain(BigInteger poolId) throws Exception {
        return syncPoolFromChain(poolId, null);
    }

    @Override
    public TreasurePoolDO syncPoolFromChain(BigInteger poolId, String createTxHash) throws Exception {
        log.info("同步链上奖池数据: poolId={}", poolId);

        String contractAddress = treasureConfigService.getContractAddress();
        Integer chainId = treasureConfigService.getChainId();

        // 从链上获取奖池数据
        var pool = contractService.getPool(poolId);

        LocalDateTime endTime = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(pool.endTime.longValue()),
                ZoneId.systemDefault()
        );

        // 使用固定租户ID=1，确保数据在后台管理页面可见
        return TenantUtils.execute(1L, () -> {
            // 检查是否已存在（跨租户查询）
            TreasurePoolDO existingPool = poolMapper.selectByPoolId(poolId.longValue(), contractAddress, chainId);

            if (existingPool != null) {
                // 更新现有记录
                existingPool.setPrice(pool.price.toString());
                existingPool.setTotalShares(pool.totalShares.intValue());
                existingPool.setSoldShares(pool.soldShares.intValue());
                existingPool.setWinnerCount(pool.winnerCount.intValue());
                existingPool.setEndTime(endTime);
                existingPool.setStatus(pool.status.intValue());
                existingPool.setPrizePerWinner(pool.prizePerWinner.toString());
                // 如果之前没有记录 createTxHash，补录
                if (createTxHash != null && existingPool.getCreateTxHash() == null) {
                    existingPool.setCreateTxHash(createTxHash);
                }

                poolMapper.updateById(existingPool);
                log.info("更新奖池数据: poolId={}", poolId);
                return existingPool;
            } else {
                // 创建新记录
                TreasurePoolDO newPool = TreasurePoolDO.builder()
                        .poolId(poolId.longValue())
                        .contractAddress(contractAddress)
                        .chainId(chainId)
                        .createTxHash(createTxHash)
                        .price(pool.price.toString())
                        .totalShares(pool.totalShares.intValue())
                        .soldShares(pool.soldShares.intValue())
                        .winnerCount(pool.winnerCount.intValue())
                        .endTime(endTime)
                        .status(pool.status.intValue())
                        .prizePerWinner(pool.prizePerWinner.toString())
                        .initialPrize(pool.initialPrize.toString())
                        .build();

                try {
                    poolMapper.insert(newPool);
                    log.info("创建奖池数据: poolId={}", poolId);
                } catch (DuplicateKeyException e) {
                    // 并发情况：事件监听器已经插入了这条记录，改为更新
                    log.warn("奖池记录已存在(并发插入)，改为更新: poolId={}", poolId);
                    existingPool = poolMapper.selectByPoolId(poolId.longValue(), contractAddress, chainId);
                    if (existingPool != null) {
                        if (createTxHash != null && existingPool.getCreateTxHash() == null) {
                            existingPool.setCreateTxHash(createTxHash);
                        }
                        poolMapper.updateById(existingPool);
                        return existingPool;
                    }
                }
                return newPool;
            }
        });
    }

    @Override
    public String executeDraw(Long poolId) throws Exception {
        log.info("执行开奖: poolId={}", poolId);

        String contractAddress = treasureConfigService.getContractAddress();
        Integer chainId = treasureConfigService.getChainId();

        // 检查奖池状态
        TreasurePoolDO pool = poolMapper.selectByPoolId(poolId, contractAddress, chainId);
        if (pool == null) {
            throw new IllegalArgumentException("奖池不存在: poolId=" + poolId);
        }

        if (!pool.getStatus().equals(PoolStatusEnum.LOCKED.getStatus())
                && !pool.getStatus().equals(PoolStatusEnum.ACTIVE.getStatus())) {
            throw new IllegalStateException("奖池状态不正确，无法开奖: status=" + pool.getStatus());
        }

        // 调用合约执行开奖
        String txHash = contractService.executeDraw(BigInteger.valueOf(poolId));

        log.info("开奖交易已提交: poolId={}, txHash={}", poolId, txHash);
        return txHash;
    }

    @Override
    public PageResult<TreasurePoolDO> getActivePoolPage(TreasurePoolPageReqVO pageReqVO) {
        // 只返回进行中的奖池
        pageReqVO.setStatus(PoolStatusEnum.ACTIVE.getStatus());
        return poolMapper.selectPage(pageReqVO);
    }

    @Override
    public TreasurePoolDO getFirstActivePool() {
        return poolMapper.selectFirstActivePool();
    }

    @Override
    public PageResult<TreasurePoolDO> getHistoryPoolPage(PageParam pageParam) {
        TreasurePoolPageReqVO reqVO = new TreasurePoolPageReqVO();
        reqVO.setPageNo(pageParam.getPageNo());
        reqVO.setPageSize(pageParam.getPageSize());
        return poolMapper.selectHistoryPage(reqVO);
    }
}

