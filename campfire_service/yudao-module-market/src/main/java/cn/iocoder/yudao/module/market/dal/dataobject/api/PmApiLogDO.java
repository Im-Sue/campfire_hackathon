package cn.iocoder.yudao.module.market.dal.dataobject.api;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Polymarket API 调用日志 DO
 */
@TableName("pm_api_log")
@Data
@EqualsAndHashCode(callSuper = true)
public class PmApiLogDO extends BaseDO {

    /**
     * 主键 ID
     */
    @TableId
    private Long id;

    /**
     * API 类型: GAMMA, CLOB
     */
    private String apiType;

    /**
     * 方法名: getEvents, getPrice, etc.
     */
    private String method;

    /**
     * 请求 URL
     */
    private String url;

    /**
     * 请求参数 (JSON)
     */
    private String params;

    /**
     * 状态: SUCCESS, FAIL, TIMEOUT
     */
    private String status;

    /**
     * HTTP 状态码
     */
    private Integer httpCode;

    /**
     * 响应时间 (ms)
     */
    private Long responseTime;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 关联 ID: tokenId, eventId, marketId
     */
    private String refId;

    /**
     * ID 类型: TOKEN_ID, EVENT_ID, MARKET_ID
     */
    private String refType;

    /**
     * 租户 ID（手动管理，因为异步线程无租户上下文）
     */
    private Long tenantId;

}
