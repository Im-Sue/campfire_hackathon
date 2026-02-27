package cn.iocoder.yudao.module.market.controller.admin.dashboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 数据概览 - 趋势数据 Response VO (通用)
 */
@Schema(description = "管理后台 - 数据概览趋势数据 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardTrendRespVO {

    @Schema(description = "时间维度: day/week/month/year", example = "month")
    private String dimension;

    @Schema(description = "数据点列表")
    private List<TrendItem> items;

    @Schema(description = "趋势数据点")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendItem {
        @Schema(description = "时间标签", example = "01-06")
        private String time;

        @Schema(description = "数值1", example = "128")
        private Long value1;

        @Schema(description = "数值2", example = "12580")
        private Long value2;

        @Schema(description = "数值3 (可选)", example = "1000")
        private Long value3;
    }
}
