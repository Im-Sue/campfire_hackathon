package cn.iocoder.yudao.module.ai.controller.app.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "C端 - Agent对话消息 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppAgentChatMessageRespVO {

    @Schema(description = "消息编号", example = "1")
    private Long id;

    @Schema(description = "会话编号", example = "100")
    private Long sessionId;

    @Schema(description = "消息类型：user/assistant", example = "assistant")
    private String type;

    @Schema(description = "消息内容", example = "您好，有什么可以帮您的？")
    private String content;

    @Schema(description = "推理过程（可选）")
    private String reasoningContent;

    @Schema(description = "工具调用记录（JSON格式）")
    private String toolCalls;

    @Schema(description = "关联事件ID")
    private Long contextEventId;

    @Schema(description = "关联市场ID")
    private Long contextMarketId;

    @Schema(description = "Token消耗", example = "150")
    private Integer tokensUsed;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
