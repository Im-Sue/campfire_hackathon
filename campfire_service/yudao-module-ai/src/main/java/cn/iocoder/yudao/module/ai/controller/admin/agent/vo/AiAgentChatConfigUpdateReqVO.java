package cn.iocoder.yudao.module.ai.controller.admin.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI Agent 对话配置更新请求 VO
 *
 * @author campfire
 */
@Data
@Schema(description = "管理后台 - AI Agent 对话配置更新请求")
public class AiAgentChatConfigUpdateReqVO {

    @Schema(description = "配置键", requiredMode = Schema.RequiredMode.REQUIRED, example = "free_quota_daily")
    @NotBlank(message = "配置键不能为空")
    private String configKey;

    @Schema(description = "配置值", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotBlank(message = "配置值不能为空")
    private String configValue;

    @Schema(description = "配置说明", example = "每用户每天免费对话次数")
    private String description;

}
