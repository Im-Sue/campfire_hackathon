package cn.iocoder.yudao.module.market.controller.admin.settlement.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 结算分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class SettlementPageReqVO extends PageParam {

    @Schema(description = "市场 ID", example = "1")
    private Long marketId;

    @Schema(description = "状态: 0-待确认 1-已确认 2-已完成", example = "2")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
