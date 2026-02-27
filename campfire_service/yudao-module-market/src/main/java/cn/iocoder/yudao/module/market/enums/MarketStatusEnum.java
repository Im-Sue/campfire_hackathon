package cn.iocoder.yudao.module.market.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 市场状态枚举
 */
@Getter
@AllArgsConstructor
public enum MarketStatusEnum {

    DRAFT(0, "待上架"),
    TRADING(1, "交易中"),
    SUSPENDED(2, "已封盘"),
    PENDING_SETTLEMENT(3, "待结算"),
    SETTLED(4, "已结算");

    private final Integer status;
    private final String name;

}
