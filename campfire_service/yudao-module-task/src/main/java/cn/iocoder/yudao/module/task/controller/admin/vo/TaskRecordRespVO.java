package cn.iocoder.yudao.module.task.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 任务记录 Response VO
 */
@Schema(description = "管理后台 - 任务记录 Response VO")
@Data
public class TaskRecordRespVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "用户钱包地址", example = "0x1234...")
    private String userAddress;

    @Schema(description = "任务类型", example = "SIGN_IN")
    private String taskType;

    @Schema(description = "完成日期", example = "2025-12-24")
    private LocalDate completeDate;

    @Schema(description = "完成次数", example = "3")
    private Integer completeCount;

    @Schema(description = "奖励积分", example = "100")
    private Long rewardPoints;

    @Schema(description = "奖励状态", example = "1")
    private Integer rewardStatus;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
