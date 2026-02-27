package cn.iocoder.yudao.module.market.service.price;

import cn.iocoder.yudao.module.market.api.PolymarketApiClient;
import cn.iocoder.yudao.module.market.dal.dataobject.market.PmMarketDO;
import cn.iocoder.yudao.module.market.dal.mysql.market.PmMarketMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 价格管理 Service 实现类
 */
@Service
@Validated
@Slf4j
public class PmPriceServiceImpl implements PmPriceService {

    /**
     * Redis Key 前缀
     */
    private static final String PRICE_KEY_PREFIX = "market:price:";

    /**
     * 缓存过期时间（秒）
     */
    private static final long CACHE_TTL_SECONDS = 60;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private PmMarketMapper pmMarketMapper;

    @Resource
    private PolymarketApiClient polymarketApiClient;

    @Override
    public PriceInfo getPriceByTokenId(String tokenId) {
        // log.info("[getPriceByTokenId][开始获取价格 tokenId={}]", tokenId);
        if (tokenId == null || tokenId.isEmpty()) {
            log.info("[getPriceByTokenId][tokenId为空]");
            return new PriceInfo().setTokenId(tokenId);
        }

        PriceInfo info = new PriceInfo().setTokenId(tokenId);

        // 1. 从 Redis 读取
        String bidKey = PRICE_KEY_PREFIX + tokenId + ":bid";
        String askKey = PRICE_KEY_PREFIX + tokenId + ":ask";
        String timeKey = PRICE_KEY_PREFIX + tokenId + ":time";
        // log.info("[getPriceByTokenId][查询Redis - bidKey={}, askKey={}]", bidKey, askKey);

        String bidStr = stringRedisTemplate.opsForValue().get(bidKey);
        String askStr = stringRedisTemplate.opsForValue().get(askKey);
        String timeStr = stringRedisTemplate.opsForValue().get(timeKey);
        // log.info("[getPriceByTokenId][Redis查询结果 - bidStr={}, askStr={}, timeStr={}]", bidStr, askStr, timeStr);

        if (bidStr != null || askStr != null) {
            // 缓存命中
            if (bidStr != null) {
                info.setBestBid(new BigDecimal(bidStr));
            }
            if (askStr != null) {
                info.setBestAsk(new BigDecimal(askStr));
            }
            if (timeStr != null) {
                info.setUpdateTime(Long.parseLong(timeStr));
            }
            // log.info("[getPriceByTokenId][缓存命中 tokenId={}, bid={}, ask={}]", tokenId, bidStr, askStr);
        } else {
            // 2. 缓存未命中，从 CLOB API 获取
            log.info("[getPriceByTokenId][缓存未命中，准备调用CLOB API tokenId={}]", tokenId);
            try {
                // log.info("[getPriceByTokenId][调用polymarketApiClient.getPrice SELL]");
                BigDecimal apiBid = polymarketApiClient.getPrice(tokenId, "SELL");
                // log.info("[getPriceByTokenId][CLOB API SELL结果 - tokenId={}, apiBid={}]", tokenId, apiBid);

                // log.info("[getPriceByTokenId][调用polymarketApiClient.getPrice BUY]");
                BigDecimal apiAsk = polymarketApiClient.getPrice(tokenId, "BUY");
                // log.info("[getPriceByTokenId][CLOB API BUY结果 - tokenId={}, apiAsk={}]", tokenId, apiAsk);

                info.setBestBid(apiBid);
                info.setBestAsk(apiAsk);
                info.setUpdateTime(System.currentTimeMillis());

                // 写入缓存
                if (apiBid != null || apiAsk != null) {
                    // log.info("[getPriceByTokenId][写入Redis缓存 tokenId={}, bid={}, ask={}]", tokenId, apiBid, apiAsk);
                    updatePrice(tokenId, apiBid, apiAsk);
                }

                // log.info("[getPriceByTokenId][API 回退完成 tokenId={}, bid={}, ask={}]", tokenId, apiBid, apiAsk);
            } catch (Exception e) {
                log.warn("[getPriceByTokenId][CLOB API 获取价格失败 tokenId={}]", tokenId, e);
            }
        }

        return info.calculate();
    }

    @Override
    public PriceInfo getPriceByOutcomeIndex(Long marketId, int outcomeIndex) {
        PmMarketDO market = pmMarketMapper.selectById(marketId);
        if (market == null) {
            log.warn("[getPriceByOutcomeIndex][市场不存在 marketId={}]", marketId);
            return new PriceInfo();
        }

        List<String> tokenIds = market.getClobTokenIds();
        List<String> outcomes = market.getOutcomes();

        if (tokenIds == null || outcomeIndex >= tokenIds.size()) {
            log.warn("[getPriceByOutcomeIndex][选项索引越界 marketId={}, outcomeIndex={}, size={}]",
                    marketId, outcomeIndex, tokenIds != null ? tokenIds.size() : 0);
            return new PriceInfo();
        }

        String tokenId = tokenIds.get(outcomeIndex);
        PriceInfo info = getPriceByTokenId(tokenId);

        // 补充 outcome 信息
        info.setOutcomeIndex(outcomeIndex);
        if (outcomes != null && outcomeIndex < outcomes.size()) {
            info.setOutcomeName(outcomes.get(outcomeIndex));
        }

        return info;
    }

