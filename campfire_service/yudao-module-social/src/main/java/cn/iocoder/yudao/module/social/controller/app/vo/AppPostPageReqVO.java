package cn.iocoder.yudao.module.social.controller.app.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "用户 App - 帖子分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppPostPageReqVO extends PageParam {

    @Schema(description = "用户 ID (查看某用户的帖子时传入)", example = "1")
    private Long userId;

    @Schema(description = "话题名称 (如 #预测市场)", example = "#预测市场")
    private String topicName;

}
