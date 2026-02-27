package cn.iocoder.yudao.module.ai.enums.agent;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI Agent 事件评论状态枚举
 *
 * @author campfire
 */
@Getter
@AllArgsConstructor
public enum AiEventCommentStatusEnum {

    GENERATING(0, "生成中"),
    NORMAL(1, "正常"),
    DELETED(2, "已删除"),
    FAILED(3, "生成失败");

    /**
     * 状态值
     */
    private final Integer status;

    /**
     * 状态名
     */
    private final String name;
}