    @Override
    public PriceInfo getPriceByOutcomeName(Long marketId, String outcomeName) {
        PmMarketDO market = pmMarketMapper.selectById(marketId);
        if (market == null) {
            log.warn("[getPriceByOutcomeName][市场不存在 marketId={}]", marketId);
            return new PriceInfo();
        }

        List<String> outcomes = market.getOutcomes();
        if (outcomes == null) {
            log.warn("[getPriceByOutcomeName][市场无 outcomes marketId={}]", marketId);
            return new PriceInfo();
        }

        int index = outcomes.indexOf(outcomeName);
        if (index < 0) {
            log.warn("[getPriceByOutcomeName][选项不存在 marketId={}, outcomeName={}]", marketId, outcomeName);
            return new PriceInfo();
        }

        return getPriceByOutcomeIndex(marketId, index);
    }

    @Override
    public Map<Integer, PriceInfo> getAllPrices(Long marketId) {
        // log.info("[getAllPrices][===== 开始获取所有选项价格 marketId={} =====]", marketId);
        Map<Integer, PriceInfo> result = new HashMap<>();

        PmMarketDO market = pmMarketMapper.selectById(marketId);
        if (market == null) {
            log.warn("[getAllPrices][市场不存在 marketId={}]", marketId);
            return result;
        }

        List<String> tokenIds = market.getClobTokenIds();
        List<String> outcomes = market.getOutcomes();
        // log.info("[getAllPrices][市场信息 - outcomes={}, clobTokenIds={}]", outcomes, tokenIds);

        if (tokenIds == null || tokenIds.isEmpty()) {
            log.warn("[getAllPrices][市场无 tokenIds marketId={}]", marketId);
            return result;
        }

        for (int i = 0; i < tokenIds.size(); i++) {
            String tokenId = tokenIds.get(i);
            log.debug("[getAllPrices][开始处理选项 index={}, tokenId={}]", i, tokenId);
            PriceInfo info = getPriceByTokenId(tokenId);
            log.debug("[getAllPrices][选项价格结果 index={}, tokenId={}, priceInfo={}]", i, tokenId, info);

            info.setOutcomeIndex(i);
            if (outcomes != null && i < outcomes.size()) {
                info.setOutcomeName(outcomes.get(i));
            }

            result.put(i, info);
        }

        return result;
    }

    @Override
    public void updatePrice(String tokenId, BigDecimal bestBid, BigDecimal bestAsk) {
        if (tokenId == null || tokenId.isEmpty()) {
            return;
        }

        String bidKey = PRICE_KEY_PREFIX + tokenId + ":bid";
        String askKey = PRICE_KEY_PREFIX + tokenId + ":ask";
        String timeKey = PRICE_KEY_PREFIX + tokenId + ":time";

        if (bestBid != null) {
            stringRedisTemplate.opsForValue().set(bidKey, bestBid.toPlainString(), CACHE_TTL_SECONDS, TimeUnit.SECONDS);
        }
        if (bestAsk != null) {
            stringRedisTemplate.opsForValue().set(askKey, bestAsk.toPlainString(), CACHE_TTL_SECONDS, TimeUnit.SECONDS);
        }
        stringRedisTemplate.opsForValue().set(timeKey, String.valueOf(System.currentTimeMillis()), CACHE_TTL_SECONDS,
                TimeUnit.SECONDS);

        log.debug("[updatePrice][更新价格 tokenId={}, bid={}, ask={}]", tokenId, bestBid, bestAsk);
    }

    @Override
    public void updatePrice(String tokenId, String bestBid, String bestAsk) {
        BigDecimal bid = null;
        BigDecimal ask = null;

        try {
            if (bestBid != null && !bestBid.isEmpty()) {
                bid = new BigDecimal(bestBid);
            }
        } catch (NumberFormatException e) {
            log.warn("[updatePrice][bid 格式错误 tokenId={}, bestBid={}]", tokenId, bestBid);
        }

        try {
            if (bestAsk != null && !bestAsk.isEmpty()) {
                ask = new BigDecimal(bestAsk);
            }
        } catch (NumberFormatException e) {
            log.warn("[updatePrice][ask 格式错误 tokenId={}, bestAsk={}]", tokenId, bestAsk);
        }

        updatePrice(tokenId, bid, ask);
    }

}
