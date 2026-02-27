package cn.iocoder.yudao.module.market.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 奖励状态枚举
 */
@Getter
@AllArgsConstructor
public enum RewardStatusEnum {

    PENDING(0, "待领取"),
    CLAIMED(1, "已领取"),
    FAILED(2, "失败");

    private final Integer status;
    private final String name;

}
