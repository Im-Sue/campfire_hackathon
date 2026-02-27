package cn.iocoder.yudao.module.ai.controller.app.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户 App - 互动评论 Response VO")
@Data
@Builder
public class AppInteractionCommentRespVO {

    @Schema(description = "评论ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "评论内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "分析得很专业")
    private String content;

    @Schema(description = "钱包地址 (脱敏)", requiredMode = Schema.RequiredMode.REQUIRED, example = "0x12...abcd")
    private String walletAddress;

    @Schema(description = "评论人昵称", example = "加密小王子")
    private String nickname;

    @Schema(description = "评论人头像URL", example = "https://xxx.com/avatar1.jpg")
    private String avatar;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
