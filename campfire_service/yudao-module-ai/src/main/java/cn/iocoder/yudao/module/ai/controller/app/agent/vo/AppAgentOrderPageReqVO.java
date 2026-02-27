package cn.iocoder.yudao.module.ai.controller.app.agent.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI Agent 订单分页查询参数
 *
 * @author campfire
 */
@Schema(description = "用户端 - AI Agent 订单分页查询参数")
@Data
@EqualsAndHashCode(callSuper = true)
public class AppAgentOrderPageReqVO extends PageParam {

    @Schema(description = "Agent ID 筛选", example = "1")
    private Long agentId;

    @Schema(description = "市场ID筛选", example = "1")
    private Long marketId;

    @Schema(description = "订单状态筛选", example = "1")
    private Integer status;

}
