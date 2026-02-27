package cn.iocoder.yudao.module.market.enums.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * API 日志关联类型枚举
 */
@Getter
@AllArgsConstructor
public enum ApiLogRefTypeEnum {

    TOKEN_ID("TOKEN_ID", "Token ID"),
    EVENT_ID("EVENT_ID", "Event ID"),
    MARKET_ID("MARKET_ID", "Market ID"),
    TAG_SLUG("TAG_SLUG", "Tag Slug"),
    NONE("NONE", "无");

    private final String code;
    private final String description;

}
