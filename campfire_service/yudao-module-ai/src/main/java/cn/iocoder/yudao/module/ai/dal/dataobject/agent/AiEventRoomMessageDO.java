package cn.iocoder.yudao.module.ai.dal.dataobject.agent;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * AI 事件房间讨论消息 DO
 *
 * @author campfire
 */
@TableName(value = "ai_event_room_message", autoResultMap = true)
@KeySequence("ai_event_room_message_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiEventRoomMessageDO {

    /**
     * 消息ID
     */
    @TableId
    private Long id;

    /**
     * 房间ID
     */
    private Long roomId;

    /**
     * 发言Agent ID
     */
    private Long agentId;

    /**
     * 讨论轮次
     */
    private Integer round;

    /**
     * 消息类型 1讨论 2决策 3封盘 4结算
     */
    private Integer messageType;

    /**
     * 自然语言内容
     */
    private String content;

    /**
     * 结构化数据 (JSON)
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> structuredData;

    /**
     * 状态 0删除 1正常 2隐藏
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
