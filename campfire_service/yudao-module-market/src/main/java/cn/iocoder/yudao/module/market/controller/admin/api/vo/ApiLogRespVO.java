package cn.iocoder.yudao.module.market.controller.admin.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - API 日志 Response VO")
@Data
public class ApiLogRespVO {

    @Schema(description = "主键 ID", required = true, example = "1024")
    private Long id;

    @Schema(description = "API 类型", required = true, example = "CLOB")
    private String apiType;

    @Schema(description = "方法名", required = true, example = "getPrice")
    private String method;

    @Schema(description = "请求 URL", example = "/price?token_id=xxx")
    private String url;

    @Schema(description = "请求参数 (JSON)", example = "{\"tokenId\":\"xxx\"}")
    private String params;

    @Schema(description = "状态", required = true, example = "SUCCESS")
    private String status;

    @Schema(description = "HTTP 状态码", example = "200")
    private Integer httpCode;

    @Schema(description = "响应时间 (ms)", example = "150")
    private Long responseTime;

    @Schema(description = "错误信息", example = "Token not found")
    private String errorMessage;

    @Schema(description = "关联 ID", example = "12345")
    private String refId;

    @Schema(description = "关联类型", example = "TOKEN_ID")
    private String refType;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

}
