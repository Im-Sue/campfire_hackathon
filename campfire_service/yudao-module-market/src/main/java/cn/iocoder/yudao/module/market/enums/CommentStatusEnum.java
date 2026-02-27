package cn.iocoder.yudao.module.market.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 评论状态枚举
 */
@Getter
@AllArgsConstructor
public enum CommentStatusEnum {

    NORMAL(0, "正常"),
    PENDING(1, "待审核"),
    DELETED(2, "已删除"),
    REJECTED(3, "审核拒绝");

    private final Integer status;
    private final String name;

}
