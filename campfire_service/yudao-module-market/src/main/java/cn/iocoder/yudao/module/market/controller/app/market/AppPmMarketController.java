package cn.iocoder.yudao.module.market.controller.app.market;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.market.api.PolymarketApiClient;
import cn.iocoder.yudao.module.market.api.dto.PolymarketEventDTO;
import cn.iocoder.yudao.module.market.controller.app.market.vo.AppEventPriceHistoryRespVO;
import cn.iocoder.yudao.module.market.controller.app.market.vo.AppMarketDetailRespVO;
import cn.iocoder.yudao.module.market.controller.app.market.vo.AppMarketPriceHistoryVO;
import cn.iocoder.yudao.module.market.controller.app.market.vo.AppMarketPricesRespVO;
import cn.iocoder.yudao.module.market.dal.dataobject.event.PmEventDO;
import cn.iocoder.yudao.module.market.dal.dataobject.market.PmMarketDO;
import cn.iocoder.yudao.module.market.service.event.EventMarketFilterService;
import cn.iocoder.yudao.module.market.service.event.PmEventService;
import cn.iocoder.yudao.module.market.service.market.PmMarketService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import java.math.BigDecimal;
import java.util.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 预测市场")
@RestController
@RequestMapping("/app-market/market")
@Validated
@Slf4j
public class AppPmMarketController {

    @Resource
    private PmEventService pmEventService;

    @Resource
    private PmMarketService pmMarketService;

    @Resource
    private PolymarketApiClient polymarketApiClient;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private EventMarketFilterService eventMarketFilterService;

    @GetMapping("/get")
    @Operation(summary = "获取市场详情")
    @Parameter(name = "id", description = "市场编号", required = true)
    @PermitAll
    public CommonResult<AppMarketDetailRespVO> getMarketDetail(@RequestParam("id") Long id) {
        // 1. 获取本地市场数据
        PmMarketDO market = pmMarketService.getMarket(id);
        if (market == null) {
            return success(null);
        }

        // 2. 从 Polymarket API 获取动态数据
        PolymarketEventDTO.MarketDTO apiMarket = polymarketApiClient.getMarketDetail(market.getPolymarketId());

        // 3. 组装响应
        AppMarketDetailRespVO result = new AppMarketDetailRespVO();
        result.setId(market.getId());
        result.setEventId(market.getEventId());
        result.setPolymarketId(market.getPolymarketId());
        result.setConditionId(market.getConditionId());
        result.setQuestion(market.getQuestion());
        result.setGroupItemTitle(market.getGroupItemTitle());
        result.setOutcomes(market.getOutcomes());
        result.setStatus(market.getStatus());
        result.setStartDate(market.getStartDate());
        result.setEndDate(market.getEndDate());

        // 填充动态数据
        if (apiMarket != null) {
            result.setVolume(apiMarket.getVolume());
            result.setLiquidity(apiMarket.getLiquidity());
            result.setBestBid(apiMarket.getBestBid());
            result.setBestAsk(apiMarket.getBestAsk());
            result.setAcceptingOrders(apiMarket.getAcceptingOrders());
            // 优先使用 API 返回的 enableOrderBook，否则根据 clobTokenIds 判断
            if (apiMarket.getEnableOrderBook() != null) {
                result.setEnableOrderBook(apiMarket.getEnableOrderBook());
            } else {
                result.setEnableOrderBook(
                        apiMarket.getClobTokenIds() != null && !apiMarket.getClobTokenIds().isEmpty());
            }
            // 解析价格
            result.setOutcomePrices(parseOutcomePrices(market.getOutcomes(), apiMarket.getOutcomePrices()));
        } else {
            // API 不可用时使用缓存价格
            try {
                Map<String, BigDecimal> prices = pmMarketService.getMarketPrices(id);
                result.setOutcomePrices(prices);
            } catch (Exception e) {
                log.warn("[getMarketDetail][获取缓存价格失败, id={}]", id);
            }
        }

        return success(result);
    }

