package cn.iocoder.yudao.module.point.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 积分业务类型枚举
 */
@Getter
@AllArgsConstructor
public enum PointBizTypeEnum {

    TASK("TASK", "任务系统"),
    ORDER("ORDER", "订单系统"),
    REWARD("REWARD", "结算奖励"),
    MARKET("MARKET", "预测市场"),
    ADMIN("ADMIN", "管理员操作");

    /**
     * 业务类型编码
     */
    private final String code;

    /**
     * 业务类型名称
     */
    private final String name;

    public static PointBizTypeEnum getByCode(String code) {
        for (PointBizTypeEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }

}
