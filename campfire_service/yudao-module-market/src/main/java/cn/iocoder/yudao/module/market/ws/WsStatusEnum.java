package cn.iocoder.yudao.module.market.ws;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * WebSocket 连接状态枚举
 */
@Getter
@AllArgsConstructor
public enum WsStatusEnum {

    INIT("初始化"),
    CONNECTING("连接中"),
    CONNECTED("已连接"),
    DISCONNECTED("已断开"),
    STALE("连接异常"),
    RECONNECTING("重连中");

    private final String name;

}
