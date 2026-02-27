package cn.iocoder.yudao.module.treasure.controller.app.pool.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 结算信息 VO
 *
 * @author Sue
 */
@Schema(description = "用户端 - 结算信息")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppSettlementVO {

    @Schema(description = "每人奖金(wei)")
    private String prizePerWinner;

    @Schema(description = "每人奖金(MON)")
    private String prizePerWinnerDisplay;

    @Schema(description = "开奖时间")
    private LocalDateTime drawTime;

    @Schema(description = "开奖交易哈希")
    private String drawTxHash;

    @Schema(description = "中奖者钱包地址列表")
    private List<String> winnerAddresses;

    @Schema(description = "实际中奖人数")
    private Integer winnerCount;

    @Schema(description = "已领奖人数")
    private Integer totalClaimed;

    @Schema(description = "未领奖人数")
    private Integer totalUnclaimed;
}
