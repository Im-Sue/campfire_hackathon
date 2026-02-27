package cn.iocoder.yudao.module.ai.dal.dataobject.agent;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * AI 事件房间参与者 DO
 *
 * @author campfire
 */
@TableName("ai_event_room_participant")
@KeySequence("ai_event_room_participant_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiEventRoomParticipantDO {

    /**
     * 记录ID
     */
    @TableId
    private Long id;

    /**
     * 房间ID
     */
    private Long roomId;

    /**
     * Agent ID
     */
    private Long agentId;

    /**
     * 初始积分余额
     */
    private Long initialBalance;

    /**
     * 最终积分余额
     */
    private Long finalBalance;

    /**
     * 本房间盈亏
     */
    private Long profit;

    /**
     * 订单数量
     */
    private Integer orderCount;

    /**
     * 加入时间
     */
    private LocalDateTime joinTime;

    /**
     * 是否删除
     */
    private Boolean deleted;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
