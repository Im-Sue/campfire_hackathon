package cn.iocoder.yudao.module.market.controller.admin.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - API 日志统计 Response VO")
@Data
public class ApiLogStatsVO {

    @Schema(description = "总请求数", required = true, example = "10000")
    private Long totalCount;

    @Schema(description = "成功数", required = true, example = "9500")
    private Long successCount;

    @Schema(description = "失败数", required = true, example = "500")
    private Long failCount;

    @Schema(description = "今日请求数", required = true, example = "1000")
    private Long todayCount;

    @Schema(description = "成功率 (%)", required = true, example = "95.00")
    private BigDecimal successRate;

}
