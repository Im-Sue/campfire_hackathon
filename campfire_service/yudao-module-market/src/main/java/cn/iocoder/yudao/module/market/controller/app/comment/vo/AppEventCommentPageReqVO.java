package cn.iocoder.yudao.module.market.controller.app.comment.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotNull;

@Schema(description = "用户 App - 事件评论分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class AppEventCommentPageReqVO extends PageParam {

    @Schema(description = "事件 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "事件 ID 不能为空")
    private Long eventId;

    @Schema(description = "排序方式: hot-热度(默认) / time-时间", example = "hot")
    private String orderBy;

}
