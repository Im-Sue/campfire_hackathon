package cn.iocoder.yudao.module.market.controller.admin.api.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - API 日志分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ApiLogPageReqVO extends PageParam {

    @Schema(description = "API 类型: GAMMA, CLOB", example = "CLOB")
    private String apiType;

    @Schema(description = "方法名", example = "getPrice")
    private String method;

    @Schema(description = "状态: SUCCESS, FAIL, TIMEOUT", example = "SUCCESS")
    private String status;

    @Schema(description = "关联 ID", example = "12345")
    private String refId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
