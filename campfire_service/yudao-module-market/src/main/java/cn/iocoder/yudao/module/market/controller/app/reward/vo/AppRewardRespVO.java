package cn.iocoder.yudao.module.market.controller.app.reward.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "用户 App - 奖励 Response VO")
@Data
public class AppRewardRespVO {

    @Schema(description = "奖励编号", required = true, example = "1024")
    private Long id;

    @Schema(description = "市场编号", required = true, example = "1")
    private Long marketId;

    @Schema(description = "选项", required = true, example = "Yes")
    private String outcome;

    @Schema(description = "持仓份数", required = true, example = "100.5")
    private BigDecimal quantity;

    @Schema(description = "奖励积分", required = true, example = "10000")
    private Long rewardAmount;

    @Schema(description = "状态：0-待领取 1-已领取 2-失败", required = true, example = "0")
    private Integer status;

    @Schema(description = "领取时间")
    private LocalDateTime claimedAt;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

}
