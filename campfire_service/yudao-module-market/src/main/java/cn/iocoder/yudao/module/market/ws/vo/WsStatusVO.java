package cn.iocoder.yudao.module.market.ws.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * WebSocket 状态 VO
 */
@Data
public class WsStatusVO {

    /**
     * 连接状态
     */
    private String status;

    /**
     * 最后心跳时间
     */
    private LocalDateTime lastHeartbeatTime;

    /**
     * 最后收到消息时间
     */
    private LocalDateTime lastMessageTime;

    /**
     * 重连次数
     */
    private Integer reconnectCount;

    /**
     * 已订阅的 Token 数量
     */
    private Integer subscribedCount;

    /**
     * 连接时长（秒）
     */
    private Long connectedDurationSec;

}
