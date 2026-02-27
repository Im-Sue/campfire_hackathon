package cn.iocoder.yudao.module.ai.service.agent.event;

import cn.iocoder.yudao.module.ai.service.agent.orchestrator.RoomOrchestrator;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 房间生命周期事件监听器
 * 独立的事件监听组件，避免循环依赖
 *
 * @author campfire
 */
@Component
@Slf4j
public class RoomLifecycleEventListener {

    @Resource
    private RoomOrchestrator roomOrchestrator;

    /**
     * 监听房间生命周期事件
     */
    @EventListener
    public void handleRoomLifecycleEvent(RoomLifecycleEvent event) {
        log.info("  📨 [EventListener] 收到房间事件: roomId={}, action={}",
                event.getRoomId(), event.getAction());

        try {
            switch (event.getAction()) {
                case START -> {
                    log.info("  🚀 [EventListener] 正在启动房间 {}...", event.getRoomId());
                    roomOrchestrator.startRoom(event.getRoomId());
                    log.info("  ✅ [EventListener] 房间 {} 启动命令已发送", event.getRoomId());
                }
                case PAUSE -> {
                    log.info("  ⏸️  [EventListener] 正在暂停房间 {}...", event.getRoomId());
                    roomOrchestrator.pauseRoom(event.getRoomId());
                    log.info("  ✅ [EventListener] 房间 {} 已暂停", event.getRoomId());
                }
                case STOP -> {
                    log.info("  🛑 [EventListener] 正在停止房间 {}...", event.getRoomId());
                    roomOrchestrator.stopRoom(event.getRoomId());
                    log.info("  ✅ [EventListener] 房间 {} 已停止", event.getRoomId());
                }
            }
        } catch (Exception e) {
            log.error("  ❌ [EventListener] 处理房间 {} 的 {} 事件失败",
                    event.getRoomId(), event.getAction(), e);
        }
    }

}
