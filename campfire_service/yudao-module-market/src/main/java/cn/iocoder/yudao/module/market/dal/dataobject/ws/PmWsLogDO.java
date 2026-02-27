package cn.iocoder.yudao.module.market.dal.dataobject.ws;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * WebSocket 日志 DO
 */
@TableName("pm_ws_log")
@Data
@EqualsAndHashCode(callSuper = true)
public class PmWsLogDO extends BaseDO {

    /**
     * 主键 ID
     */
    @TableId
    private Long id;

    /**
     * 日志类型: CONNECT, DISCONNECT, RECONNECT, SUBSCRIBE, UNSUBSCRIBE, SEND, RECEIVE,
     * PRICE_CHANGE, TRADE, SETTLEMENT, ERROR
     */
    private String type;

    /**
     * 关联 ID: Token ID 或 Market ID
     */
    private String refId;

    /**
     * ID 类型: TOKEN_ID, MARKET_ID, NONE
     */
    private String refType;

    /**
     * WS 事件: price_change, last_trade_price, market_resolved 等
     */
    private String event;

    /**
     * 原始消息内容（JSON）
     */
    private String message;

    /**
     * 描述信息
     */
    private String description;

    /**
     * 租户 ID（手动管理，因为异步线程无租户上下文）
     */
    private Long tenantId;

}
