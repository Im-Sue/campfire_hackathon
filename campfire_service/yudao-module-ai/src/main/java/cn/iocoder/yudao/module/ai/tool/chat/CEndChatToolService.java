package cn.iocoder.yudao.module.ai.tool.chat;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.market.dal.dataobject.event.PmEventDO;
import cn.iocoder.yudao.module.market.dal.dataobject.market.PmMarketDO;
import cn.iocoder.yudao.module.market.dal.dataobject.position.PmPositionDO;
import cn.iocoder.yudao.module.market.service.event.PmEventService;
import cn.iocoder.yudao.module.market.service.market.PmMarketService;
import cn.iocoder.yudao.module.market.service.position.PmPositionService;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * C端对话工具服务
 * 
 * 使用Spring AI的@Tool注解提供AI可调用的工具函数
 * 所有工具均为只读操作，确保C端用户无法通过对话执行敏感操作
 *
 * @author campfire
 */
@Service
@Slf4j
public class CEndChatToolService {

    @Resource
    private PmEventService eventService;

    @Resource
    private PmMarketService marketService;

    @Resource
    private PmPositionService positionService;

    // ========== 搜索事件工具 ==========

    @Tool(name = "searchEvents", description = "搜索预测市场事件，可根据关键词、类别、状态等条件筛选。返回事件列表包含ID、标题、类别、状态、结束时间等信息。")
    public SearchEventsResult searchEvents(
            @ToolParam(description = "关键词，用于搜索事件标题") String keyword,
            @ToolParam(description = "事件类别，如：体育、政治、娱乐、加密货币等", required = false) String category,
            @ToolParam(description = "事件状态：1-未开始，2-进行中，3-已结束", required = false) Integer status,
            @ToolParam(description = "返回数量限制，默认10，最大20", required = false) Integer limit) {
        
        log.info("[searchEvents] 搜索事件, keyword={}, category={}, status={}", keyword, category, status);
        
        try {
            int actualLimit = limit != null ? Math.min(limit, 20) : 10;
            
            PageParam pageParam = new PageParam();
            pageParam.setPageNo(1);
            pageParam.setPageSize(actualLimit);
            
            PageResult<PmEventDO> pageResult = eventService.getEventPage(status, category, keyword, pageParam);
            
            List<EventItem> items = pageResult.getList().stream()
                    .map(e -> new EventItem(
                            e.getId(),
                            e.getTitle(),
                            e.getCategory(),
                            e.getStatus(),
                            e.getEndDate()))
                    .collect(Collectors.toList());
            
            return SearchEventsResult.success(items, items.size());
        } catch (Exception e) {
            log.error("[searchEvents] 搜索异常", e);
            return SearchEventsResult.error("搜索失败: " + e.getMessage());
        }
    }

    // ========== 搜索市场工具 ==========

