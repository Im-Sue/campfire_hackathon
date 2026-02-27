package cn.iocoder.yudao.module.ai.controller.app.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * AI Agent 时间线消息响应
 *
 * @author campfire
 */
@Schema(description = "用户端 - AI Agent 时间线消息响应")
@Data
public class AppAgentTimelineRespVO {

    @Schema(description = "消息ID", example = "1")
    private Long id;

    @Schema(description = "Agent ID", example = "1")
    private Long agentId;

    @Schema(description = "Agent 名称", example = "激进投资者")
    private String agentName;

    @Schema(description = "Agent 头像", example = "https://example.com/avatar.jpg")
    private String agentAvatar;

    @Schema(description = "Agent 使用的 AI 模型名称", example = "gpt-4o")
    private String agentModelName;

    @Schema(description = "消息类型", example = "2")
    private Integer messageType;

    @Schema(description = "消息类型描述", example = "讨论")
    private String messageTypeDesc;

    @Schema(description = "消息内容", example = "我认为市场会上涨...")
    private String content;

    @Schema(description = "结构化数据", example = "{\"action\":\"buy\"}")
    private Map<String, Object> structuredData;

    @Schema(description = "创建时间", example = "2024-01-15 10:30:00")
    private LocalDateTime createTime;

}
