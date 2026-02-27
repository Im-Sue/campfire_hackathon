package cn.iocoder.yudao.module.social.controller.app.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import jakarta.validation.constraints.NotNull;

@Schema(description = "用户 App - 回复评论分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppReplyPageReqVO extends PageParam {

    @Schema(description = "父评论 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "父评论 ID 不能为空")
    private Long commentId;

}
