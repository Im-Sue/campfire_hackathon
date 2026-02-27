package cn.iocoder.yudao.module.social.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - 审核帖子 Request VO")
@Data
public class PostAuditReqVO {

    @Schema(description = "帖子 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "帖子 ID 不能为空")
    private Long id;

    @Schema(description = "是否通过", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "审核结果不能为空")
    private Boolean pass;

    @Schema(description = "拒绝原因", example = "内容违规")
    private String reason;

}
