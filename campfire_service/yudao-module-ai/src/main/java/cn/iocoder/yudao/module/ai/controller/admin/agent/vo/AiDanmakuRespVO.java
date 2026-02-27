package cn.iocoder.yudao.module.ai.controller.admin.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 弹幕 Response VO")
@Data
public class AiDanmakuRespVO {

    @Schema(description = "弹幕ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "房间ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Long roomId;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long userId;

    @Schema(description = "钱包地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "0x123...abc")
    private String walletAddress;

    @Schema(description = "弹幕内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "Alpha")
    private String content;

    @Schema(description = "颜色", example = "#FF0000")
    private String color;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
