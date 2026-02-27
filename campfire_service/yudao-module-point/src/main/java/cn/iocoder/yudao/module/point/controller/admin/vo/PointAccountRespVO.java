package cn.iocoder.yudao.module.point.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 积分账户 Response VO")
@Data
public class PointAccountRespVO {

    @Schema(description = "用户ID", required = true, example = "1")
    private Long userId;

    @Schema(description = "可用积分", required = true, example = "1000")
    private Long availablePoints;

    @Schema(description = "累计获得", required = true, example = "2000")
    private Long totalEarned;

    @Schema(description = "累计消费", required = true, example = "1000")
    private Long totalSpent;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

}
