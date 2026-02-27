package cn.iocoder.yudao.module.point.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 积分流水类型枚举
 */
@Getter
@AllArgsConstructor
public enum PointTransactionTypeEnum {

    TASK_REWARD(1, "任务奖励"),
    ORDER_CONSUME(2, "下单消费"),
    SELL_INCOME(3, "卖单收入"),
    SETTLEMENT_CLAIM(4, "订单结算领取"),
    ADMIN_ADJUST(5, "管理员调整"),
    MARKET_ORDER(6, "预测市场下单"),
    MARKET_CANCEL(7, "预测市场取消"),
    MARKET_REWARD(8, "预测市场奖励");

    /**
     * 类型值
     */
    private final Integer type;

    /**
     * 类型名称
     */
    private final String name;

    public static PointTransactionTypeEnum getByType(Integer type) {
        for (PointTransactionTypeEnum value : values()) {
            if (value.getType().equals(type)) {
                return value;
            }
        }
        return null;
    }

}
