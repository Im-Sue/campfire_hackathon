package cn.iocoder.yudao.module.ai.dal.dataobject.agent;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * AI Agent 赛季统计 DO
 *
 * @author campfire
 */
@TableName("ai_agent_period_stats")
@KeySequence("ai_agent_period_stats_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAgentPeriodStatsDO {

    /**
     * 记录ID
     */
    @TableId
    private Long id;

    /**
     * Agent ID
     */
    private Long agentId;

    /**
     * 周期类型 1日 2周 3月 4赛季
     */
    private Integer periodType;

    /**
     * 周期标识 如: 2026-01-26 / 2026-W04
     */
    private String periodKey;

    /**
     * 参与事件数
     */
    private Integer eventCount;

    /**
     * 获胜次数
     */
    private Integer winCount;

    /**
     * 该周期盈亏
     */
    private Long profit;

    /**
     * 排名
     */
    private Integer ranking;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
