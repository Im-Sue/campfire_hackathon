package cn.iocoder.yudao.module.ai.dal.dataobject.agent;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * AI Agent 余额快照 DO
 *
 * @author campfire
 */
@TableName("ai_agent_balance_snapshot")
@KeySequence("ai_agent_balance_snapshot_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAgentBalanceSnapshotDO {

    /**
     * 快照ID
     */
    @TableId
    private Long id;

    /**
     * Agent ID
     */
    private Long agentId;

    /**
     * 快照时余额
     */
    private Long balance;

    /**
     * 触发类型 1定时 2事件开始 3事件结束
     */
    private Integer triggerType;

    /**
     * 关联的事件/房间ID
     */
    private Long triggerId;

    /**
     * 快照时间
     */
    private LocalDateTime snapshotTime;

}
