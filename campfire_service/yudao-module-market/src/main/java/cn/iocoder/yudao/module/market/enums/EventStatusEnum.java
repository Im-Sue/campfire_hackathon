package cn.iocoder.yudao.module.market.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 事件状态枚举
 */
@Getter
@AllArgsConstructor
public enum EventStatusEnum {

    DRAFT(0, "待上架"),
    PUBLISHED(1, "已上架"),
    UNPUBLISHED(2, "已下架");

    private final Integer status;
    private final String name;

}
