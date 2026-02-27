package cn.iocoder.yudao.module.ai.controller.admin.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * AI Agent 对话配置响应 VO
 *
 * @author campfire
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Schema(description = "管理后台 - AI Agent 对话配置响应")
public class AiAgentChatConfigRespVO {

    @Schema(description = "配置编号", example = "1")
    private Long id;

    @Schema(description = "配置键", example = "free_quota_daily")
    private String configKey;

    @Schema(description = "配置值", example = "10")
    private String configValue;

    @Schema(description = "配置说明", example = "每用户每天免费对话次数")
    private String description;

}