    @GetMapping("/prices")
    @Operation(summary = "获取市场实时价格（轻量级，从缓存读取）")
    @Parameter(name = "id", description = "市场编号", required = true)
    @PermitAll
    public CommonResult<AppMarketPricesRespVO> getMarketPrices(@RequestParam("id") Long id) {
        log.info("[getMarketPrices][===== 开始处理价格请求 =====]");
        log.info("[getMarketPrices][请求参数: marketId={}]", id);

        // 1. 获取本地市场数据
        PmMarketDO market = pmMarketService.getMarket(id);
        if (market == null) {
            log.info("[getMarketPrices][市场不存在 marketId={}]", id);
            return success(null);
        }
        log.info("[getMarketPrices][市场数据 - polymarketId={}, status={}, outcomes={}, clobTokenIds={}]",
                market.getPolymarketId(), market.getStatus(), market.getOutcomes(), market.getClobTokenIds());

        // 2. 组装响应
        AppMarketPricesRespVO result = new AppMarketPricesRespVO();
        result.setId(market.getId());
        result.setPolymarketId(market.getPolymarketId());

        // 3. 从缓存获取成交价（内部有 API 回退）
        try {
            log.info("[getMarketPrices][开始获取市场价格...]");
            Map<String, BigDecimal> prices = pmMarketService.getMarketPrices(id);
            log.info("[getMarketPrices][获取到的成交价格: outcomePrices={}]", prices);
            result.setOutcomePrices(prices);
        } catch (Exception e) {
            log.warn("[getMarketPrices][获取缓存价格失败, id={}]", id, e);
            result.setOutcomePrices(Collections.emptyMap());
        }

        // 4. 从缓存获取每个选项的 bid/ask
        Map<String, AppMarketPricesRespVO.OutcomeBidAsk> outcomeBidAsk = new LinkedHashMap<>();
        List<String> outcomes = market.getOutcomes();
        List<String> clobTokenIds = market.getClobTokenIds();
        boolean hasAnyBidAsk = false;

        if (outcomes != null && clobTokenIds != null) {
            for (int i = 0; i < outcomes.size() && i < clobTokenIds.size(); i++) {
                String outcome = outcomes.get(i);
                String tokenId = clobTokenIds.get(i);

                AppMarketPricesRespVO.OutcomeBidAsk bidAsk = new AppMarketPricesRespVO.OutcomeBidAsk();

                try {
                    // 从 Redis 读取 bid/ask
                    String bidKey = "market:price:" + tokenId + ":bid";
                    String askKey = "market:price:" + tokenId + ":ask";
                    log.info("[getMarketPrices][Redis键 - bidKey={}, askKey={}]", bidKey, askKey);

                    String bidStr = stringRedisTemplate.opsForValue().get(bidKey);
                    String askStr = stringRedisTemplate.opsForValue().get(askKey);
                    log.info("[getMarketPrices][Redis结果 - outcome={}, tokenId={}, bidStr={}, askStr={}]",
                            outcome, tokenId, bidStr, askStr);

                    if (bidStr != null) {
                        bidAsk.setBestBid(new BigDecimal(bidStr));
                        hasAnyBidAsk = true;
                    }
                    if (askStr != null) {
                        bidAsk.setBestAsk(new BigDecimal(askStr));
                        hasAnyBidAsk = true;
                    }

                    // 计算价差
                    if (bidAsk.getBestBid() != null && bidAsk.getBestAsk() != null) {
                        bidAsk.setSpread(bidAsk.getBestAsk().subtract(bidAsk.getBestBid()));
                    }
                } catch (Exception e) {
                    log.warn("[getMarketPrices][获取 bid/ask 失败, tokenId={}]", tokenId, e);
                }

                outcomeBidAsk.put(outcome, bidAsk);
            }
        }

        // 5. Redis 无 bid/ask 数据时，从 CLOB API 逐个获取每个 Token 的订单簿
        if (!hasAnyBidAsk && clobTokenIds != null) {
            for (int i = 0; i < outcomes.size() && i < clobTokenIds.size(); i++) {
                String outcome = outcomes.get(i);
                String tokenId = clobTokenIds.get(i);

                AppMarketPricesRespVO.OutcomeBidAsk bidAsk = outcomeBidAsk.get(outcome);
                if (bidAsk == null) {
                    bidAsk = new AppMarketPricesRespVO.OutcomeBidAsk();
                    outcomeBidAsk.put(outcome, bidAsk);
                }

                // 只有当该 outcome 没有 bid/ask 时才从 API 获取
                if (bidAsk.getBestBid() == null && bidAsk.getBestAsk() == null) {
                    log.info("[getMarketPrices][Redis无缓存，准备调用CLOB /price API - outcome={}, tokenId={}]", outcome,
                            tokenId);
                    try {
                        // 使用 /price API 替代 /book，更轻量（同时保留 404 异常处理触发市场状态同步）
                        BigDecimal apiBid = polymarketApiClient.getPrice(tokenId, "SELL");
                        BigDecimal apiAsk = polymarketApiClient.getPrice(tokenId, "BUY");
                        log.info("[getMarketPrices][CLOB /price API结果 - outcome={}, tokenId={}, bid={}, ask={}]",
                                outcome, tokenId, apiBid, apiAsk);
                        if (apiBid != null) {
                            bidAsk.setBestBid(apiBid);
                        }
                        if (apiAsk != null) {
                            bidAsk.setBestAsk(apiAsk);
                        }
                        if (bidAsk.getBestBid() != null && bidAsk.getBestAsk() != null) {
                            bidAsk.setSpread(bidAsk.getBestAsk().subtract(bidAsk.getBestBid()));
                        }
                        log.info("[getMarketPrices][从 CLOB /price API 获取 {} 的 bid/ask, tokenId={}]", outcome, tokenId);
                    } catch (Exception e) {
                        log.warn("[getMarketPrices][CLOB /price API 获取 bid/ask 失败, outcome={}, tokenId={}]", outcome,
                                tokenId,
                                e);
                    }
                }
            }
            result.setAcceptingOrders(true);
        } else {
            result.setAcceptingOrders(true);
        }

        result.setOutcomeBidAsk(outcomeBidAsk);

        // 6. 从 outcomeBidAsk 中提取卖出价格（bestBid）生成 outcomeSellPrices
        Map<String, BigDecimal> sellPrices = new LinkedHashMap<>();
        for (Map.Entry<String, AppMarketPricesRespVO.OutcomeBidAsk> entry : outcomeBidAsk.entrySet()) {
            if (entry.getValue() != null && entry.getValue().getBestBid() != null) {
                sellPrices.put(entry.getKey(), entry.getValue().getBestBid());
            }
        }
        result.setOutcomeSellPrices(sellPrices);

        result.setUpdateTime(System.currentTimeMillis());

        log.debug("[getMarketPrices][===== 响应结果 =====]");
        log.debug(
                "[getMarketPrices][最终响应: id={}, polymarketId={}, outcomePrices={}, outcomeSellPrices={}, outcomeBidAsk={}]",
                result.getId(), result.getPolymarketId(), result.getOutcomePrices(), result.getOutcomeSellPrices(),
                result.getOutcomeBidAsk());
        return success(result);
    }

