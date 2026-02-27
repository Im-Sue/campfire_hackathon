package cn.iocoder.yudao.module.point.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 APP - 积分排行榜 Response VO")
@Data
public class AppPointRankRespVO {

    @Schema(description = "排名", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer rank;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long userId;

    @Schema(description = "钱包地址", example = "0x1234...5678")
    private String walletAddress;

    @Schema(description = "用户头像", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "可用积分", requiredMode = Schema.RequiredMode.REQUIRED, example = "10000")
    private Long availablePoints;

    @Schema(description = "累计获得", requiredMode = Schema.RequiredMode.REQUIRED, example = "50000")
    private Long totalEarned;

}
