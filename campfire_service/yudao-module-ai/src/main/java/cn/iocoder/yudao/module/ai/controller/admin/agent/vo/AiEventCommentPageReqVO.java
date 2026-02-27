package cn.iocoder.yudao.module.ai.controller.admin.agent.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * 管理后台 - Agent 事件评论分页 Request VO
 */
@Schema(description = "管理后台 - Agent 事件评论分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class AiEventCommentPageReqVO extends PageParam {

    @Schema(description = "事件 ID")
    private Long eventId;

    @Schema(description = "Agent ID")
    private Long agentId;

    @Schema(description = "状态")
    private Integer status;
}
