package cn.iocoder.yudao.module.point.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - 积分调整 Request VO")
@Data
public class PointAdjustReqVO {

    @Schema(description = "用户ID", required = true, example = "1")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "钱包地址", example = "0x1234...")
    private String walletAddress;

    @Schema(description = "调整金额（正数增加，负数减少）", required = true, example = "100")
    @NotNull(message = "调整金额不能为空")
    private Long amount;

    @Schema(description = "备注", example = "客服补偿")
    private String remark;

}
