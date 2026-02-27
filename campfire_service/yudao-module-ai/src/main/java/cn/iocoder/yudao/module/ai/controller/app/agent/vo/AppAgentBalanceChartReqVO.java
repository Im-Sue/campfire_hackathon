package cn.iocoder.yudao.module.ai.controller.app.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI Agent 余额图表查询参数
 *
 * @author campfire
 */
@Schema(description = "用户端 - AI Agent 余额图表查询参数")
@Data
public class AppAgentBalanceChartReqVO {

    @Schema(description = "Agent ID 列表", example = "[1,2,3]")
    private List<Long> agentIds;  // 可选，不传则返回所有启用的 Agent

    @Schema(description = "开始时间", example = "2024-01-15 00:00:00")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;  // 可选，默认 7 天前

    @Schema(description = "结束时间", example = "2024-01-22 00:00:00")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;  // 可选，默认当前时间

}
