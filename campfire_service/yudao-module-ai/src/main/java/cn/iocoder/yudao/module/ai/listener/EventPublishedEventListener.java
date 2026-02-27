package cn.iocoder.yudao.module.ai.listener;

import cn.iocoder.yudao.module.ai.service.agent.AiEventCommentService;
import cn.iocoder.yudao.module.market.event.EventPublishedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 事件上架事件监听器
 * 监听事件上架操作，触发 Agent 评论生成
 *
 * @author campfire
 */
@Slf4j
@Component
public class EventPublishedEventListener {

    @Resource
    private AiEventCommentService aiEventCommentService;

    /**
     * 处理事件上架事件
     * 异步生成 Agent 评论
     *
     * @param event 事件上架事件
     */
    @Async
    @EventListener
    public void handleEventPublished(EventPublishedEvent event) {
        log.info("[EventPublishedEventListener] 收到事件上架事件: eventId={}", event.getEventId());
        try {
            aiEventCommentService.generateCommentForEvent(event.getEventId());
            log.info("[EventPublishedEventListener] Agent 评论生成流程完成: eventId={}", event.getEventId());
        } catch (Exception e) {
            log.error("[EventPublishedEventListener] Agent 评论生成失败: eventId={}", event.getEventId(), e);
        }
    }
}
