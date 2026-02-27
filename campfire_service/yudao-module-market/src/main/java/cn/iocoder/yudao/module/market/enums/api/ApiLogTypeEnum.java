package cn.iocoder.yudao.module.market.enums.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * API 日志类型枚举
 */
@Getter
@AllArgsConstructor
public enum ApiLogTypeEnum {

    GAMMA("GAMMA", "Gamma API"),
    CLOB("CLOB", "CLOB API");

    private final String code;
    private final String description;

}
