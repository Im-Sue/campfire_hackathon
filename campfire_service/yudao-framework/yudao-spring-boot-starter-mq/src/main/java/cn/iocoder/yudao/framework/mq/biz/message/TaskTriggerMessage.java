package cn.iocoder.yudao.framework.mq.biz.message;

import cn.iocoder.yudao.framework.mq.redis.core.stream.AbstractRedisStreamMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 任务触发消息
 *
 * 由业务模块发送，任务模块消费
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskTriggerMessage extends AbstractRedisStreamMessage {

    /**
     * 任务类型
     *
     * 例如：REGISTER, UPDATE_PROFILE, INVITE_FRIEND, LIKE_POST, COMMENT 等
     */
    private String taskType;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 钱包地址
     */
    private String walletAddress;

    /**
     * 业务ID（帖子ID、订单ID等）
     */
    private Long bizId;

    /**
     * 目标用户ID（用于排除自我互动）
     * 例如：点赞/评论时，目标用户ID = 帖子/评论作者ID
     * 如果 targetUserId == userId，则为自我互动，不计入任务
     */
    private Long targetUserId;

    @Override
    public String getStreamKey() {
        return "task:trigger";
    }

}
