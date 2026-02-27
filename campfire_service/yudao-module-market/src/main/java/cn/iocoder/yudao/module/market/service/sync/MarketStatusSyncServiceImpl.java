package cn.iocoder.yudao.module.market.service.sync;

import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.market.api.PolymarketApiClient;
import cn.iocoder.yudao.module.market.api.dto.PolymarketEventDTO;
import cn.iocoder.yudao.module.market.dal.dataobject.market.PmMarketDO;
import cn.iocoder.yudao.module.market.dal.mysql.market.PmMarketMapper;
import cn.iocoder.yudao.module.market.service.settlement.PmSettlementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 市场状态同步服务实现类
 * 
 * 核心功能：
 * 1. 检测 Polymarket 市场是否已结算
 * 2. 更新本地市场状态
 * 3. 创建结算记录
 * 4. 防抖机制：5分钟内同一市场只处理一次
 */
@Service
@Slf4j
public class MarketStatusSyncServiceImpl implements MarketStatusSyncService {

    /**
     * 防抖缓存：记录最近处理的市场时间戳
     * Key: marketId, Value: 处理时间戳
     */
    private final Map<Long, Long> recentlySyncedMarkets = new ConcurrentHashMap<>();

    /**
     * 防抖时间：5分钟
     */
    private static final long DEBOUNCE_MS = 5 * 60 * 1000;

    @Resource
    private PmMarketMapper pmMarketMapper;

    @Resource
    private PmSettlementService pmSettlementService;

    @Resource
    private PolymarketApiClient polymarketApiClient;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 监听市场状态同步事件
     * 由 PolymarketApiClient 在检测到 404 时发布
     */
    @EventListener
    @Async
    @TenantIgnore
    public void onMarketStatusSyncEvent(MarketStatusSyncEvent event) {
        log.debug("[onMarketStatusSyncEvent][收到同步事件 tokenId={}]", event.getTokenId());
        syncMarketStatusByTokenIdAsync(event.getTokenId());
    }

    @Override
    @Async
    @TenantIgnore
    public void syncMarketStatusAsync(Long marketId) {
        log.debug("[syncMarketStatusAsync][触发异步同步 marketId={}]", marketId);
        syncMarketStatus(marketId);
    }

    @Override
    @Async
    @TenantIgnore
    public void syncMarketStatusByTokenIdAsync(String tokenId) {
        if (tokenId == null || tokenId.isEmpty()) {
            return;
        }

        log.debug("[syncMarketStatusByTokenIdAsync][触发异步同步 tokenId={}]", tokenId);

        // 根据 tokenId 查找市场
        PmMarketDO market = pmMarketMapper.selectByTokenId(tokenId);
        if (market == null) {
            log.warn("[syncMarketStatusByTokenIdAsync][未找到对应市场 tokenId={}]", tokenId);
            return;
        }

        syncMarketStatus(market.getId());
    }

    @Override
    @TenantIgnore
    public boolean syncMarketStatus(Long marketId) {
        // 1. 防抖检查
        if (!shouldProcess(marketId)) {
            log.debug("[syncMarketStatus][跳过防抖 marketId={}]", marketId);
            return false;
        }

        // 2. 获取市场
        PmMarketDO market = pmMarketMapper.selectById(marketId);
        if (market == null) {
            log.warn("[syncMarketStatus][市场不存在 marketId={}]", marketId);
            return false;
        }

        String polymarketId = market.getPolymarketId();
        if (polymarketId == null || polymarketId.isEmpty()) {
            log.warn("[syncMarketStatus][市场无 polymarketId marketId={}]", marketId);
            return false;
        }

        // 3. 查询 Polymarket 市场详情
        PolymarketEventDTO.MarketDTO pmMarket = polymarketApiClient.getMarketDetail(polymarketId);
        if (pmMarket == null) {
            log.warn("[syncMarketStatus][无法获取 Polymarket 市场详情 marketId={}, polymarketId={}]",
                    marketId, polymarketId);
            return false;
        }

        // 4. 检查是否已关闭（结算）
        if (!Boolean.TRUE.equals(pmMarket.getClosed())) {
            log.debug("[syncMarketStatus][市场未关闭 marketId={}]", marketId);
            return false;
        }

        log.info("[syncMarketStatus][检测到市场已结算 marketId={}, polymarketId={}]",
                marketId, polymarketId);

        // 5. 解析获胜选项
        String winnerOutcome = parseWinnerOutcome(pmMarket);
        if (winnerOutcome == null) {
            log.warn("[syncMarketStatus][无法解析获胜选项 marketId={}, outcomePrices={}]",
                    marketId, pmMarket.getOutcomePrices());
            return false;
        }

        // 6. 创建结算记录（幂等）
        try {
            Long settlementId = pmSettlementService.createSettlement(
                    marketId, polymarketId, winnerOutcome, "status_sync");
            log.info("[syncMarketStatus][创建结算记录成功 settlementId={}, marketId={}, winnerOutcome={}]",
                    settlementId, marketId, winnerOutcome);
            return true;
        } catch (Exception e) {
            log.error("[syncMarketStatus][创建结算记录失败 marketId={}]", marketId, e);
            return false;
        }
    }

    /**
     * 防抖检查：5分钟内同一市场只处理一次
     */
    private boolean shouldProcess(Long marketId) {
        long now = System.currentTimeMillis();
        Long lastProcessed = recentlySyncedMarkets.get(marketId);

        if (lastProcessed != null && (now - lastProcessed) < DEBOUNCE_MS) {
            return false;
        }

        // 更新处理时间
        recentlySyncedMarkets.put(marketId, now);

        // 清理过期缓存（简单实现，每次都清理）
        recentlySyncedMarkets.entrySet().removeIf(entry -> (now - entry.getValue()) > DEBOUNCE_MS * 2);

        return true;
    }

    /**
     * 从 outcomePrices 解析获胜选项
     * 
     * outcomePrices 格式: "[\"1.00\", \"0.00\"]" 或 "[\"0.00\", \"1.00\"]"
     * 价格接近 1.00 的选项获胜
     * 
     * outcomes 格式: "[\"Yes\", \"No\"]"
     */
    private String parseWinnerOutcome(PolymarketEventDTO.MarketDTO pmMarket) {
        String outcomePrices = pmMarket.getOutcomePrices();
        String outcomes = pmMarket.getOutcomes();

        if (outcomePrices == null || outcomes == null) {
            return null;
        }

        try {
            // 解析 JSON 数组
            List<String> priceList = objectMapper.readValue(outcomePrices,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
            List<String> outcomeList = objectMapper.readValue(outcomes,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));

            if (priceList.size() != outcomeList.size()) {
                return null;
            }

            // 找到价格接近 1.00 的选项
            for (int i = 0; i < priceList.size(); i++) {
                double price = Double.parseDouble(priceList.get(i));
                if (price >= 0.99) {
                    return outcomeList.get(i);
                }
            }

            log.warn("[parseWinnerOutcome][未找到获胜选项 prices={}, outcomes={}]",
                    outcomePrices, outcomes);
            return null;

        } catch (Exception e) {
            log.error("[parseWinnerOutcome][解析失败 outcomePrices={}, outcomes={}]",
                    outcomePrices, outcomes, e);
            return null;
        }
    }
}
