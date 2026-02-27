package cn.iocoder.yudao.module.point.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "管理后台 - 积分流水 Response VO")
@Data
public class PointTransactionRespVO {

    @Schema(description = "流水ID", required = true, example = "1")
    private Long id;

    @Schema(description = "用户ID", required = true, example = "1")
    private Long userId;

    @Schema(description = "类型", required = true, example = "1")
    private Integer type;

    @Schema(description = "类型名称", required = true, example = "任务奖励")
    private String typeName;

    @Schema(description = "变动金额", required = true, example = "100")
    private Long amount;

    @Schema(description = "变动前余额", required = true, example = "900")
    private Long beforeBalance;

    @Schema(description = "变动后余额", required = true, example = "1000")
    private Long afterBalance;

    @Schema(description = "业务类型", required = true, example = "TASK")
    private String bizType;

    @Schema(description = "业务ID", example = "1")
    private Long bizId;

    @Schema(description = "扩展信息")
    private Map<String, Object> extension;

    @Schema(description = "备注", example = "完成每日签到")
    private String remark;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

}
