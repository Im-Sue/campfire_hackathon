package cn.iocoder.yudao.module.ai.controller.admin.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - AI 事件房间创建 Request VO")
@Data
public class AiEventRoomCreateReqVO {

    @Schema(description = "事件ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "事件ID不能为空")
    private Long eventId;

    @Schema(description = "参与的Agent ID列表 (不填则使用所有启用的Agent)", example = "[1, 2, 3]")
    private List<Long> agentIds;

    @Schema(description = "讨论间隔(分钟)", example = "5")
    private Integer discussionInterval;

    @Schema(description = "是否立即开始", example = "true")
    private Boolean startImmediately;

}
