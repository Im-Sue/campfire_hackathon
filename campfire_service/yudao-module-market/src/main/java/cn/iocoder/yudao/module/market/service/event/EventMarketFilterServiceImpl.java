package cn.iocoder.yudao.module.market.service.event;

import cn.iocoder.yudao.module.market.api.PolymarketApiClient;
import cn.iocoder.yudao.module.market.api.dto.PolymarketEventDTO;
import cn.iocoder.yudao.module.market.api.dto.PolymarketEventDTO.MarketDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 事件市场过滤服务实现
 * 
 * 从 Polymarket API 获取事件的市场列表，过滤出有交易量和流动性的有效市场，
 * 并缓存结果到 Redis（5分钟）
 */
@Slf4j
@Service
public class EventMarketFilterServiceImpl implements EventMarketFilterService {

    /**
     * Redis 缓存 Key 前缀
     */
    private static final String CACHE_KEY_PREFIX = "event:valid_markets:";

    /**
     * 事件交易量缓存 Key 前缀
     */
    private static final String VOLUME_CACHE_KEY_PREFIX = "event:volume:";

    /**
     * 缓存过期时间（分钟）
     */
    private static final long CACHE_TTL_MINUTES = 5;

    /**
     * 流动性最小阈值
     */
    private static final BigDecimal MIN_LIQUIDITY = BigDecimal.ONE;

    /**
     * 并发请求线程池大小
     */
    private static final int THREAD_POOL_SIZE = 10;

    /**
     * 批量请求超时时间（秒）
     */
    private static final int BATCH_TIMEOUT_SECONDS = 30;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private PolymarketApiClient polymarketApiClient;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 异步请求线程池
     */
    private ExecutorService asyncExecutor;

    @PostConstruct
    public void init() {
        asyncExecutor = Executors.newFixedThreadPool(THREAD_POOL_SIZE, r -> {
            Thread t = new Thread(r, "polymarket-api-" + System.currentTimeMillis());
            t.setDaemon(true);
            return t;
        });
        log.info("[init][Polymarket API 线程池已初始化，大小={}]", THREAD_POOL_SIZE);
    }

