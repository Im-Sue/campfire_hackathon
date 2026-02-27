package cn.iocoder.yudao.module.treasure.job;

import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.treasure.event.TreasureEventListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 夺宝补偿回放调度器
 *
 * @author Sue
 */
@Slf4j
@Component
public class TreasureCompensationScheduler {

    @Resource
    private TreasureEventListener treasureEventListener;

    /**
     * 每小时执行一次补偿回放
     */
    @Scheduled(cron = "0 0 * * * ?")
    @TenantIgnore
    public void compensationScan() {
        try {
            log.info("开始执行补偿回放任务");
            treasureEventListener.replayOnce();
        } catch (Exception e) {
            log.error("补偿回放任务执行异常", e);
        }
    }
}

