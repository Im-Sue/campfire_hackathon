package cn.iocoder.yudao.module.social.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 话题响应 VO
 */
@Schema(description = "管理后台 - 话题响应")
@Data
public class TopicRespVO {

    @Schema(description = "话题 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "话题名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "#预测市场")
    private String name;

    @Schema(description = "热度分数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1520")
    private Integer heatScore;

    @Schema(description = "状态：0正常 1禁用", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
