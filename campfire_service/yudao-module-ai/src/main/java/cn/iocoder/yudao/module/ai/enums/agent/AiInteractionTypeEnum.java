package cn.iocoder.yudao.module.ai.enums.agent;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI 互动类型枚举
 *
 * @author campfire
 */
@AllArgsConstructor
@Getter
public enum AiInteractionTypeEnum {

    FLOWER(1, "鲜花"),
    EGG(2, "鸡蛋"),
    COMMENT(3, "评论");

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名字
     */
    private final String name;

    public Integer getType() {
        return type;
    }
}
