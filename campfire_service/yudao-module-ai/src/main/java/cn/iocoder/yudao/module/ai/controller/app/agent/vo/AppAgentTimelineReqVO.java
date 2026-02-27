package cn.iocoder.yudao.module.ai.controller.app.agent.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI Agent 时间线查询参数
 *
 * @author campfire
 */
@Schema(description = "用户端 - AI Agent 时间线查询参数")
@Data
@EqualsAndHashCode(callSuper = true)
public class AppAgentTimelineReqVO extends PageParam {

    @Schema(description = "消息类型筛选", example = "2")
    private Integer messageType;  // 可选：1=市场数据, 2=讨论, 3=决策, 4=执行

    @Schema(description = "Agent ID 筛选", example = "1")
    private Long agentId;  // 可选：筛选特定 Agent

}
