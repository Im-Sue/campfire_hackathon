package cn.iocoder.yudao.module.ai.controller.admin.agent.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 弹幕分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class AiDanmakuPageReqVO extends PageParam {

    @Schema(description = "房间ID", example = "100")
    private Long roomId;

    @Schema(description = "用户ID", example = "1024")
    private Long userId;

    @Schema(description = "弹幕内容", example = "Alpha")
    private String content;

}
