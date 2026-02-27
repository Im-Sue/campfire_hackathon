package cn.iocoder.yudao.module.market.controller.admin.order.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 订单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderPageReqVO extends PageParam {

    @Schema(description = "订单号", example = "202601010001")
    private String orderNo;

    @Schema(description = "用户 ID", example = "1")
    private Long userId;

    @Schema(description = "钱包地址", example = "0x1234...")
    private String walletAddress;

    @Schema(description = "市场 ID", example = "1")
    private Long marketId;

    @Schema(description = "订单类型: 1-市价 2-限价", example = "1")
    private Integer orderType;

    @Schema(description = "方向: 1-买入 2-卖出", example = "1")
    private Integer side;

    @Schema(description = "状态: 0-待成交 1-已成交 2-部分成交 3-已取消 4-已失效", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