    @GetMapping("/price-history")
    @Operation(summary = "获取事件下所有市场的K线历史")
    @Parameter(name = "eventId", description = "事件编号", required = true)
    @PermitAll
    public CommonResult<AppEventPriceHistoryRespVO> getEventPriceHistory(
            @RequestParam("eventId") Long eventId,
            @RequestParam(value = "interval", defaultValue = "1w") String interval) {

        // 0. 体育赛事（gameId != null）不返回K线数据
        PmEventDO event = pmEventService.getEvent(eventId);
        if (event == null) {
            return success(null);
        }
        if (event.getGameId() != null) {
            log.debug("[getEventPriceHistory][体育赛事不返回K线, eventId={}, gameId={}]", eventId, event.getGameId());
            return success(null);
        }

        // 1. 从缓存获取有效市场（已按交易量降序排列）
        String polymarketEventId = event.getPolymarketEventId();
        List<String> validMarketIds = Collections.emptyList();
        if (polymarketEventId != null && !polymarketEventId.isEmpty()) {
            validMarketIds = eventMarketFilterService.getValidMarketIds(polymarketEventId);
        }

        // 2. 取前 4 个市场
        Set<String> topMarketIds = validMarketIds.stream()
                .limit(4)
                .collect(java.util.stream.Collectors.toSet());

        // 3. 获取本地市场，过滤出 TOP 4
        List<PmMarketDO> markets = pmMarketService.getMarketsByEventId(eventId);
        if (!topMarketIds.isEmpty()) {
            markets = markets.stream()
                    .filter(m -> m.getPolymarketId() != null && topMarketIds.contains(m.getPolymarketId()))
                    .collect(java.util.stream.Collectors.toList());
            log.info("[getEventPriceHistory][K线优化: eventId={}, 筛选后市场数={}]", eventId, markets.size());
        }

        if (markets == null || markets.isEmpty()) {
            return success(null);
        }

        // 2. 批量获取每个 Market 的价格历史
        AppEventPriceHistoryRespVO result = new AppEventPriceHistoryRespVO();
        result.setEventId(eventId);
        List<AppMarketPriceHistoryVO> marketHistories = new ArrayList<>();

        for (PmMarketDO market : markets) {
            AppMarketPriceHistoryVO historyVO = new AppMarketPriceHistoryVO();
            historyVO.setMarketId(market.getId());
            historyVO.setPolymarketId(market.getPolymarketId());
            historyVO.setQuestion(market.getQuestion());

            // 获取 clobTokenIds（优先本地，其次从 API 获取）
            List<String> tokenIds = market.getClobTokenIds();
            if (tokenIds == null || tokenIds.isEmpty()) {
                // 本地没有，从 Polymarket API 获取
                tokenIds = getTokenIdsFromApi(market.getPolymarketId());
            }

            if (tokenIds != null && !tokenIds.isEmpty()) {
                historyVO.setEnableOrderBook(true);
                // 获取第一个 token 的价格历史（通常是 Yes 选项）
                List<Map<String, Object>> priceHistory = polymarketApiClient.getPriceHistory(
                        tokenIds.get(0), interval, null, null);
                historyVO.setPriceHistory(priceHistory);
            } else {
                historyVO.setEnableOrderBook(false);
                historyVO.setPriceHistory(Collections.emptyList());
            }

            marketHistories.add(historyVO);
        }

        result.setMarkets(marketHistories);
        return success(result);
    }

