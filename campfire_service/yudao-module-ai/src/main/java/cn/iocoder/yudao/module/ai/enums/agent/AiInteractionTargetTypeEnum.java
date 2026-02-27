package cn.iocoder.yudao.module.ai.enums.agent;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI 互动目标类型枚举
 *
 * @author campfire
 */
@AllArgsConstructor
@Getter
public enum AiInteractionTargetTypeEnum {

    ROOM_MESSAGE(1, "房间讨论消息"),
    EVENT_COMMENT(2, "事件上架评论");

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名字
     */
    private final String name;

    public Integer getType() {
        return type;
    }
}
