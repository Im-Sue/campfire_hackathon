package cn.iocoder.yudao.module.social.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 帖子状态枚举
 */
@Getter
@AllArgsConstructor
public enum PostStatusEnum {

    NORMAL(0, "正常"),
    PENDING(1, "待审核"),
    DELETED(2, "已删除"),
    REJECTED(3, "审核拒绝");

    /**
     * 状态值
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

}