    /**
     * 从 Polymarket API 获取 clobTokenIds
     */
    private List<String> getTokenIdsFromApi(String polymarketId) {
        try {
            PolymarketEventDTO.MarketDTO apiMarket = polymarketApiClient.getMarketDetail(polymarketId);
            if (apiMarket != null && apiMarket.getClobTokenIds() != null) {
                // clobTokenIds 格式: "[\"token1\", \"token2\"]"
                return objectMapper.readValue(apiMarket.getClobTokenIds(), new TypeReference<List<String>>() {
                });
            }
        } catch (Exception e) {
            log.warn("[getTokenIdsFromApi][获取 tokenIds 失败, polymarketId={}]", polymarketId, e);
        }
        return Collections.emptyList();
    }

    /**
     * 解析 outcomePrices JSON 字符串为 Map
     */
    private Map<String, BigDecimal> parseOutcomePrices(List<String> outcomes, String outcomePricesJson) {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        if (outcomes == null || outcomePricesJson == null) {
            return result;
        }
        try {
            // outcomePrices 格式: "[\"0.65\", \"0.35\"]"
            List<String> prices = objectMapper.readValue(outcomePricesJson, new TypeReference<List<String>>() {
            });
            for (int i = 0; i < outcomes.size() && i < prices.size(); i++) {
                result.put(outcomes.get(i), new BigDecimal(prices.get(i)));
            }
        } catch (Exception e) {
            log.warn("[parseOutcomePrices][解析价格失败: {}]", outcomePricesJson);
        }
        return result;
    }

}
