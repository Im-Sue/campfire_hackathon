package cn.iocoder.yudao.module.market.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 事件上架事件
 * 当管理员执行上架操作完成后触发，用于通知 AI 模块生成 Agent 评论
 *
 * @author campfire
 */
@Getter
public class EventPublishedEvent extends ApplicationEvent {

    /**
     * 事件ID
     */
    private final Long eventId;

    public EventPublishedEvent(Object source, Long eventId) {
        super(source);
        this.eventId = eventId;
    }
}
