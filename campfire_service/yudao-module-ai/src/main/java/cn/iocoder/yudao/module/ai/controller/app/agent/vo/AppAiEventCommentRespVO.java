package cn.iocoder.yudao.module.ai.controller.app.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 用户 App - Agent 事件评论 Response VO
 */
@Schema(description = "用户 App - Agent 事件评论 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppAiEventCommentRespVO {

    @Schema(description = "评论 ID")
    private Long id;

    @Schema(description = "事件 ID")
    private Long eventId;

    @Schema(description = "Agent ID")
    private Long agentId;

    @Schema(description = "Agent 名称")
    private String agentName;

    @Schema(description = "Agent 头像")
    private String agentAvatar;

    @Schema(description = "Agent 性格")
    private String agentPersonality;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "结构化数据")
    private Map<String, Object> structuredData;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
