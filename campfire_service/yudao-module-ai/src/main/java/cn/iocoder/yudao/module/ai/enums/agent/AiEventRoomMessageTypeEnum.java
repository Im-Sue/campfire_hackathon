package cn.iocoder.yudao.module.ai.enums.agent;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI 房间消息类型枚举
 *
 * @author campfire
 */
@Getter
@AllArgsConstructor
public enum AiEventRoomMessageTypeEnum {

    DISCUSSION(1, "讨论"),
    DECISION(2, "决策"),
    CLOSING(3, "封盘"),
    SETTLEMENT(4, "结算");

    /**
     * 类型值
     */
    private final Integer type;
    /**
     * 类型名
     */
    private final String name;

}
