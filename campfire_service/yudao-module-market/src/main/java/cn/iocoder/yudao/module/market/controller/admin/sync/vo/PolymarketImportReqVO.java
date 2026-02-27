package cn.iocoder.yudao.module.market.controller.admin.sync.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 导入 Polymarket 事件请求 VO
 */
@Schema(description = "管理后台 - 导入 Polymarket 事件请求")
@Data
public class PolymarketImportReqVO {

    @Schema(description = "Polymarket Event ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "16068")
    @NotBlank(message = "Polymarket Event ID 不能为空")
    private String polymarketEventId;

}
