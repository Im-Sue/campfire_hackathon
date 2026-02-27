package cn.iocoder.yudao.module.ai.controller.admin.agent.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 互动分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class AiInteractionPageReqVO extends PageParam {

    @Schema(description = "目标类型", example = "1")
    private Integer targetType;

    @Schema(description = "目标ID", example = "100")
    private Long targetId;

    @Schema(description = "互动类型", example = "3")
    private Integer interactionType;

    @Schema(description = "用户ID", example = "1024")
    private Long userId;

}
