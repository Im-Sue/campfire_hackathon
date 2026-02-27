package cn.iocoder.yudao.module.ai.controller.admin.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - AI 事件房间 Response VO")
@Data
public class AiEventRoomRespVO {

    @Schema(description = "房间ID", example = "1")
    private Long id;

    @Schema(description = "关联的事件ID", example = "1")
    private Long eventId;

    @Schema(description = "事件标题", example = "2024年美国总统大选")
    private String eventTitle;

    @Schema(description = "事件封面URL", example = "https://example.com/cover.jpg")
    private String eventCoverUrl;

    @Schema(description = "状态: 0待开始 1运行中 2暂停 3已结束", example = "1")
    private Integer status;

    @Schema(description = "当前轮次", example = "5")
    private Integer currentRound;

    @Schema(description = "讨论间隔(分钟)", example = "5")
    private Integer discussionInterval;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "下一轮开始时间(毫秒时间戳)", example = "1706428800000")
    private Long nextRoundTime;

    @Schema(description = "参与者列表")
    private List<AiEventRoomParticipantRespVO> participants;

    @Schema(description = "总下单数", example = "25")
    private Integer totalOrders;

    @Schema(description = "总盈亏", example = "5000")
    private Long totalProfit;

    @Schema(description = "总交易额", example = "50000")
    private Long totalAmount;

    @Schema(description = "市场数量", example = "3")
    private Integer marketCount;

}
