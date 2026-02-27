package cn.iocoder.yudao.module.market.enums.ws;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * WS 日志类型枚举
 */
@Getter
@AllArgsConstructor
public enum WsLogTypeEnum {

    CONNECT("CONNECT", "连接成功"),
    DISCONNECT("DISCONNECT", "断开连接"),
    RECONNECT("RECONNECT", "重连"),
    SUBSCRIBE("SUBSCRIBE", "订阅"),
    UNSUBSCRIBE("UNSUBSCRIBE", "取消订阅"),
    SEND("SEND", "发送消息"),
    RECEIVE("RECEIVE", "收到消息"),
    PRICE_CHANGE("PRICE_CHANGE", "价格变化"),
    TRADE("TRADE", "成交价更新"),
    SETTLEMENT("SETTLEMENT", "市场结算"),
    NEW_MARKET("NEW_MARKET", "新市场创建"),
    ERROR("ERROR", "错误");

    private final String code;
    private final String description;

}
