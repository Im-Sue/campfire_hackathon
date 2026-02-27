package cn.iocoder.yudao.module.social.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 创建话题请求 VO
 */
@Schema(description = "管理后台 - 创建话题请求")
@Data
public class TopicCreateReqVO {

    @Schema(description = "话题名称（必须以#开头）", requiredMode = Schema.RequiredMode.REQUIRED, example = "#预测市场")
    @NotBlank(message = "话题名称不能为空")
    @Size(min = 2, max = 50, message = "话题名称长度必须在 2-50 之间")
    @Pattern(regexp = "^#.+", message = "话题名称必须以#开头")
    private String name;

}
