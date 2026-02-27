package cn.iocoder.yudao.module.point.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 APP - 积分账户 Response VO")
@Data
public class AppPointAccountRespVO {

    @Schema(description = "可用积分", required = true, example = "1000")
    private Long availablePoints;

    @Schema(description = "累计获得", required = true, example = "2000")
    private Long totalEarned;

    @Schema(description = "累计消费", required = true, example = "1000")
    private Long totalSpent;

}
