package cn.iocoder.yudao.module.ai.tool.agent;

import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiAgentDO;
import cn.iocoder.yudao.module.ai.service.agent.AiAgentService;
import cn.iocoder.yudao.module.market.dal.dataobject.position.PmPositionDO;
import cn.iocoder.yudao.module.market.service.position.PmPositionService;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.function.Function;

/**
 * 工具：查询Agent在指定市场的持仓情况
 *
 * @author campfire
 */
@Component("getMyPosition")
@Slf4j
public class GetMyPositionToolFunction implements Function<GetMyPositionToolFunction.Request, GetMyPositionToolFunction.Response> {

    @Resource
    private AiAgentService agentService;

    @Resource
    private PmPositionService positionService;

    @Data
    @JsonClassDescription("查询Agent在指定市场的持仓情况，包括Yes和No的持仓数量")
    public static class Request {

        @JsonProperty(required = true, value = "agentId")
        @JsonPropertyDescription("Agent ID")
        private Long agentId;

        @JsonProperty(required = true, value = "marketId")
        @JsonPropertyDescription("市场ID")
        private Long marketId;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class Response {

        private Long agentId;
        private Long marketId;
        private BigDecimal yesAmount;
        private BigDecimal noAmount;
        private Long yesCost;
        private Long noCost;
        private String error;

    }

    @Override
    public Response apply(Request request) {
        log.info("[GetMyPositionToolFunction] 查询持仓, agentId={}, marketId={}", 
                request.getAgentId(), request.getMarketId());

        AiAgentDO agent = agentService.getAgent(request.getAgentId());
        if (agent == null) {
            return new Response().setError("Agent不存在");
        }

        Long walletUserId = agent.getWalletUserId();
        
        // 查询Yes和No方向的持仓
        PmPositionDO yesPosition = positionService.getPosition(walletUserId, request.getMarketId(), "Yes");
        PmPositionDO noPosition = positionService.getPosition(walletUserId, request.getMarketId(), "No");

        return new Response()
                .setAgentId(request.getAgentId())
                .setMarketId(request.getMarketId())
                .setYesAmount(yesPosition != null ? yesPosition.getQuantity() : BigDecimal.ZERO)
                .setNoAmount(noPosition != null ? noPosition.getQuantity() : BigDecimal.ZERO)
                .setYesCost(yesPosition != null ? yesPosition.getTotalCost() : 0L)
                .setNoCost(noPosition != null ? noPosition.getTotalCost() : 0L);
    }

}
