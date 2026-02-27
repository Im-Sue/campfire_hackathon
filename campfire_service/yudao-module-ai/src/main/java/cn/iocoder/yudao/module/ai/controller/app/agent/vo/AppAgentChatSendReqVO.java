package cn.iocoder.yudao.module.ai.controller.app.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "C端 - Agent对话发送消息 Request VO")
@Data
public class AppAgentChatSendReqVO {

    @Schema(description = "Agent编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "Agent编号不能为空")
    private Long agentId;

    @Schema(description = "会话编号（不传则自动获取或创建）", example = "100")
    private Long sessionId;

    @Schema(description = "消息内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "你怎么看BTC市场？")
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 500, message = "消息内容最长500字")
    private String content;

    @Schema(description = "关联事件ID（可选，提供上下文）", example = "101")
    private Long eventId;

    @Schema(description = "关联市场ID（可选，提供上下文）", example = "201")
    private Long marketId;

    @Schema(description = "是否强制新建会话", example = "false")
    private Boolean forceNew;

}
