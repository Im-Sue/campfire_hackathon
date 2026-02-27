package cn.iocoder.yudao.module.ai.controller.admin.agent.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - AI 事件房间分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AiEventRoomPageReqVO extends PageParam {

    @Schema(description = "事件ID", example = "1")
    private Long eventId;

    @Schema(description = "状态", example = "1")
    private Integer status;

}
