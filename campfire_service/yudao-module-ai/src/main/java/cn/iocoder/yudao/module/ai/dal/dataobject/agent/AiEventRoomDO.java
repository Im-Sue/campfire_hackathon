package cn.iocoder.yudao.module.ai.dal.dataobject.agent;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * AI 事件讨论房间 DO
 *
 * @author campfire
 */
@TableName("ai_event_room")
@KeySequence("ai_event_room_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiEventRoomDO extends BaseDO {

    /**
     * 房间ID
     */
    @TableId
    private Long id;

    /**
     * 关联的事件ID
     * 关联 pm_event.id
     */
    private Long eventId;

    /**
     * 状态 0待开始 1运行中 2暂停 3已结束
     */
    private Integer status;

    /**
     * 当前轮次
     */
    private Integer currentRound;

    /**
     * 讨论间隔(分钟)
     */
    private Integer discussionInterval;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 房间配置 (JSON)
     */
    private String config;

}
