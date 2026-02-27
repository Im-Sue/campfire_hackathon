package cn.iocoder.yudao.module.treasure.controller.app.pool.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 奖池详情响应 VO
 *
 * @author Sue
 */
@Schema(description = "用户端 - 奖池详情响应")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppPoolDetailRespVO {

    // ===== 奖池完整信息 =====

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

    @Schema(description = "奖池状态: 0=进行中, 1=已锁定, 2=开奖中, 3=已结算")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    // ===== 奖金信息 =====

    @Schema(description = "总奖金池(wei)")
    private String totalPrize;

    @Schema(description = "总奖金池(MON)")
    private String totalPrizeDisplay;

    // ===== 结算信息 =====

    @Schema(description = "结算信息，status=3时存在")
    private AppSettlementVO settlement;

    // ===== 参与者列表 =====

    @Schema(description = "参与者列表")
    private List<AppParticipantVO> participants;

    @Schema(description = "总参与人数")
    private Integer participantCount;

    // ===== 用户参与信息 =====

    @Schema(description = "用户参与信息，仅登录时返回，未参与时为null")
    private AppUserParticipationVO userParticipation;
}
