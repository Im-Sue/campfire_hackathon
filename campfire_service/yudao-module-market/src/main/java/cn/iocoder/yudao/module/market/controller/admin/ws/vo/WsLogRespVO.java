package cn.iocoder.yudao.module.market.controller.admin.ws.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * WS 日志响应 VO
 */
@Schema(description = "管理后台 - WS 日志 Response VO")
@Data
public class WsLogRespVO {

    @Schema(description = "日志 ID", example = "1")
    private Long id;

    @Schema(description = "日志类型", example = "PRICE_CHANGE")
    private String type;

    @Schema(description = "关联 ID", example = "12345")
    private String refId;

    @Schema(description = "ID 类型", example = "TOKEN_ID")
    private String refType;

    @Schema(description = "WS 事件", example = "price_change")
    private String event;

    @Schema(description = "原始消息内容")
    private String message;

    @Schema(description = "描述信息")
    private String description;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
