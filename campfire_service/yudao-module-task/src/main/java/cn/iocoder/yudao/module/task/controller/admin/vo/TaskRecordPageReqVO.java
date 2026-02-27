package cn.iocoder.yudao.module.task.controller.admin.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Schema(description = "管理后台 - 任务记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskRecordPageReqVO extends PageParam {

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "任务类型", example = "SIGN_IN")
    private String taskType;

    @Schema(description = "奖励状态：0待领取 1已领取", example = "0")
    private Integer rewardStatus;

    @Schema(description = "完成日期开始")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate completeDateStart;

    @Schema(description = "完成日期结束")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate completeDateEnd;

}
