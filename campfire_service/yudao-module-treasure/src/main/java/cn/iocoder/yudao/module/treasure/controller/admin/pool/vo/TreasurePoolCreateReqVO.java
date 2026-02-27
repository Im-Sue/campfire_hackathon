package cn.iocoder.yudao.module.treasure.controller.admin.pool.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 管理后台 - 创建奖池 Request VO
 *
 * @author Sue
 */
@Data
public class TreasurePoolCreateReqVO {

    @Schema(description = "单价（wei 字符串）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000000000000000000")
    @NotBlank(message = "单价不能为空")
    private String price;

    @Schema(description = "总份数", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "总份数不能为空")
    @Min(value = 1, message = "总份数必须大于 0")
    private Integer totalShares;

    @Schema(description = "持续时长（秒）", requiredMode = Schema.RequiredMode.REQUIRED, example = "86400")
    @NotNull(message = "持续时长不能为空")
    @Min(value = 1, message = "持续时长必须大于 0")
    private Integer duration;

    @Schema(description = "中奖名额", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    @NotNull(message = "中奖名额不能为空")
    @Min(value = 1, message = "中奖名额必须大于 0")
    private Integer winnerCount;

    @Schema(description = "初始奖金（MON 字符串，选填，默认 0）", example = "10")
    private String initialPrize;
}

