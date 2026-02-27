package cn.iocoder.yudao.module.ai.service.agent.dto;

import lombok.Data;

/**
 * Agent参与者信息
 * 用于在Controller和Service之间传递Agent参与房间的信息
 *
 * @author campfire
 */
@Data
public class AgentParticipantInfo {

    /**
     * Agent ID
     */
    private Long agentId;

    /**
     * 初始余额(积分)
     */
    private Long initialBalance;

}
