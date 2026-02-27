package cn.iocoder.yudao.module.market.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单方向枚举
 */
@Getter
@AllArgsConstructor
public enum OrderSideEnum {

    BUY(1, "买入"),
    SELL(2, "卖出");

    private final Integer side;
    private final String name;

}
