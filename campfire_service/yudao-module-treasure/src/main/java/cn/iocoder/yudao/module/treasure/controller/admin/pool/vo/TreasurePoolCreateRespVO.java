package cn.iocoder.yudao.module.treasure.controller.admin.pool.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 管理后台 - 创建奖池 Response VO
 *
 * @author Sue
 */
@Data
@Builder
public class TreasurePoolCreateRespVO {

    @Schema(description = "链上奖池 ID", example = "1")
    private Long poolId;

    @Schema(description = "交易哈希", example = "0x1234")
    private String txHash;
}

