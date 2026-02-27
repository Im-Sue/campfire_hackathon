package cn.iocoder.yudao.module.market.controller.admin.ws.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * WS 日志分页查询请求 VO
 */
@Schema(description = "管理后台 - WS 日志分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class WsLogPageReqVO extends PageParam {

    @Schema(description = "日志类型", example = "PRICE_CHANGE")
    private String type;

    @Schema(description = "关联 ID", example = "12345")
    private String refId;

    @Schema(description = "ID 类型: TOKEN_ID, MARKET_ID, NONE", example = "TOKEN_ID")
    private String refType;

    @Schema(description = "WS 事件", example = "price_change")
    private String event;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
