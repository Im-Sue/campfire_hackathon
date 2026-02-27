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
 * C端对话工具：搜索预测市场
 *
 * @author campfire
 */
@Component("searchMarkets")
@Slf4j
public class SearchMarketsToolFunction implements Function<SearchMarketsToolFunction.Request, SearchMarketsToolFunction.Response> {

    @Resource
    private PmMarketService marketService;

    @Data
    @JsonClassDescription("搜索预测市场，可根据关键词或事件ID筛选")
    public static class Request {

        @JsonProperty(value = "keyword")
        @JsonPropertyDescription("关键词，用于搜索市场问题")
        private String keyword;

        @JsonProperty(value = "eventId")
        @JsonPropertyDescription("事件ID，获取特定事件下的所有市场")
        private Long eventId;

        @JsonProperty(value = "limit")
        @JsonPropertyDescription("返回数量限制，默认10，最大20")
        private Integer limit;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class Response {

        private List<MarketItem> markets;
        private Integer total;
        private String error;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class MarketItem {
            private Long id;
            private Long eventId;
            private String question;
            private Integer status;
        }

    }

    @Override
    public Response apply(Request request) {
        log.info("[SearchMarketsToolFunction] 搜索市场, keyword={}, eventId={}", 
                request.getKeyword(), request.getEventId());

        try {
            int limit = request.getLimit() != null ? Math.min(request.getLimit(), 20) : 10;

            List<PmMarketDO> markets;
            
            if (request.getEventId() != null) {
                markets = marketService.getMarketsByEventId(request.getEventId());
            } else {
                markets = marketService.getTradingMarkets();
            }
            
            if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
                String keyword = request.getKeyword().toLowerCase();
                markets = markets.stream()
                        .filter(m -> m.getQuestion() != null && m.getQuestion().toLowerCase().contains(keyword))
                        .collect(Collectors.toList());
            }
            
            if (markets.size() > limit) {
                markets = markets.subList(0, limit);
            }

            List<Response.MarketItem> items = markets.stream()
                    .map(m -> new Response.MarketItem(
                            m.getId(),
                            m.getEventId(),
                            m.getQuestion(),
                            m.getStatus()))
                    .collect(Collectors.toList());

            return new Response()
                    .setMarkets(items)
                    .setTotal(items.size());

        } catch (Exception e) {
            log.error("[SearchMarketsToolFunction] 搜索异常", e);
            return new Response().setError("搜索失败: " + e.getMessage());
        }
    }

}
