package cn.iocoder.yudao.module.point.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户 APP - 积分流水 Response VO")
@Data
public class AppPointTransactionRespVO {

    @Schema(description = "流水ID", required = true, example = "1")
    private Long id;

    @Schema(description = "类型", required = true, example = "1")
    private Integer type;

    @Schema(description = "类型名称", required = true, example = "任务奖励")
    private String typeName;

    @Schema(description = "变动金额", required = true, example = "100")
    private Long amount;

    @Schema(description = "变动后余额", required = true, example = "1000")
    private Long afterBalance;

    @Schema(description = "业务类型", required = true, example = "TASK")
    private String bizType;

    @Schema(description = "备注", example = "完成每日签到")
    private String remark;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

}
