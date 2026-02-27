package cn.iocoder.yudao.module.task.mq.consumer;

import cn.iocoder.yudao.framework.mq.redis.core.stream.AbstractRedisStreamMessageListener;
import cn.iocoder.yudao.framework.mq.biz.message.TaskTriggerMessage;
import cn.iocoder.yudao.module.task.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 任务触发消息监听器
 *
 * 监听业务模块发送的任务触发消息，处理任务完成逻辑
 */
@Component
@Slf4j
public class TaskTriggerMessageListener extends AbstractRedisStreamMessageListener<TaskTriggerMessage> {

    @Resource
    private TaskService taskService;

    @Override
    public void onMessage(TaskTriggerMessage message) {
        log.info("[onMessage] 收到任务触发消息: {}", message);
        try {
            taskService.processTaskTrigger(message);
        } catch (Exception e) {
            log.error("[onMessage] 处理任务触发消息失败: message={}", message, e);
            // 不抛出异常，避免消息重试导致重复处理
        }
    }

}
