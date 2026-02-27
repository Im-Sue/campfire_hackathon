package cn.iocoder.yudao.module.treasure.controller.admin.ticket.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 票号分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TreasureTicketPageReqVO extends PageParam {

    @Schema(description = "奖池 ID", example = "1")
    private Long poolId;

    @Schema(description = "持有者地址", example = "0x...")
    private String ownerAddress;

    @Schema(description = "是否中奖", example = "true")
    private Boolean isWinner;

    @Schema(description = "是否已领奖", example = "false")
    private Boolean isClaimed;

    @Schema(description = "合约地址", example = "0x...")
    private String contractAddress;

    @Schema(description = "链 ID", example = "10143")
    private Integer chainId;
}
