package cn.iocoder.yudao.module.treasure.controller.app.pool.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户参与信息 VO
 *
 * @author Sue
 */
@Schema(description = "用户端 - 用户参与信息")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppUserParticipationVO {

    @Schema(description = "是否已参与该奖池")
    private Boolean hasJoined;

    @Schema(description = "票号索引")
    private Integer ticketIndex;

    @Schema(description = "票号展示码")
    private String displayCode;

    @Schema(description = "购买时间")
    private LocalDateTime purchaseTime;

    @Schema(description = "购买交易哈希")
    private String purchaseTxHash;

    // ===== 中奖相关（仅已结算奖池返回） =====

    @Schema(description = "是否中奖")
    private Boolean isWinner;

    @Schema(description = "中奖金额(wei)")
    private String prizeAmount;

    @Schema(description = "中奖金额(MON)")
    private String prizeAmountDisplay;

    @Schema(description = "是否已领奖")
    private Boolean isClaimed;

    @Schema(description = "领奖时间")
    private LocalDateTime claimTime;

    @Schema(description = "领奖交易哈希")
    private String claimTxHash;
}
