package cn.iocoder.yudao.module.ai.controller.admin.agent.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - AI Agent 分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AiAgentPageReqVO extends PageParam {

    @Schema(description = "Agent名称", example = "激进派分析师")
    private String name;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "风险偏好", example = "3")
    private Integer riskLevel;

}
