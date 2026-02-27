package cn.iocoder.yudao.module.social.service.helper;

import cn.iocoder.yudao.framework.mq.redis.core.RedisMQTemplate;
import cn.iocoder.yudao.framework.mq.biz.message.TaskTriggerMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 任务触发消息发送助手
 *
 * 提供给 Social 模块各 Service 调用，发送任务触发消息到 Task 模块
 */
@Component("socialTaskTriggerHelper")
@Slf4j
public class TaskTriggerHelper {

    @Resource
    private RedisMQTemplate redisMQTemplate;

    /**
     * 发送发帖任务触发
     *
     * @param userId 用户ID
     * @param postId 帖子ID
     */
    public void sendPostTask(Long userId, Long postId) {
        sendTaskTrigger("POST", userId, postId, null);
    }

    /**
     * 发送点赞任务触发
     *
     * @param userId       用户ID
     * @param bizId        被点赞的帖子/评论ID
     * @param targetUserId 被点赞的帖子/评论所属用户ID（排除自己点赞自己）
     */
    public void sendLikeTask(Long userId, Long bizId, Long targetUserId) {
        sendTaskTrigger("LIKE", userId, bizId, targetUserId);
    }

    /**
     * 发送评论任务触发
     *
     * @param userId       用户ID
     * @param commentId    评论ID
     * @param targetUserId 被评论的帖子/评论所属用户ID（排除自己评论自己）
     */
    public void sendCommentTask(Long userId, Long commentId, Long targetUserId) {
        sendTaskTrigger("COMMENT", userId, commentId, targetUserId);
    }

    /**
     * 通用发送任务触发消息
     */
    private void sendTaskTrigger(String taskType, Long userId, Long bizId, Long targetUserId) {
        try {
            TaskTriggerMessage message = new TaskTriggerMessage();
            message.setTaskType(taskType);
            message.setUserId(userId);
            message.setBizId(bizId);
            message.setTargetUserId(targetUserId);

            redisMQTemplate.send(message);
            log.info("[sendTaskTrigger] Sent task trigger: taskType={}, userId={}, bizId={}, targetUserId={}",
                    taskType, userId, bizId, targetUserId);
        } catch (Exception e) {
            // 任务触发消息发送失败不应该影响业务流程
            log.error("[sendTaskTrigger] Failed to send task trigger: taskType={}, userId={}, bizId={}",
                    taskType, userId, bizId, e);
        }
    }

}
