package cn.iocoder.yudao.module.market.controller.app.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "用户 App - 创建订单 Request VO")
@Data
public class AppOrderCreateReqVO {

    @Schema(description = "市场编号", required = true, example = "1024")
    @NotNull(message = "市场编号不能为空")
    private Long marketId;

    @Schema(description = "订单类型：1-市价单 2-限价单", required = true, example = "1")
    @NotNull(message = "订单类型不能为空")
    private Integer orderType;

    @Schema(description = "方向：1-买入 2-卖出", required = true, example = "1")
    @NotNull(message = "方向不能为空")
    private Integer side;

    @Schema(description = "选项：Yes/No/选项名", required = true, example = "Yes")
    @NotNull(message = "选项不能为空")
    private String outcome;

    @Schema(description = "份数（限价单必填）", example = "100.5")
    @DecimalMin(value = "0.000001", message = "份数必须大于0")
    private BigDecimal quantity;

    @Schema(description = "花费积分（市价单必填）", example = "1000")
    private Long amount;

    @Schema(description = "价格（限价单必填）", example = "0.50")
    private BigDecimal price;

    @Schema(description = "滑点容忍度（市价单可选，默认 5%）", example = "0.05")
    @DecimalMin(value = "0", message = "滑点不能为负数")
    @DecimalMax(value = "1", message = "滑点不能超过 100%")
    private BigDecimal slippageTolerance;

}