    @Tool(name = "searchMarkets", description = "搜索预测市场，可根据关键词或事件ID筛选。返回市场列表包含ID、问题、选项、状态等信息。")
    public SearchMarketsResult searchMarkets(
            @ToolParam(description = "关键词，用于搜索市场问题（将先搜索事件，再获取事件下的市场）", required = false) String keyword,
            @ToolParam(description = "关联的事件ID，如果指定则直接获取该事件下的市场", required = false) Long eventId,
            @ToolParam(description = "返回数量限制，默认10，最大20", required = false) Integer limit) {
        
        log.info("[searchMarkets] 搜索市场, keyword={}, eventId={}", keyword, eventId);
        
        try {
            int actualLimit = limit != null ? Math.min(limit, 20) : 10;
            List<MarketItem> items = new ArrayList<>();
            
            if (eventId != null) {
                // 直接获取指定事件下的市场
                List<PmMarketDO> markets = marketService.getMarketsByEventId(eventId);
                items = markets.stream()
                        .limit(actualLimit)
                        .map(m -> new MarketItem(
                                m.getId(),
                                m.getQuestion(),
                                m.getGroupItemTitle(),
                                m.getOutcomes(),
                                m.getStatus(),
                                m.getEndDate()))
                        .collect(Collectors.toList());
            } else if (keyword != null && !keyword.isEmpty()) {
                // 先搜索事件，再获取事件下的市场
                PageParam pageParam = new PageParam();
                pageParam.setPageNo(1);
                pageParam.setPageSize(5); // 最多搜索5个事件
                
                PageResult<PmEventDO> eventResult = eventService.getEventPage(null, null, keyword, pageParam);
                
                for (PmEventDO event : eventResult.getList()) {
                    if (items.size() >= actualLimit) break;
                    
                    List<PmMarketDO> markets = marketService.getMarketsByEventId(event.getId());
                    for (PmMarketDO m : markets) {
                        if (items.size() >= actualLimit) break;
                        items.add(new MarketItem(
                                m.getId(),
                                m.getQuestion(),
                                m.getGroupItemTitle(),
                                m.getOutcomes(),
                                m.getStatus(),
                                m.getEndDate()));
                    }
                }
            } else {
                // 获取交易中的市场
                List<PmMarketDO> markets = marketService.getTradingMarkets();
                items = markets.stream()
                        .limit(actualLimit)
                        .map(m -> new MarketItem(
                                m.getId(),
                                m.getQuestion(),
                                m.getGroupItemTitle(),
                                m.getOutcomes(),
                                m.getStatus(),
                                m.getEndDate()))
                        .collect(Collectors.toList());
            }
            
            return SearchMarketsResult.success(items, items.size());
        } catch (Exception e) {
            log.error("[searchMarkets] 搜索异常", e);
            return SearchMarketsResult.error("搜索失败: " + e.getMessage());
        }
    }

    // ========== 获取用户持仓工具 ==========

