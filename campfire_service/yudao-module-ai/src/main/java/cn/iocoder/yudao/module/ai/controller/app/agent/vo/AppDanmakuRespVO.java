package cn.iocoder.yudao.module.ai.controller.app.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户 App - 弹幕 Response VO")
@Data
@Builder
public class AppDanmakuRespVO {

    @Schema(description = "弹幕ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "弹幕内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "Alpha说得太对了！")
    private String content;

    @Schema(description = "弹幕颜色", example = "#FF6B6B")
    private String color;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
