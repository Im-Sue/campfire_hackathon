package cn.iocoder.yudao.module.ai.service.agent.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 房间生命周期事件
 * 用于解耦 Service 层和 Orchestrator 层的直接依赖
 *
 * @author campfire
 */
@Getter
public class RoomLifecycleEvent extends ApplicationEvent {

    /**
     * 房间ID
     */
    private final Long roomId;

    /**
     * 房间操作类型
     */
    private final RoomAction action;

    /**
     * 房间操作类型枚举
     */
    public enum RoomAction {
        /**
         * 启动房间
         */
        START,

        /**
         * 暂停房间
         */
        PAUSE,

        /**
         * 停止房间
         */
        STOP
    }

    public RoomLifecycleEvent(Object source, Long roomId, RoomAction action) {
        super(source);
        this.roomId = roomId;
        this.action = action;
    }
}
