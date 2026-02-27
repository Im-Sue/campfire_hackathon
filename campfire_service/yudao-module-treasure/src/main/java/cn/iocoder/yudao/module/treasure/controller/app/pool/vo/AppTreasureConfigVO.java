package cn.iocoder.yudao.module.treasure.controller.app.pool.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 夺宝模块公开配置响应 VO
 *
 * @author Sue
 */
@Schema(description = "用户端 - 夺宝模块配置")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppTreasureConfigVO {

    @Schema(description = "TreasurePool 合约地址", example = "0x31F7ed553109C8361a5A57cc893B5aEB145cf7b1")
    private String contractAddress;

    @Schema(description = "区块链网络 ID", example = "10143")
    private Integer chainId;

    @Schema(description = "平台手续费率（基点，500 = 5%）", example = "500")
    private Integer platformFeeRate;
}
