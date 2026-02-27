package cn.iocoder.yudao.module.market.api;

import cn.iocoder.yudao.module.market.api.dto.PolymarketEventDTO;
import cn.iocoder.yudao.module.market.enums.api.ApiLogRefTypeEnum;
import cn.iocoder.yudao.module.market.enums.api.ApiLogStatusEnum;
import cn.iocoder.yudao.module.market.enums.api.ApiLogTypeEnum;
import cn.iocoder.yudao.module.market.service.api.PmApiLogService;
import cn.iocoder.yudao.module.market.service.sync.MarketStatusSyncEvent;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Polymarket REST API 客户端
 * 
 * 用于调用 Polymarket Gamma API 和 CLOB API
 * 所有 API 调用都会记录到 pm_api_log 表
 */
@Component
@Slf4j
public class PolymarketApiClient {

    private static final String GAMMA_API_BASE_URL = "https://gamma-api.polymarket.com";
    private static final String CLOB_API_BASE_URL = "https://clob.polymarket.com";
    private static final int TIMEOUT_SECONDS = 15;

    private WebClient gammaClient;
    private WebClient clobClient;

    @Resource
    private ApplicationEventPublisher eventPublisher;

    @Resource
    private PmApiLogService apiLogService;

    @PostConstruct
    public void init() {
        gammaClient = WebClient.builder()
                .baseUrl(GAMMA_API_BASE_URL)
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024)) // 10MB buffer
                .build();

        clobClient = WebClient.builder()
                .baseUrl(CLOB_API_BASE_URL)
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024))
                .build();

        log.info("[PolymarketApiClient][初始化完成, gamma={}, clob={}]",
                GAMMA_API_BASE_URL, CLOB_API_BASE_URL);
    }

    /**
     * 获取事件列表（支持分页）
     *
     * @param active  是否活跃
     * @param closed  是否已关闭
     * @param tagSlug 标签 slug (politics, sports, crypto 等)
     * @param limit   数量限制（每页条数，最大500）
     * @param offset  偏移量（用于分页）
     * @return 事件列表
     */
    public List<PolymarketEventDTO> getEvents(boolean active, boolean closed, String tagSlug, int limit, int offset) {
        long startTime = System.currentTimeMillis();
        String url = "/events?tag_slug=" + tagSlug + "&limit=" + limit + "&offset=" + offset;
        String params = "{\"active\":" + active + ",\"closed\":" + closed + ",\"tagSlug\":\"" + tagSlug
                + "\",\"limit\":" + limit + ",\"offset\":" + offset + "}";

        try {
            List<PolymarketEventDTO> result = gammaClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/events")
                                .queryParam("active", active)
                                .queryParam("closed", closed)
                                .queryParam("tag_slug", tagSlug)
                                .queryParam("limit", Math.min(limit, 500))
                                .queryParam("offset", offset);
                        uriBuilder.queryParam("order", "volume24hr")
                                .queryParam("ascending", false);
                        return uriBuilder.build();
                    })
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<PolymarketEventDTO>>() {
                    })
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .block();

            if (result == null) {
                result = Collections.emptyList();
            }

            // 记录成功日志
            apiLogService.logSuccessAsync(ApiLogTypeEnum.GAMMA, "getEvents", url, params,
                    System.currentTimeMillis() - startTime, tagSlug, ApiLogRefTypeEnum.TAG_SLUG);

            log.info("[getEvents][获取到 {} 个事件, tagSlug={}, offset={}]", result.size(), tagSlug, offset);
            return result;

        } catch (Exception e) {
            // 记录失败日志
            Integer httpCode = extractHttpCode(e);
            apiLogService.logFailAsync(ApiLogTypeEnum.GAMMA, "getEvents", url, params, httpCode,
                    System.currentTimeMillis() - startTime, e.getMessage(), tagSlug, ApiLogRefTypeEnum.TAG_SLUG);

            log.error("[getEvents][获取事件列表失败, tagSlug={}, offset={}]", tagSlug, offset, e);
            return Collections.emptyList();
        }
    }

    /**
     * 搜索事件
     *
     * @param keyword 搜索关键字
     * @param limit   返回数量限制
     * @return 事件列表
     */
    @SuppressWarnings("unchecked")
    public List<PolymarketEventDTO> searchEvents(String keyword, int limit) {
        long startTime = System.currentTimeMillis();
        String url = "/public-search?q=" + keyword + "&limit_per_type=" + limit;
        String params = "{\"keyword\":\"" + keyword + "\",\"limit\":" + limit + "}";

        try {
            Map<String, Object> response = gammaClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/public-search")
                            .queryParam("q", keyword)
                            .queryParam("limit_per_type", Math.min(limit, 100))
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    })
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .block();

            if (response == null || !response.containsKey("events")) {
                return Collections.emptyList();
            }

            // 从响应中提取 events 字段并转换为 DTO
            Object eventsObj = response.get("events");
            List<PolymarketEventDTO> result = new java.util.ArrayList<>();

            if (eventsObj instanceof List) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                        false);

                for (Object eventObj : (List<?>) eventsObj) {
                    try {
                        PolymarketEventDTO event = mapper.convertValue(eventObj, PolymarketEventDTO.class);
                        result.add(event);
                    } catch (Exception e) {
                        log.warn("[searchEvents][转换事件失败]", e);
                    }
                }
            }

            // 记录成功日志
            apiLogService.logSuccessAsync(ApiLogTypeEnum.GAMMA, "searchEvents", url, params,
                    System.currentTimeMillis() - startTime, keyword, ApiLogRefTypeEnum.TAG_SLUG);

            log.info("[searchEvents][搜索到 {} 个事件, keyword={}]", result.size(), keyword);
            return result;

        } catch (Exception e) {
            // 记录失败日志
            Integer httpCode = extractHttpCode(e);
            apiLogService.logFailAsync(ApiLogTypeEnum.GAMMA, "searchEvents", url, params, httpCode,
                    System.currentTimeMillis() - startTime, e.getMessage(), keyword, ApiLogRefTypeEnum.TAG_SLUG);

            log.error("[searchEvents][搜索事件失败, keyword={}]", keyword, e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取体育赛事
     *
     * @param seriesId 联赛 ID (如 10345=NBA, 10346=NHL)
     * @param tagId    标签 ID (100639 = Games 单场比赛)
     * @return 事件列表
     */

    public List<PolymarketEventDTO> getSportsEvents(String seriesId, Integer tagId) {
        try {
            List<PolymarketEventDTO> result = gammaClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/events")
                            .queryParam("series_id", seriesId)
                            .queryParam("tag_id", tagId)
                            .queryParam("active", true)
                            .queryParam("closed", false)
                            .queryParam("order", "volume24hr")
                            .queryParam("ascending", false)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<PolymarketEventDTO>>() {
                    })
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .block();

            log.info("[getSportsEvents][获取到 {} 个体育事件, seriesId={}]",
                    result != null ? result.size() : 0, seriesId);
            return result != null ? result : Collections.emptyList();

        } catch (Exception e) {
            log.error("[getSportsEvents][获取体育事件失败, seriesId={}]", seriesId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取事件详情
     *
     * @param eventId 事件 ID
     * @return 事件详情
     */
    public PolymarketEventDTO getEventDetail(String eventId) {
        try {
            PolymarketEventDTO result = gammaClient.get()
                    .uri("/events/{id}", eventId)
                    .retrieve()
                    .bodyToMono(PolymarketEventDTO.class)
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .block();

            if (result != null) {
                log.info("[getEventDetail][获取事件详情成功, eventId={}, title={}, markets={}]",
                        eventId, result.getTitle(),
                        result.getMarkets() != null ? result.getMarkets().size() : 0);
            }
            return result;

        } catch (Exception e) {
            log.error("[getEventDetail][获取事件详情失败, eventId={}]", eventId, e);
            return null;
        }
    }

    /**
     * 获取价格历史（K线）
     *
     * @param tokenId  Token ID
     * @param interval 间隔: max, 1d, 1h, 1m
     * @param startTs  开始时间戳
     * @param endTs    结束时间戳
     * @return 价格历史 [{t: 时间戳, p: 价格}, ...]
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getPriceHistory(String tokenId, String interval, Long startTs, Long endTs) {
        try {
            // API 可能返回两种格式:
            // 1. 直接数组: [{t: 123, p: "0.5"}, ...]
            // 2. 对象包装: {history: [{t: 123, p: "0.5"}, ...]}
            Map<String, Object> response = clobClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/prices-history")
                                .queryParam("market", tokenId)
                                .queryParam("interval", interval)
                                .queryParam("fidelity", 60);
                        if (startTs != null) {
                            uriBuilder.queryParam("startTs", startTs);
                        }
                        if (endTs != null) {
                            uriBuilder.queryParam("endTs", endTs);
                        }
                        return uriBuilder.build();
                    })
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    })
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .block();

            if (response == null) {
                return Collections.emptyList();
            }

            // 尝试从 "history" 字段提取
            List<Map<String, Object>> history = null;
            if (response.containsKey("history")) {
                history = (List<Map<String, Object>>) response.get("history");
            }

            log.debug("[getPriceHistory][获取价格历史, tokenId={}, count={}]",
                    tokenId, history != null ? history.size() : 0);
            return history != null ? history : Collections.emptyList();

        } catch (Exception e) {
            log.error("[getPriceHistory][获取价格历史失败, tokenId={}]", tokenId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取所有体育联赛
     */
    public List<Map<String, Object>> getSports() {
        try {
            List<Map<String, Object>> result = gammaClient.get()
                    .uri("/sports")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {
                    })
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .block();

            log.info("[getSports][获取到 {} 个体育联赛]", result != null ? result.size() : 0);
            return result != null ? result : Collections.emptyList();

        } catch (Exception e) {
            log.error("[getSports][获取体育联赛失败]", e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取单个市场详情
     *
     * @param marketId Polymarket 市场 ID (conditionId)
     * @return 市场详情
     */
    public PolymarketEventDTO.MarketDTO getMarketDetail(String marketId) {
        long startTime = System.currentTimeMillis();
        String url = "/markets/" + marketId;
        String params = "{\"marketId\":\"" + marketId + "\"}";

        try {
            PolymarketEventDTO.MarketDTO result = gammaClient.get()
                    .uri("/markets/{id}", marketId)
                    .retrieve()
                    .bodyToMono(PolymarketEventDTO.MarketDTO.class)
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .block();

            // 记录成功日志
            apiLogService.logSuccessAsync(ApiLogTypeEnum.GAMMA, "getMarketDetail", url, params,
                    System.currentTimeMillis() - startTime, marketId, ApiLogRefTypeEnum.MARKET_ID);

            if (result != null) {
                log.debug("[getMarketDetail][获取市场详情成功, marketId={}, question={}]",
                        marketId, result.getQuestion());
            }
            return result;

        } catch (Exception e) {
            // 记录失败日志
            Integer httpCode = extractHttpCode(e);
            apiLogService.logFailAsync(ApiLogTypeEnum.GAMMA, "getMarketDetail", url, params, httpCode,
                    System.currentTimeMillis() - startTime, e.getMessage(), marketId, ApiLogRefTypeEnum.MARKET_ID);

            log.error("[getMarketDetail][获取市场详情失败, marketId={}]", marketId, e);
            return null;
        }
    }

    /**
     * 获取订单簿（从 CLOB API）
     * 用于获取某个 Token 的真实 best bid / best ask
     *
     * @param tokenId Token ID (clobTokenId)
     * @return 订单簿数据 {bids: [[price, size], ...], asks: [[price, size], ...]}
     */
    @SuppressWarnings("unchecked")
    public OrderBookDTO getOrderBook(String tokenId) {
        try {
            Map<String, Object> result = clobClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/book")
                            .queryParam("token_id", tokenId)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    })
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .block();

            if (result == null) {
                return null;
            }

            OrderBookDTO orderBook = new OrderBookDTO();

            // 解析 bids: [[price, size], ...] - 按价格降序
            List<List<String>> bids = (List<List<String>>) result.get("bids");
            if (bids != null && !bids.isEmpty()) {
                // 第一个是最高买价
                orderBook.setBestBid(new BigDecimal(bids.get(0).get(0)));
            }

            // 解析 asks: [[price, size], ...] - 按价格升序
            List<List<String>> asks = (List<List<String>>) result.get("asks");
            if (asks != null && !asks.isEmpty()) {
                // 第一个是最低卖价
                orderBook.setBestAsk(new BigDecimal(asks.get(0).get(0)));
            }

            log.debug("[getOrderBook][获取订单簿, tokenId={}, bestBid={}, bestAsk={}]",
                    tokenId, orderBook.getBestBid(), orderBook.getBestAsk());
            return orderBook;

        } catch (WebClientResponseException.NotFound e) {
            // 404 是预期情况（已结算/已关闭市场），订单簿已被移除
            log.debug("[getOrderBook][订单簿不存在(可能已结算), tokenId={}]", tokenId);
            eventPublisher.publishEvent(new MarketStatusSyncEvent(this, tokenId));
            return null;
        } catch (Exception e) {
            log.warn("[getOrderBook][获取订单簿失败, tokenId={}]", tokenId, e);
            return null;
        }
    }

    /**
     * 订单簿 DTO
     */
    @Data
    public static class OrderBookDTO {
        private BigDecimal bestBid;
        private BigDecimal bestAsk;
    }

    /**
     * 获取单个 Token 的价格（CLOB /price 端点）
     * 
     * @param tokenId Token ID (clobTokenId / asset_id)
     * @param side    BUY (返回 best_ask) 或 SELL (返回 best_bid)
     * @return 价格，失败返回 null
     */
    public BigDecimal getPrice(String tokenId, String side) {
        long startTime = System.currentTimeMillis();
        String url = "/price?token_id=" + tokenId + "&side=" + side;
        String params = "{\"tokenId\":\"" + tokenId + "\",\"side\":\"" + side + "\"}";

        try {
            Map<String, Object> result = clobClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/price")
                            .queryParam("token_id", tokenId)
                            .queryParam("side", side)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    })
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .block();

            if (result == null) {
                return null;
            }

            Object priceObj = result.get("price");
            if (priceObj == null) {
                return null;
            }

            BigDecimal price = new BigDecimal(priceObj.toString());

            // 记录成功日志
            apiLogService.logSuccessAsync(ApiLogTypeEnum.CLOB, "getPrice", url, params,
                    System.currentTimeMillis() - startTime, tokenId, ApiLogRefTypeEnum.TOKEN_ID);

            log.debug("[getPrice][获取价格 tokenId={}, side={}, price={}]", tokenId, side, price);
            return price;

        } catch (WebClientResponseException.NotFound e) {
            // 404 是预期情况（已结算/已关闭市场）
            apiLogService.logFailAsync(ApiLogTypeEnum.CLOB, "getPrice", url, params, 404,
                    System.currentTimeMillis() - startTime, "Token不存在(可能已结算)", tokenId, ApiLogRefTypeEnum.TOKEN_ID);

            log.debug("[getPrice][Token不存在(可能已结算) tokenId={}, side={}]", tokenId, side);
            eventPublisher.publishEvent(new MarketStatusSyncEvent(this, tokenId));
            return null;
        } catch (Exception e) {
            // 记录失败日志
            Integer httpCode = extractHttpCode(e);
            apiLogService.logFailAsync(ApiLogTypeEnum.CLOB, "getPrice", url, params, httpCode,
                    System.currentTimeMillis() - startTime, e.getMessage(), tokenId, ApiLogRefTypeEnum.TOKEN_ID);

            log.warn("[getPrice][获取价格失败 tokenId={}, side={}]", tokenId, side, e);
            return null;
        }
    }

    /**
     * 从异常中提取 HTTP 状态码
     */
    private Integer extractHttpCode(Exception e) {
        if (e instanceof WebClientResponseException) {
            return ((WebClientResponseException) e).getRawStatusCode();
        }
        if (e.getCause() instanceof WebClientResponseException) {
            return ((WebClientResponseException) e.getCause()).getRawStatusCode();
        }
        if (e instanceof TimeoutException || e.getCause() instanceof TimeoutException) {
            return 408; // Request Timeout
        }
        return null;
    }

}
