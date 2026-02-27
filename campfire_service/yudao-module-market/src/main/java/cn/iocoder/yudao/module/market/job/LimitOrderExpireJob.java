package cn.iocoder.yudao.module.market.job;

import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.market.service.order.PmOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 限价单过期检查任务
 */
@Component
@Slf4j
public class LimitOrderExpireJob {

    @Resource
    private PmOrderService pmOrderService;

    /**
     * 每小时检查过期的限价单
     */
    @Scheduled(cron = "0 0 * * * ?")
    @TenantIgnore
    public void execute() {
        log.info("[LimitOrderExpireJob][开始检查过期限价单]");
        try {
            pmOrderService.processExpiredOrders();
            log.info("[LimitOrderExpireJob][过期限价单检查完成]");
        } catch (Exception e) {
            log.error("[LimitOrderExpireJob][检查过期限价单失败]", e);
        }
    }

}
