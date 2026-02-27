package cn.iocoder.yudao.module.market.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 链上批次状态枚举
 */
@Getter
@AllArgsConstructor
public enum ChainBatchStatusEnum {

    PENDING(0, "待提交"),
    SUBMITTED(1, "已提交"),
    CONFIRMED(2, "已确认"),
    FAILED(3, "失败");

    private final Integer status;
    private final String name;

}
