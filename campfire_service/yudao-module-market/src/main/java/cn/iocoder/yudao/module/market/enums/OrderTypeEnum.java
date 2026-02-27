package cn.iocoder.yudao.module.market.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单类型枚举
 */
@Getter
@AllArgsConstructor
public enum OrderTypeEnum {

    MARKET(1, "市价单"),
    LIMIT(2, "限价单");

    private final Integer type;
    private final String name;

}
