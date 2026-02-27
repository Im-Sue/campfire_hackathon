package cn.iocoder.yudao.module.market.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单状态枚举
 */
@Getter
@AllArgsConstructor
public enum OrderStatusEnum {

    PENDING(0, "待成交"),
    FILLED(1, "已成交"),
    PARTIAL_FILLED(2, "部分成交"),
    CANCELLED(3, "已取消"),
    EXPIRED(4, "已失效");

    private final Integer status;
    private final String name;

}
