package cn.iocoder.yudao.module.ai.tool.chat;

import cn.iocoder.yudao.module.market.dal.dataobject.market.PmMarketDO;
import cn.iocoder.yudao.module.market.service.market.PmMarketService;
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

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * C端对话工具：推荐热门市场
 *
 * 为用户推荐热门或高价值市场
 *
 * @author campfire
 */
@Component("recommendMarkets")
@Slf4j
public class RecommendMarketsToolFunction implements Function<RecommendMarketsToolFunction.Request, RecommendMarketsToolFunction.Response> {

    @Resource
    private PmMarketService marketService;

    @Data
    @JsonClassDescription("推荐热门或高价值的预测市场")
    public static class Request {

        @JsonProperty(value = "type")
        @JsonPropertyDescription("推荐类型：hot-热门市场")
        private String type;

        @JsonProperty(value = "limit")
        @JsonPropertyDescription("返回数量限制，默认5，最大10")
        private Integer limit;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class Response {

        private String recommendationType;
        private List<MarketRecommendation> markets;
        private String error;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class MarketRecommendation {
            private Long id;
            private Long eventId;
            private String question;
            private Integer status;
            private String reason;
        }

    }

    @Override
    public Response apply(Request request) {
        String type = request.getType() != null ? request.getType() : "hot";
        log.info("[RecommendMarketsToolFunction] 推荐市场, type={}", type);

        try {
            int limit = request.getLimit() != null ? Math.min(request.getLimit(), 10) : 5;

            List<PmMarketDO> markets = marketService.getTradingMarkets();
            String reasonTemplate = "正在交易中";

            if (markets.size() > limit) {
                markets = markets.subList(0, limit);
            }

            List<Response.MarketRecommendation> items = markets.stream()
                    .map(m -> new Response.MarketRecommendation(
                            m.getId(),
                            m.getEventId(),
                            m.getQuestion(),
                            m.getStatus(),
                            reasonTemplate))
                    .collect(Collectors.toList());

            return new Response()
                    .setRecommendationType(type)
                    .setMarkets(items);

        } catch (Exception e) {
            log.error("[RecommendMarketsToolFunction] 推荐异常", e);
            return new Response().setError("推荐失败: " + e.getMessage());
        }
    }

}
