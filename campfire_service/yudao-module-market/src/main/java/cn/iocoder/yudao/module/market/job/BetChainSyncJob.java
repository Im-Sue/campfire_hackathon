package cn.iocoder.yudao.module.market.job;

import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.market.service.chain.BetChainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

/**
 * 下注链上记账定时任务
 * <p>
 * 定期扫描已成交但未上链的订单，批量提交到链上
 *
 * @author Sue
 */
@Component
@Slf4j
public class BetChainSyncJob {

    @Resource
    private BetChainService betChainService;

    @PostConstruct
    public void init() {
        log.info("[BetChainSyncJob][链上记账定时任务已初始化，每 60 秒执行一次]");
    }

    /**
     * 每 60 秒批量上链
     */
    @Scheduled(fixedRate = 60000)
    @TenantIgnore
    public void execute() {
        try {
            log.info("[BetChainSyncJob][开始执行链上记账同步]");
            betChainService.syncPendingOrders();
        } catch (Exception e) {
            log.error("[BetChainSyncJob][批量上链任务失败]", e);
        }
    }

}
