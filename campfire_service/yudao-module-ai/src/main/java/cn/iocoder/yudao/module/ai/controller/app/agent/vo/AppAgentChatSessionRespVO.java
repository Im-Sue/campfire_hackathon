package cn.iocoder.yudao.module.ai.controller.app.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * C端 Agent 对话会话响应 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "C端 - Agent对话会话响应")
public class AppAgentChatSessionRespVO {

    @Schema(description = "会话编号", example = "123")
    private Long sessionId;

    @Schema(description = "Agent编号", example = "1")
    private Long agentId;

    @Schema(description = "Agent名称", example = "Alpha")
    private String agentName;

    @Schema(description = "会话标题", example = "你怎么看BTC市场？")
    private String title;

    @Schema(description = "消息数量", example = "5")
    private Integer messageCount;

    @Schema(description = "最后消息时间")
    private LocalDateTime lastMessageTime;

}
