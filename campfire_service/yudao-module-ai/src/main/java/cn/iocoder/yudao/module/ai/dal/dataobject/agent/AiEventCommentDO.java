package cn.iocoder.yudao.module.ai.dal.dataobject.agent;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.util.Map;

/**
 * AI Agent 事件评论 DO
 *
 * @author campfire
 */
@TableName(value = "ai_event_comment", autoResultMap = true)
@KeySequence("ai_event_comment_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiEventCommentDO extends BaseDO {

    /**
     * 评论ID
     */
    @TableId
    private Long id;

    /**
     * 事件ID
     */
    private Long eventId;

    /**
     * Agent ID
     */
    private Long agentId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 结构化数据 JSON
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> structuredData;

    /**
     * 状态 0-生成中 1-正常 2-已删除 3-生成失败
     */
    private Integer status;

    /**
     * 失败原因
     */
    private String errorMessage;
}
