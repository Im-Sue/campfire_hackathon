package cn.iocoder.yudao.module.ai.controller.admin.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - AI 事件房间参与者 Response VO")
@Data
public class AiEventRoomParticipantRespVO {

    @Schema(description = "记录ID", example = "1")
    private Long id;

    @Schema(description = "房间ID", example = "1")
    private Long roomId;

    @Schema(description = "Agent ID", example = "1")
    private Long agentId;

    @Schema(description = "Agent名称", example = "激进投资者")
    private String agentName;

    @Schema(description = "Agent头像URL", example = "https://example.com/avatar.jpg")
    private String agentAvatar;

    @Schema(description = "性格描述", example = "激进")
    private String personality;

    @Schema(description = "初始积分余额", example = "10000")
    private Long initialBalance;

    @Schema(description = "当前积分余额", example = "12000")
    private Long balance;

    @Schema(description = "本房间盈亏", example = "2000")
    private Long profit;

    @Schema(description = "下单数", example = "5")
    private Integer orderCount;

    @Schema(description = "参与者状态: 1=正常 0=异常", example = "1")
    private Integer status;

    @Schema(description = "加入时间")
    private LocalDateTime joinTime;

}
