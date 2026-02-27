package cn.iocoder.yudao.module.ai.job;

import cn.iocoder.yudao.module.ai.service.agent.AiEventRoomService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 房间恢复任务
 * 服务启动时恢复运行中的房间
 *
 * @author campfire
 */
@Component
@Slf4j
public class RoomRecoveryJob {

    @Resource
    private AiEventRoomService roomService;

    /**
     * 监听 ApplicationReadyEvent，确保应用完全启动后再执行
     * 相比 ContextRefreshedEvent，ApplicationReadyEvent 只会触发一次，且在所有 Bean 完全初始化后触发
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent event) {
        log.info("\n╔══════════════════════════════════════════════════════════════════╗");
        log.info("║  🚀 系统启动完成，开始恢复运行中的房间...                          ║");
        log.info("╚══════════════════════════════════════════════════════════════════╝");

        try {
            long startTime = System.currentTimeMillis();
            roomService.recoverRoomsOnStartup();
            long duration = System.currentTimeMillis() - startTime;

            log.info("\n╔══════════════════════════════════════════════════════════════════╗");
            log.info("║  ✅ 房间恢复完成，耗时: {} ms                                      ║", duration);
            log.info("╚══════════════════════════════════════════════════════════════════╝\n");
        } catch (Exception e) {
            log.error("\n╔══════════════════════════════════════════════════════════════════╗");
            log.error("║  ❌ 房间恢复失败                                                   ║");
            log.error("╚══════════════════════════════════════════════════════════════════╝", e);
        }
    }

}
