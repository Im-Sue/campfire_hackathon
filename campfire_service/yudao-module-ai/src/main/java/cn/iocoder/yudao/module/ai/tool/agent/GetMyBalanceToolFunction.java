package cn.iocoder.yudao.module.ai.tool.agent;

import cn.iocoder.yudao.module.ai.service.agent.AiAgentService;
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
 * 工具：查询Agent当前的积分余额
 *
 * @author campfire
 */
@Component("getMyBalance")
@Slf4j
public class GetMyBalanceToolFunction implements Function<GetMyBalanceToolFunction.Request, GetMyBalanceToolFunction.Response> {

    @Resource
    private AiAgentService agentService;

    @Data
    @JsonClassDescription("查询Agent当前的积分余额")
    public static class Request {

        @JsonProperty(required = true, value = "agentId")
        @JsonPropertyDescription("Agent ID")
        private Long agentId;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class Response {

        private Long agentId;
        private Long balance;

    }

    @Override
    public Response apply(Request request) {
        log.info("[GetMyBalanceToolFunction] 查询余额, agentId={}", request.getAgentId());

        Long balance = agentService.getAvailableBalance(request.getAgentId());

        return new Response()
                .setAgentId(request.getAgentId())
                .setBalance(balance != null ? balance : 0L);
    }

}
