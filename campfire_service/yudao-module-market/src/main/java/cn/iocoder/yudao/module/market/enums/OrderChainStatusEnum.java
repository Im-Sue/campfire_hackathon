package cn.iocoder.yudao.module.market.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单链上状态枚举
 */
@Getter
@AllArgsConstructor
public enum OrderChainStatusEnum {

    PENDING(0, "待上链"),
    SUBMITTING(1, "上链中"),
    CONFIRMED(2, "已上链"),
    FAILED(3, "上链失败");

    private final Integer status;
    private final String name;

}
