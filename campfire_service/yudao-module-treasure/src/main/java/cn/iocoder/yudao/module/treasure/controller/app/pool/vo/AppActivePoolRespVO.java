package cn.iocoder.yudao.module.treasure.controller.app.pool.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 活跃奖池响应 VO
 *
 * @author Sue
 */
@Schema(description = "用户端 - 活跃奖池响应")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppActivePoolRespVO {

    // ===== 奖池基本信息 =====

    @Schema(description = "链上奖池ID")
    private Long poolId;

    @Schema(description = "智能合约地址")
    private String contractAddress;

    @Schema(description = "区块链网络ID")
    private Integer chainId;

    @Schema(description = "单价原始值(wei)")
    private String price;

    @Schema(description = "单价展示值(MON)")
    private String priceDisplay;

    @Schema(description = "总份数")
    private Integer totalShares;

    @Schema(description = "已售份数")
    private Integer soldShares;

    @Schema(description = "中奖名额")
    private Integer winnerCount;

    @Schema(description = "截止时间")
    private LocalDateTime endTime;

    @Schema(description = "奖池状态: 0=进行中")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    // ===== 奖金信息 =====

    @Schema(description = "当前总奖金池(wei)")
    private String totalPrize;

    @Schema(description = "当前总奖金池(MON)")
    private String totalPrizeDisplay;

    @Schema(description = "预估每人奖金(MON)")
    private String estimatedPrizePerWinner;

    // ===== 用户参与信息 =====

    @Schema(description = "用户参与信息，仅登录时返回，未参与时为null")
    private AppUserParticipationVO userParticipation;
}
