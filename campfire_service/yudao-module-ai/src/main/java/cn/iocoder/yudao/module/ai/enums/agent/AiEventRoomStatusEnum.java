package cn.iocoder.yudao.module.ai.enums.agent;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI 事件房间状态枚举
 *
 * @author campfire
 */
@Getter
@AllArgsConstructor
public enum AiEventRoomStatusEnum {

    PENDING(0, "待开始"),
    RUNNING(1, "运行中"),
    PAUSED(2, "暂停"),
    FINISHED(3, "已结束");

    /**
     * 状态值
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

}
