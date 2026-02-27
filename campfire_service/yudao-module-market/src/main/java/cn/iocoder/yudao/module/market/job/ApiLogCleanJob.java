package cn.iocoder.yudao.module.market.job;

import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.market.service.api.PmApiLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * API 日志清理定时任务
 * 
 * 每天凌晨 3 点执行，清理 15 天前的日志
 */
@Component
@Slf4j
public class ApiLogCleanJob {

    @Resource
    private PmApiLogService apiLogService;

    /**
     * 每天凌晨 3 点执行
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @TenantIgnore
    public void execute() {
        log.info("[ApiLogCleanJob][开始清理过期 API 日志]");
        try {
            int deleted = apiLogService.cleanExpiredLogs(15);
            log.info("[ApiLogCleanJob][清理完成，删除 {} 条记录]", deleted);
        } catch (Exception e) {
            log.error("[ApiLogCleanJob][清理失败]", e);
        }
    }

}