    @Tool(name = "getUserPortfolio", description = "获取当前用户的预测市场持仓信息，包括持有的头寸、成本等。")
    public GetUserPortfolioResult getUserPortfolio(
            @ToolParam(description = "用户ID") Long userId,
            @ToolParam(description = "返回数量限制，默认20，最大50", required = false) Integer limit) {
        
        log.info("[getUserPortfolio] 获取用户持仓, userId={}", userId);
        
        if (userId == null) {
            return GetUserPortfolioResult.error("用户未登录");
        }
        
        try {
            int actualLimit = limit != null ? Math.min(limit, 50) : 20;
            
            List<PmPositionDO> positions = positionService.getPositionsByUserId(userId);
            
            // 限制返回数量
            if (positions.size() > actualLimit) {
                positions = positions.subList(0, actualLimit);
            }
            
            List<PositionItem> items = positions.stream()
                    .map(p -> new PositionItem(
                            p.getId(),
                            p.getMarketId(),
                            p.getOutcome(),
                            p.getQuantity(),
                            p.getTotalCost() != null ? BigDecimal.valueOf(p.getTotalCost()) : BigDecimal.ZERO))
                    .collect(Collectors.toList());
            
            // 计算总持仓价值
            BigDecimal totalValue = positions.stream()
                    .map(p -> p.getTotalCost() != null ? BigDecimal.valueOf(p.getTotalCost()) : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            return GetUserPortfolioResult.success(items, items.size(), totalValue);
        } catch (Exception e) {
            log.error("[getUserPortfolio] 查询异常", e);
            return GetUserPortfolioResult.error("查询失败: " + e.getMessage());
        }
    }

    // ========== 推荐热门市场工具 ==========

    @Tool(name = "recommendMarkets", description = "获取热门推荐市场，返回当前交易中的市场。用于向用户推荐可以参与的预测市场。")
    public RecommendMarketsResult recommendMarkets(
            @ToolParam(description = "返回数量，默认5，最大10", required = false) Integer limit) {
        
        log.info("[recommendMarkets] 推荐市场, limit={}", limit);
        
        try {
            int actualLimit = limit != null ? Math.min(limit, 10) : 5;
            
            // 获取交易中的市场
            List<PmMarketDO> markets = marketService.getTradingMarkets();
            
            List<RecommendMarketItem> items = markets.stream()
                    .limit(actualLimit)
                    .map(m -> new RecommendMarketItem(
                            m.getId(),
                            m.getQuestion(),
                            m.getGroupItemTitle(),
                            m.getOutcomes(),
                            "热门交易中"))
                    .collect(Collectors.toList());
            
            return RecommendMarketsResult.success(items);
        } catch (Exception e) {
            log.error("[recommendMarkets] 推荐异常", e);
            return RecommendMarketsResult.error("获取推荐失败: " + e.getMessage());
        }
    }

    // ========== 结果数据类 ==========

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SearchEventsResult {
        private boolean success;
        private List<EventItem> events;
        private Integer total;
        private String error;
        
        public static SearchEventsResult success(List<EventItem> events, Integer total) {
            SearchEventsResult result = new SearchEventsResult();
            result.setSuccess(true);
            result.setEvents(events);
            result.setTotal(total);
            return result;
        }
        
        public static SearchEventsResult error(String error) {
            SearchEventsResult result = new SearchEventsResult();
            result.setSuccess(false);
            result.setError(error);
            result.setEvents(new ArrayList<>());
            return result;
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EventItem {
        private Long id;
        private String title;
        private String category;
        private Integer status;
        private LocalDateTime endDate;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SearchMarketsResult {
        private boolean success;
        private List<MarketItem> markets;
        private Integer total;
        private String error;
        
        public static SearchMarketsResult success(List<MarketItem> markets, Integer total) {
            SearchMarketsResult result = new SearchMarketsResult();
            result.setSuccess(true);
            result.setMarkets(markets);
            result.setTotal(total);
            return result;
        }
        
        public static SearchMarketsResult error(String error) {
            SearchMarketsResult result = new SearchMarketsResult();
            result.setSuccess(false);
            result.setError(error);
            result.setMarkets(new ArrayList<>());
            return result;
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MarketItem {
        private Long id;
        private String question;
        private String groupTitle;
        private List<String> outcomes;
        private Integer status;
        private LocalDateTime endDate;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GetUserPortfolioResult {
        private boolean success;
        private List<PositionItem> positions;
        private Integer total;
        private BigDecimal totalValue;
        private String error;
        
        public static GetUserPortfolioResult success(List<PositionItem> positions, Integer total, BigDecimal totalValue) {
            GetUserPortfolioResult result = new GetUserPortfolioResult();
            result.setSuccess(true);
            result.setPositions(positions);
            result.setTotal(total);
            result.setTotalValue(totalValue);
            return result;
        }
        
        public static GetUserPortfolioResult error(String error) {
            GetUserPortfolioResult result = new GetUserPortfolioResult();
            result.setSuccess(false);
            result.setError(error);
            result.setPositions(new ArrayList<>());
            result.setTotalValue(BigDecimal.ZERO);
            return result;
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PositionItem {
        private Long id;
        private Long marketId;
        private String outcome;
        private BigDecimal quantity;
        private BigDecimal totalCost;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecommendMarketsResult {
        private boolean success;
        private List<RecommendMarketItem> markets;
        private String error;
        
        public static RecommendMarketsResult success(List<RecommendMarketItem> markets) {
            RecommendMarketsResult result = new RecommendMarketsResult();
            result.setSuccess(true);
            result.setMarkets(markets);
            return result;
        }
        
        public static RecommendMarketsResult error(String error) {
            RecommendMarketsResult result = new RecommendMarketsResult();
            result.setSuccess(false);
            result.setError(error);
            result.setMarkets(new ArrayList<>());
            return result;
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecommendMarketItem {
        private Long id;
        private String question;
        private String groupTitle;
        private List<String> outcomes;
        private String recommendReason;
    }

}
