package cn.iocoder.yudao.module.treasure.controller.app.pool.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 参与者列表项 VO
 *
 * @author Sue
 */
@Schema(description = "用户端 - 参与者信息")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppParticipantVO {

    @Schema(description = "参与者钱包地址")
    private String address;

    @Schema(description = "票号索引")
    private Integer ticketIndex;

    @Schema(description = "票号展示码")
    private String displayCode;

    @Schema(description = "购买时间")
    private LocalDateTime purchaseTime;
}
