package cn.iocoder.yudao.module.task.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 奖励状态枚举
 */
@Getter
@AllArgsConstructor
public enum RewardStatusEnum {

    /**
     * 待领取
     */
    PENDING(0, "待领取"),

    /**
     * 已领取
     */
    CLAIMED(1, "已领取");

    private final Integer value;
    private final String name;

}

