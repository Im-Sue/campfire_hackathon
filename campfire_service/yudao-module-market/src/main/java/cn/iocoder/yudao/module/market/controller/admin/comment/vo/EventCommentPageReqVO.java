package cn.iocoder.yudao.module.market.controller.admin.comment.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 事件评论分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class EventCommentPageReqVO extends PageParam {

    @Schema(description = "事件 ID", example = "1")
    private Long eventId;

    @Schema(description = "用户 ID", example = "123")
    private Long userId;

    @Schema(description = "评论状态: 0-正常 1-待审核 2-已删除", example = "0")
    private Integer status;

    @Schema(description = "评论内容（模糊搜索）", example = "有趣")
    private String content;

}
