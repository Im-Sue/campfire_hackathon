package cn.iocoder.yudao.module.ai.controller.admin.agent.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - AI 房间消息分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AiEventRoomMessagePageReqVO extends PageParam {

    @Schema(description = "房间ID", example = "1")
    private Long roomId;

    @Schema(description = "Agent ID", example = "1")
    private Long agentId;

    @Schema(description = "轮次", example = "5")
    private Integer round;

    @Schema(description = "消息类型", example = "1")
    private Integer messageType;

}
