package cn.iocoder.yudao.module.ai.tool.agent;

import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiAgentDO;
import cn.iocoder.yudao.module.ai.service.agent.AiAgentService;
import cn.iocoder.yudao.module.market.controller.app.order.vo.AppOrderCreateReqVO;
import cn.iocoder.yudao.module.market.service.order.PmOrderService;
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

import java.util.function.Function;

/**
 * 工具：在预测市场下单，买入Yes或No
 *
 * @author campfire
 */
@Component("placeOrder")
@Slf4j
public class PlaceOrderToolFunction implements Function<PlaceOrderToolFunction.Request, PlaceOrderToolFunction.Response> {

    @Resource
    private AiAgentService agentService;

    @Resource
    private PmOrderService orderService;

    @Data
    @JsonClassDescription("在预测市场下单，买入Yes或No")
    public static class Request {

        @JsonProperty(required = true, value = "agentId")
        @JsonPropertyDescription("Agent ID")
        private Long agentId;

        @JsonProperty(required = true, value = "marketId")
        @JsonPropertyDescription("市场ID")
        private Long marketId;

        @JsonProperty(required = true, value = "outcome")
        @JsonPropertyDescription("下注方向: Yes 或 No")
        private String outcome;

        @JsonProperty(required = true, value = "amount")
        @JsonPropertyDescription("下注金额(积分)")
        private Long amount;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class Response {

        private Boolean success;
        private Long orderId;
        private Long agentId;
        private Long marketId;
        private String outcome;
        private Long amount;
        private String error;

    }

    @Override
    public Response apply(Request request) {
        log.info("[PlaceOrderToolFunction] 下单, agentId={}, marketId={}, outcome={}, amount={}", 
                request.getAgentId(), request.getMarketId(), request.getOutcome(), request.getAmount());

        AiAgentDO agent = agentService.getAgent(request.getAgentId());
        if (agent == null) {
            return new Response().setSuccess(false).setError("Agent不存在");
        }

        // 检查余额
        Long balance = agentService.getAvailableBalance(request.getAgentId());
        if (balance < request.getAmount()) {
            return new Response()
                    .setSuccess(false)
                    .setError("余额不足，当前余额: " + balance + "，需要: " + request.getAmount());
        }

        try {
            // 构建下单请求
            AppOrderCreateReqVO orderReq = new AppOrderCreateReqVO();
            orderReq.setMarketId(request.getMarketId());
            orderReq.setOutcome(request.getOutcome());
            orderReq.setAmount(request.getAmount());
            orderReq.setOrderType(1); // 市价单
            
            // 调用下单服务
            Long orderId = orderService.createOrder(agent.getWalletUserId(), orderReq);

            log.info("[PlaceOrderToolFunction] 下单成功, orderId={}", orderId);
            return new Response()
                    .setSuccess(true)
                    .setOrderId(orderId)
                    .setAgentId(request.getAgentId())
                    .setMarketId(request.getMarketId())
                    .setOutcome(request.getOutcome())
                    .setAmount(request.getAmount());
        } catch (Exception e) {
            log.error("[PlaceOrderToolFunction] 下单失败", e);
            return new Response().setSuccess(false).setError(e.getMessage());
        }
    }

}
