package cn.iocoder.yudao.module.market.controller.app.event.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Schema(description = "用户 App - 事件 Response VO")
@Data
public class AppEventRespVO {

    @Schema(description = "事件编号", required = true, example = "1024")
    private Long id;

    @Schema(description = "Polymarket Event ID", example = "abc123")
    private String polymarketEventId;

    @Schema(description = "事件标题", required = true, example = "Will Trump win 2024?")
    private String title;

    @Schema(description = "封面图", example = "https://example.com/image.jpg")
    private String imageUrl;

    @Schema(description = "主分类", example = "politics")
    private String category;

    @Schema(description = "标签数组")
    private List<Map<String, Object>> tags;

    @Schema(description = "包含的市场数量", example = "3")
    private Integer marketCount;

    @Schema(description = "开始时间")
    private LocalDateTime startDate;

    @Schema(description = "结束时间")
    private LocalDateTime endDate;

    // ========== 体育信息 ==========

    @Schema(description = "体育赛事唯一 ID（非 null 表示体育赛事，不展示 K 线）", example = "12345")
    private Integer gameId;

    @Schema(description = "主队", example = "Lakers")
    private String homeTeamName;

    @Schema(description = "客队", example = "Celtics")
    private String awayTeamName;

    @Schema(description = "比赛日期")
    private LocalDate eventDate;

    @Schema(description = "比赛开始时间（体育赛事，从第一个市场提取）")
    private LocalDateTime gameStartTime;

    // ========== 动态数据（来自缓存） ==========

    @Schema(description = "实时比分", example = "110-105")
    private String score;

    @Schema(description = "总交易量（USD）", example = "1250000.50")
    private String volume;

    // ========== 嵌套 Market 列表 ==========

    @Schema(description = "关联的市场列表")
    private List<AppMarketSimpleVO> markets;

}
