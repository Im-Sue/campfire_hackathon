package cn.iocoder.yudao.module.treasure.controller.admin.pool.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 奖池分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TreasurePoolPageReqVO extends PageParam {

    @Schema(description = "链上奖池 ID", example = "1")
    private Long poolId;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "合约地址", example = "0x...")
    private String contractAddress;

    @Schema(description = "链 ID", example = "10143")
    private Integer chainId;
}
