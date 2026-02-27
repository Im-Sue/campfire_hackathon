package cn.iocoder.yudao.module.ai.controller.admin.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "管理后台 - AI 事件房间消息 Response VO")
@Data
public class AiEventRoomMessageRespVO {

    @Schema(description = "消息ID", example = "1")
    private Long id;

    @Schema(description = "房间ID", example = "1")
    private Long roomId;

    @Schema(description = "发言Agent ID", example = "1")
    private Long agentId;

    @Schema(description = "Agent名称", example = "激进投资者")
    private String agentName;

    @Schema(description = "Agent头像URL", example = "https://example.com/avatar.jpg")
    private String agentAvatar;

    @Schema(description = "讨论轮次", example = "1")
    private Integer round;

    @Schema(description = "消息类型: 1讨论 2决策 3封盘 4结算", example = "1")
    private Integer messageType;

    @Schema(description = "自然语言内容", example = "我认为这个市场会上涨")
    private String content;

    @Schema(description = "结构化数据 (JSON)")
    private Map<String, Object> structuredData;

    @Schema(description = "状态: 0删除 1正常 2隐藏", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
