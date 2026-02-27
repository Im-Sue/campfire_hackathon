package cn.iocoder.yudao.module.ai.tool.agent;

import cn.iocoder.yudao.module.market.service.price.PmPriceService;
import cn.iocoder.yudao.module.market.service.price.PriceInfo;
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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 工具：获取预测市场的当前价格
 *
 * @author campfire
 */
@Component("getMarketPrice")
@Slf4j
public class GetMarketPriceToolFunction implements Function<GetMarketPriceToolFunction.Request, GetMarketPriceToolFunction.Response> {

    @Resource
    private PmPriceService priceService;

    @Data
    @JsonClassDescription("获取预测市场的当前价格，包括Yes和No的买卖价格")
    public static class Request {

        @JsonProperty(required = true, value = "marketId")
        @JsonPropertyDescription("市场ID")
        private Long marketId;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class Response {

        private Long marketId;
        private Long lastUpdate;
        private Map<String, PriceData> prices;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class PriceData {
            private BigDecimal bid;
            private BigDecimal ask;
        }

    }

    @Override
    public Response apply(Request request) {
        log.info("[GetMarketPriceToolFunction] 获取市场价格, marketId={}", request.getMarketId());

        Map<Integer, PriceInfo> allPrices = priceService.getAllPrices(request.getMarketId());
        
        Map<String, Response.PriceData> prices = new HashMap<>();
        for (Map.Entry<Integer, PriceInfo> entry : allPrices.entrySet()) {
            PriceInfo price = entry.getValue();
            prices.put("outcome_" + entry.getKey(), 
                    new Response.PriceData(price.getBestBid(), price.getBestAsk()));
        }
        
        return new Response()
                .setMarketId(request.getMarketId())
                .setLastUpdate(System.currentTimeMillis())
                .setPrices(prices);
    }

}
