package cn.iocoder.yudao.module.ai.controller.app.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * C端 Agent 对话配额响应 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "C端 - Agent对话配额响应")
public class AppAgentChatQuotaRespVO {

    @Schema(description = "每日免费次数总量", example = "10")
    private Integer freeQuotaTotal;

    @Schema(description = "今日已使用次数", example = "2")
    private Integer freeQuotaUsed;

    @Schema(description = "今日剩余免费次数", example = "8")
    private Integer freeQuotaRemaining;

    @Schema(description = "积分余额", example = "1500")
    private Long pointBalance;

    @Schema(description = "超额每次消耗积分", example = "5")
    private Integer costPerMessage;

}
