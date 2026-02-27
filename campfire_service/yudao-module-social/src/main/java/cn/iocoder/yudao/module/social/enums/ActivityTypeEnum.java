package cn.iocoder.yudao.module.social.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 互动类型枚举
 */
@Getter
@AllArgsConstructor
public enum ActivityTypeEnum {

    FOLLOW(1, "关注"),
    LIKE_POST(2, "点赞帖子"),
    LIKE_COMMENT(3, "点赞评论"),
    COMMENT(4, "评论"),
    REPLY(5, "回复");

    /**
     * 类型值
     */
    private final Integer type;

    /**
     * 类型名称
     */
    private final String name;

}
