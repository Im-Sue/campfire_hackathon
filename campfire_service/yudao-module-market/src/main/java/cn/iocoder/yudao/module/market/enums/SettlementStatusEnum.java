package cn.iocoder.yudao.module.market.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 结算状态枚举
 */
@Getter
@AllArgsConstructor
public enum SettlementStatusEnum {

    PENDING(0, "待确认"),
    CONFIRMED(1, "已确认"),
    COMPLETED(2, "已完成");

    private final Integer status;
    private final String name;

}
