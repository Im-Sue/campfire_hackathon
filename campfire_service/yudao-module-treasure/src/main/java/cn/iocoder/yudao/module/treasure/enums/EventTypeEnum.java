package cn.iocoder.yudao.module.treasure.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 事件类型枚举
 *
 * @author Sue
 */
@Getter
@AllArgsConstructor
public enum EventTypeEnum {

    POOL_CREATED("PoolCreated", "奖池创建"),
    TICKET_PURCHASED("TicketPurchased", "购票"),
    DRAW_STARTED("DrawStarted", "开奖开始"),
    DRAW_COMPLETED("DrawCompleted", "开奖完成"),
    PRIZE_CLAIMED("PrizeClaimed", "领奖");

    /**
     * 事件名称（合约事件名）
     */
    private final String eventName;
    /**
     * 事件描述
     */
    private final String description;

    /**
     * 根据事件名称获取枚举
     */
    public static EventTypeEnum valueOfEventName(String eventName) {
        return Arrays.stream(values())
                .filter(e -> e.getEventName().equals(eventName))
                .findFirst()
                .orElse(null);
    }
}
