package cn.iocoder.yudao.module.market.job;

import cn.iocoder.yudao.module.market.service.ws.PmWsLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * WS 日志清理定时任务
 *
 * 每天凌晨 2 点执行，清理 15 天前的日志
 */
@Component
@Slf4j
public class WsLogCleanJob {

    /**
     * 日志保留天数
     */
    private static final int RETENTION_DAYS = 15;

    @Resource
    private PmWsLogService wsLogService;

    /**
     * 清理过期 WS 日志
     *
     * 每天凌晨 2 点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void execute() {
        log.info("[WsLogCleanJob][开始清理 WS 日志]");
        try {
            int deleted = wsLogService.cleanExpiredLogs(RETENTION_DAYS);
            log.info("[WsLogCleanJob][清理完成，删除 {} 条日志]", deleted);
        } catch (Exception e) {
            log.error("[WsLogCleanJob][清理失败]", e);
        }
    }

}
