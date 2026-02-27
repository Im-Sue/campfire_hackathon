package cn.iocoder.yudao.module.ai.dal.dataobject.agent;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * AI 事件房间订单关联 DO
 *
 * @author campfire
 */
@TableName("ai_event_room_order")
@KeySequence("ai_event_room_order_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiEventRoomOrderDO {

    /**
     * 主键
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
     * 订单ID（关联pm_order.id）
     */
    private Long orderId;

    /**
     * 下单轮次
     */
    private Integer round;

    /**
     * 决策动作：buy/sell/hold
     */
    private String decisionAction;

    /**
     * 决策理由
     */
    private String decisionReason;

    /**
     * 订单金额（积分）
     */
    private Long orderAmount;

    /**
     * 下注方向：Yes/No
     */
    private String orderOutcome;

    /**
     * 订单状态（冗余自pm_order）
     */
    private Integer orderStatus;

    /**
     * 租户编号
     */
    private Long tenantId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
