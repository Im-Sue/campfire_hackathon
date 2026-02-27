package cn.iocoder.yudao.module.market.enums.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * API 日志状态枚举
 */
@Getter
@AllArgsConstructor
public enum ApiLogStatusEnum {

    SUCCESS("SUCCESS", "成功"),
    FAIL("FAIL", "失败"),
    TIMEOUT("TIMEOUT", "超时");

    private final String code;
    private final String description;

}
