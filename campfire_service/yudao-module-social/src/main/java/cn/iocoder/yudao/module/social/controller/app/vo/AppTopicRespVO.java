package cn.iocoder.yudao.module.social.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 话题响应")
@Data
public class AppTopicRespVO {

    @Schema(description = "话题ID", required = true)
    private Long id;

    @Schema(description = "话题名称，如 #预测市场", required = true)
    private String name;

    @Schema(description = "热度分数", required = true)
    private Integer heatScore;

}
