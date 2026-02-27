package cn.iocoder.yudao.module.ai.controller.admin.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 互动 Response VO")
@Data
public class AiInteractionRespVO {

    @Schema(description = "互动ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "目标类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer targetType;

    @Schema(description = "目标ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Long targetId;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long userId;

    @Schema(description = "钱包地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "0x123...abc")
    private String walletAddress;

    @Schema(description = "互动类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Integer interactionType;

    @Schema(description = "内容", example = "好评")
    private String content;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
