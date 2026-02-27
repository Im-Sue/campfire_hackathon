package cn.iocoder.yudao.module.market.controller.app.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "用户 App - 订单 Response VO")
@Data
public class AppOrderRespVO {

    @Schema(description = "订单编号", required = true, example = "1024")
    private Long id;

    @Schema(description = "订单号", required = true, example = "PM1234567890ABCDEF")
    private String orderNo;

    @Schema(description = "市场编号", required = true, example = "1")
    private Long marketId;

    @Schema(description = "市场问题/名称", example = "Maduro out in 2025?")
    private String marketQuestion;

    @Schema(description = "订单类型：1-市价单 2-限价单", required = true, example = "1")
    private Integer orderType;

    @Schema(description = "方向：1-买入 2-卖出", required = true, example = "1")
    private Integer side;

    @Schema(description = "选项", required = true, example = "Yes")
    private String outcome;

    @Schema(description = "价格", required = true, example = "0.50")
    private BigDecimal price;

    @Schema(description = "份数", required = true, example = "100.5")
    private BigDecimal quantity;

    @Schema(description = "金额（积分）", required = true, example = "5000")
    private Long amount;

    @Schema(description = "成交份数", example = "100.5")
    private BigDecimal filledQuantity;

    @Schema(description = "成交金额（积分）", example = "5000")
    private Long filledAmount;

    @Schema(description = "成交价格", example = "0.50")
    private BigDecimal filledPrice;

    @Schema(description = "成交时间")
    private LocalDateTime filledAt;

    @Schema(description = "状态：0-待成交 1-已成交 2-部分成交 3-已取消 4-已失效", required = true, example = "1")
    private Integer status;

    @Schema(description = "过期时间")
    private LocalDateTime expireAt;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

    @Schema(description = "链上状态：0-待上链 1-上链中 2-已上链 3-上链失败", example = "2")
    private Integer chainStatus;

    @Schema(description = "链上交易哈希", example = "0xabc123...")
    private String chainTxHash;

}