    @PreDestroy
    public void destroy() {
        if (asyncExecutor != null) {
            asyncExecutor.shutdown();
            try {
                if (!asyncExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    asyncExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                asyncExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            log.info("[destroy][Polymarket API 线程池已关闭]");
        }
    }

    @Override
    public List<String> getValidMarketIds(String polymarketEventId) {
        if (polymarketEventId == null || polymarketEventId.isEmpty()) {
            return Collections.emptyList();
        }

        // 检查缓存
        List<String> cachedIds = getFromCache(polymarketEventId);
        if (cachedIds != null) {
            return cachedIds;
        }

        // 调用 API 并缓存
        return fetchAndCacheValidMarketIds(polymarketEventId);
    }

    @Override
    public Map<String, List<String>> batchGetValidMarketIds(List<String> polymarketEventIds) {
        if (polymarketEventIds == null || polymarketEventIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // 去重
        List<String> uniqueIds = polymarketEventIds.stream()
                .filter(id -> id != null && !id.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        Map<String, List<String>> result = new ConcurrentHashMap<>();
        List<String> needFetch = new ArrayList<>();

        // 1. 先检查缓存
        for (String eventId : uniqueIds) {
            List<String> cached = getFromCache(eventId);
            if (cached != null) {
                result.put(eventId, cached);
            } else {
                needFetch.add(eventId);
            }
        }

        log.info("[batchGetValidMarketIds][总数={}, 缓存命中={}, 需请求={}]",
                uniqueIds.size(), result.size(), needFetch.size());

        if (needFetch.isEmpty()) {
            return result;
        }

        // 2. 并发调用 Polymarket API
        List<CompletableFuture<Void>> futures = needFetch.stream()
                .map(eventId -> CompletableFuture.runAsync(() -> {
                    try {
                        List<String> validIds = fetchAndCacheValidMarketIds(eventId);
                        result.put(eventId, validIds);
                    } catch (Exception e) {
                        log.warn("[batchGetValidMarketIds][调用失败 eventId={}]", eventId, e);
                        result.put(eventId, Collections.emptyList());
                    }
                }, asyncExecutor))
                .collect(Collectors.toList());

        // 3. 等待所有请求完成
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(BATCH_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            log.info("[batchGetValidMarketIds][批量请求完成，成功={}]", result.size());
        } catch (TimeoutException e) {
            log.error("[batchGetValidMarketIds][批量请求超时 {}秒]", BATCH_TIMEOUT_SECONDS);
        } catch (Exception e) {
            log.error("[batchGetValidMarketIds][批量请求异常]", e);
        }

        return result;
    }

    @Override
    public void invalidateCache(String polymarketEventId) {
        if (polymarketEventId == null || polymarketEventId.isEmpty()) {
            return;
        }
        String cacheKey = CACHE_KEY_PREFIX + polymarketEventId;
        stringRedisTemplate.delete(cacheKey);
        log.info("[invalidateCache][缓存已清除 eventId={}]", polymarketEventId);
    }

    /**
     * 从缓存获取有效市场 ID（按交易量降序排列）
     */
    private List<String> getFromCache(String polymarketEventId) {
        try {
            String cacheKey = CACHE_KEY_PREFIX + polymarketEventId;
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                List<String> cachedIds = parseMarketIds(cached);
                log.debug("[getFromCache][缓存命中 eventId={}, count={}]",
                        polymarketEventId, cachedIds.size());
                return cachedIds;
            }
        } catch (Exception e) {
            log.warn("[getFromCache][读取缓存失败 eventId={}]", polymarketEventId, e);
        }
        return null;
    }

    /**
     * 从 Polymarket API 获取有效市场并缓存（按交易量降序排列）
     */
    private List<String> fetchAndCacheValidMarketIds(String polymarketEventId) {
        log.debug("[fetchAndCacheValidMarketIds][调用 API eventId={}]", polymarketEventId);

        PolymarketEventDTO eventDTO = polymarketApiClient.getEventDetail(polymarketEventId);
        if (eventDTO == null || eventDTO.getMarkets() == null) {
            log.warn("[fetchAndCacheValidMarketIds][获取失败或无市场 eventId={}]", polymarketEventId);
            return Collections.emptyList();
        }

        // 过滤有效市场，按交易量降序排序
        List<String> validIds = eventDTO.getMarkets().stream()
                .filter(this::isValidMarket)
                .sorted((a, b) -> {
                    BigDecimal volA = a.getVolume() != null ? a.getVolume() : BigDecimal.ZERO;
                    BigDecimal volB = b.getVolume() != null ? b.getVolume() : BigDecimal.ZERO;
                    return volB.compareTo(volA); // 降序
                })
                .map(MarketDTO::getId)
                .collect(Collectors.toList());

        log.info("[fetchAndCacheValidMarketIds][eventId={}, total={}, valid={}]",
                polymarketEventId, eventDTO.getMarkets().size(), validIds.size());

        // 存入缓存
        try {
            String cacheKey = CACHE_KEY_PREFIX + polymarketEventId;
            String json = objectMapper.writeValueAsString(validIds);
            stringRedisTemplate.opsForValue().set(cacheKey, json, CACHE_TTL_MINUTES, TimeUnit.MINUTES);

            // 缓存事件交易量
            if (eventDTO.getVolume() != null) {
                String volumeKey = VOLUME_CACHE_KEY_PREFIX + polymarketEventId;
                stringRedisTemplate.opsForValue().set(volumeKey, eventDTO.getVolume().toPlainString(), CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            }
        } catch (JsonProcessingException e) {
            log.error("[fetchAndCacheValidMarketIds][缓存写入失败 eventId={}]", polymarketEventId, e);
        }

        return validIds;
    }

    /**
     * 判断市场是否有效
     */
    private boolean isValidMarket(MarketDTO market) {
        boolean hasVolume = market.getVolume() != null
                && market.getVolume().compareTo(BigDecimal.ZERO) > 0;
        boolean hasLiquidity = market.getLiquidity() != null
                && market.getLiquidity().compareTo(MIN_LIQUIDITY) >= 0;
        boolean isActive = Boolean.TRUE.equals(market.getActive());
        boolean notClosed = !Boolean.TRUE.equals(market.getClosed());

        return hasVolume && hasLiquidity && isActive && notClosed;
    }

    /**
     * 解析缓存的市场 ID 列表
     */
    private List<String> parseMarketIds(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (JsonProcessingException e) {
            log.error("[parseMarketIds][解析失败 json={}]", json, e);
            return new ArrayList<>();
        }
    }

    @Override
    public BigDecimal getEventVolume(String polymarketEventId) {
        if (polymarketEventId == null || polymarketEventId.isEmpty()) {
            return null;
        }
        try {
            String volumeKey = VOLUME_CACHE_KEY_PREFIX + polymarketEventId;
            String volumeStr = stringRedisTemplate.opsForValue().get(volumeKey);
            if (volumeStr != null) {
                return new BigDecimal(volumeStr);
            }
        } catch (Exception e) {
            log.warn("[getEventVolume][读取缓存失败 eventId={}]", polymarketEventId, e);
        }
        return null;
    }

    @Override
    public Map<String, BigDecimal> batchGetEventVolumes(List<String> polymarketEventIds) {
        Map<String, BigDecimal> result = new HashMap<>();
        if (polymarketEventIds == null || polymarketEventIds.isEmpty()) {
            return result;
        }
        for (String eventId : polymarketEventIds) {
            BigDecimal volume = getEventVolume(eventId);
            if (volume != null) {
                result.put(eventId, volume);
            }
        }
        return result;
    }

}
