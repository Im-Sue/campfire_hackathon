package cn.iocoder.yudao.module.treasure.job;

import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.treasure.dal.dataobject.TreasurePoolDO;
import cn.iocoder.yudao.module.treasure.dal.mysql.TreasurePoolMapper;
import cn.iocoder.yudao.module.treasure.enums.PoolStatusEnum;
import cn.iocoder.yudao.module.treasure.service.pool.TreasurePoolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 夺宝开奖调度器
 *
 * @author Sue
 */
@Slf4j
@Component
public class TreasureDrawScheduler {

    @Resource
    private TreasurePoolMapper poolMapper;

    @Resource
    private TreasurePoolService poolService;

    /**
     * 每分钟扫描到期奖池并触发开奖
     */
    @Scheduled(cron = "0 * * * * ?")
    @TenantIgnore
    public void checkExpiredPools() {
        log.info("[TreasureDrawScheduler] 开始扫描到期奖池, 当前时间: {}", LocalDateTime.now());
        // TODO: 暂时关闭
        // return;
        
        LocalDateTime now = LocalDateTime.now();
        List<TreasurePoolDO> expiredPools = poolMapper.selectEndedButNotDrawnPools(now);
        if (expiredPools == null || expiredPools.isEmpty()) {
            log.info("[TreasureDrawScheduler] 未发现到期奖池(status=0 且 endTime<=now)");
            return;
        }

        log.info("[TreasureDrawScheduler] 扫描到 {} 个到期奖池待开奖", expiredPools.size());
        for (TreasurePoolDO pool : expiredPools) {
            try {
                if (pool.getSoldShares() == null || pool.getSoldShares() <= 0) {
                    log.info("[TreasureDrawScheduler] 奖池 {} 无参与者，直接关闭", pool.getPoolId());
                    pool.setStatus(PoolStatusEnum.SETTLED.getStatus());
                    pool.setDrawTime(LocalDateTime.now());
                    pool.setPrizePerWinner("0");
                    poolMapper.updateById(pool);
                    continue;
                }

                // 先更新为已锁定，再走开奖流程，避免并发重复触发
                pool.setStatus(PoolStatusEnum.LOCKED.getStatus());
                poolMapper.updateById(pool);

                String txHash = poolService.executeDraw(pool.getPoolId());

                // 处理特殊返回值：链上已完成但数据库未同步的场景
                if ("ALREADY_SETTLED".equals(txHash)) {
                    log.info("[TreasureDrawScheduler] 奖池 {} 链上已 Settled，同步数据库状态", pool.getPoolId());
                    poolService.syncPoolFromChain(java.math.BigInteger.valueOf(pool.getPoolId()));
                } else {
                    log.info("[TreasureDrawScheduler] 奖池 {} 触发开奖成功，交易哈希：{}", pool.getPoolId(), txHash);
                }
            } catch (Exception e) {
                log.error("[TreasureDrawScheduler] 奖池 {} 触发开奖失败", pool.getPoolId(), e);
                // 从链上同步真实状态，而非无条件回滚为 ACTIVE
                try {
                    poolService.syncPoolFromChain(java.math.BigInteger.valueOf(pool.getPoolId()));
                    log.info("[TreasureDrawScheduler] 奖池 {} 已从链上同步状态", pool.getPoolId());
                } catch (Exception syncEx) {
                    log.error("[TreasureDrawScheduler] 奖池 {} 链上状态同步失败，回滚为 ACTIVE", pool.getPoolId(), syncEx);
                    try {
                        pool.setStatus(PoolStatusEnum.ACTIVE.getStatus());
                        poolMapper.updateById(pool);
                    } catch (Exception rollbackEx) {
                        log.error("[TreasureDrawScheduler] 奖池 {} 状态回滚失败", pool.getPoolId(), rollbackEx);
                    }
                }
            }
        }
        
    }
}

