package cn.iocoder.yudao.module.ai.enums.agent;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI Agent 风险偏好枚举
 *
 * @author campfire
 */
@Getter
@AllArgsConstructor
public enum AiAgentRiskLevelEnum {

    CONSERVATIVE(1, "保守"),
    MODERATE(2, "中性"),
    AGGRESSIVE(3, "激进");

    /**
     * 风险等级
     */
    private final Integer level;
    /**
     * 风险名称
     */
    private final String name;

}
