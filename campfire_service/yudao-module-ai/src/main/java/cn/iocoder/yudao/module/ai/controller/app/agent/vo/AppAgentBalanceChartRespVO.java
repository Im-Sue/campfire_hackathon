package cn.iocoder.yudao.module.ai.controller.app.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AI Agent 余额图表响应
 *
 * @author campfire
 */
@Schema(description = "用户端 - AI Agent 余额图表响应")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppAgentBalanceChartRespVO {

    @Schema(description = "Agent 列表")
    private List<AgentChartItem> agents;

    @Schema(description = "时间范围")
    private TimeRange timeRange;

    /**
     * Agent 图表项
     */
    @Schema(description = "Agent 图表项")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgentChartItem {

        @Schema(description = "Agent ID", example = "1")
        private Long agentId;

        @Schema(description = "Agent 名称", example = "激进投资者")
        private String agentName;

        @Schema(description = "Agent 头像", example = "https://...")
        private String agentAvatar;

        @Schema(description = "当前余额", example = "10500")
        private Long currentBalance;

        @Schema(description = "初始余额", example = "10000")
        private Long initialBalance;

        @Schema(description = "变动金额", example = "500")
        private Long changeAmount;

        @Schema(description = "变动百分比", example = "5.0")
        private BigDecimal changePercent;

        @Schema(description = "数据点列表")
        private List<DataPoint> data;
    }

    /**
     * 数据点
     */
    @Schema(description = "数据点")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataPoint {

        @Schema(description = "时间", example = "2024-01-15 10:00:00")
        private LocalDateTime time;

        @Schema(description = "余额", example = "10000")
        private Long balance;

        @Schema(description = "相对上一个点的变动", example = "500")
        private Long change;
    }

    /**
     * 时间范围
     */
    @Schema(description = "时间范围")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeRange {

        @Schema(description = "开始时间", example = "2024-01-15 00:00:00")
        private LocalDateTime start;

        @Schema(description = "结束时间", example = "2024-01-22 00:00:00")
        private LocalDateTime end;
    }

}
