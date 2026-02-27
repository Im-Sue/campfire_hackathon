package cn.iocoder.yudao.module.market.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 订单 Response VO")
@Data
public class OrderRespVO {

    @Schema(description = "订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "订单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "202601010001")
    private String orderNo;

    @Schema(description = "用户 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long userId;

    @Schema(description = "钱包地址", example = "0x1234...")
    private String walletAddress;

    @Schema(description = "市场 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long marketId;

    @Schema(description = "订单类型: 1-市价 2-限价", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer orderType;

    @Schema(description = "方向: 1-买入 2-卖出", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer side;

    @Schema(description = "选项", requiredMode = Schema.RequiredMode.REQUIRED, example = "Yes")
    private String outcome;

    @Schema(description = "价格", requiredMode = Schema.RequiredMode.REQUIRED, example = "0.65")
    private BigDecimal price;

    @Schema(description = "份数", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.5")
    private BigDecimal quantity;

    @Schema(description = "金额(积分)", requiredMode = Schema.RequiredMode.REQUIRED, example = "6500")
    private Long amount;

    @Schema(description = "滑点容忍度", example = "0.05")
    private BigDecimal slippageTolerance;

    @Schema(description = "已成交份数", example = "100.5")
    private BigDecimal filledQuantity;

    @Schema(description = "已成交金额(积分)", example = "6500")
    private Long filledAmount;

    @Schema(description = "成交价格", example = "0.65")
    private BigDecimal filledPrice;

    @Schema(description = "成交时间")
    private LocalDateTime filledAt;

    @Schema(description = "状态: 0-待成交 1-已成交 2-部分成交 3-已取消 4-已失效", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "过期时间")
    private LocalDateTime expireAt;

    @Schema(description = "取消原因", example = "用户取消")
    private String cancelReason;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    // 关联信息
    @Schema(description = "市场问题", example = "Will BTC reach 100K?")
    private String marketQuestion;

}
