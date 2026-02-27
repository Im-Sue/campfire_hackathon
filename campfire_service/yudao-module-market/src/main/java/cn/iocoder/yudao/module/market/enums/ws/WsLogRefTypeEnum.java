package cn.iocoder.yudao.module.market.enums.ws;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * WS 日志关联 ID 类型枚举
 */
@Getter
@AllArgsConstructor
public enum WsLogRefTypeEnum {

    TOKEN_ID("TOKEN_ID", "Token ID"),
    MARKET_ID("MARKET_ID", "Market ID"),
    NONE("NONE", "无");

    private final String code;
    private final String description;

}
