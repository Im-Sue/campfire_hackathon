package cn.iocoder.yudao.module.social.controller.admin.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 话题分页查询请求 VO
 */
@Schema(description = "管理后台 - 话题分页请求")
@Data
@EqualsAndHashCode(callSuper = true)
public class TopicPageReqVO extends PageParam {

    @Schema(description = "话题名称（模糊匹配）", example = "#预测")
    private String name;

    @Schema(description = "状态：0正常 1禁用", example = "0")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
