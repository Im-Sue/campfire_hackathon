package cn.iocoder.yudao.module.social.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 点赞目标类型枚举
 */
@Getter
@AllArgsConstructor
public enum LikeTargetTypeEnum {

    POST(1, "帖子"),
    COMMENT(2, "评论");

    /**
     * 类型值
     */
    private final Integer type;
    /**
     * 类型名
     */
    private final String name;

}

