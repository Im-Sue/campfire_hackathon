package cn.iocoder.yudao.module.market.ws.handler;

import cn.iocoder.yudao.module.market.enums.ws.WsLogRefTypeEnum;
import cn.iocoder.yudao.module.market.enums.ws.WsLogTypeEnum;
import cn.iocoder.yudao.module.market.dal.dataobject.market.PmMarketDO;
import cn.iocoder.yudao.module.market.service.market.PmMarketService;
import cn.iocoder.yudao.module.market.service.order.PmOrderService;
import cn.iocoder.yudao.module.market.service.price.PmPriceService;
import cn.iocoder.yudao.module.market.service.settlement.PmSettlementService;
import cn.iocoder.yudao.module.market.service.sync.PolymarketSyncService;
import cn.iocoder.yudao.module.market.service.ws.PmWsLogService;
import cn.iocoder.yudao.module.market.ws.PolymarketWsManager;
import cn.iocoder.yudao.module.market.ws.message.PolymarketWsMessage;
import cn.iocoder.yudao.module.market.enums.MarketStatusEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * WebSocket 消息处理器
 * 
 * 支持的消息类型：
 * - price_change: 价格变化（订单簿更新）
 * - best_bid_ask: 最佳买卖价变化
 * - last_trade_price: 最新成交价
 * - market_resolved: 市场结算
 * - new_market: 新市场创建
 */
@Component
@Slf4j
public class WsMessageHandler {

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private PmOrderService pmOrderService;

    @Resource
    private PmSettlementService pmSettlementService;

    @Resource
    private PmWsLogService wsLogService;

    @Resource
    private PmPriceService priceService;

    @Resource
    private PmMarketService pmMarketService;

    @Resource
    private PolymarketSyncService polymarketSyncService;

    @Resource
    private PolymarketWsManager polymarketWsManager;

    /**
     * 处理 WebSocket 消息
     */
    public void handleMessage(String messageJson) {
        try {
            // 检查是否是数组格式（如 book 类型消息）
            String trimmed = messageJson.trim();
            if (trimmed.startsWith("[")) {
                log.debug("[handleMessage][忽略数组格式消息（可能是 book 类型）]");
                return;
            }

            PolymarketWsMessage message = objectMapper.readValue(messageJson, PolymarketWsMessage.class);

            // 使用兼容方法获取事件类型（支持 event 和 event_type）
            String eventType = message.getEffectiveEventType();
            if (eventType == null) {
                log.debug("[handleMessage][无事件类型，忽略消息]");
                return;
            }

            switch (eventType) {
                case "price_change":
                    handlePriceChange(message, messageJson);
                    break;
                case "best_bid_ask":
                    handleBestBidAsk(message, messageJson);
                    break;
                case "last_trade_price":
                    handleLastTradePrice(message, messageJson);
                    break;
                case "market_resolved":
                    handleMarketResolved(message, messageJson);
                    break;
                case "new_market":
                    handleNewMarket(message, messageJson);
                    break;
                default:
                    log.debug("[handleMessage][忽略事件类型: {}]", eventType);
            }

        } catch (Exception e) {
            // 截断过长的错误信息避免数据库存储失败
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.length() > 500) {
                errorMsg = errorMsg.substring(0, 500) + "...";
            }
            // 打印完整原始消息便于排查
            String msgPreview = messageJson;
            if (msgPreview != null && msgPreview.length() > 1000) {
                msgPreview = msgPreview.substring(0, 1000) + "...";
            }
            log.error("[handleMessage][解析消息失败]\n  错误: {}\n  原始消息: {}", errorMsg, msgPreview);
            // 记录错误日志（截断描述）
            wsLogService.logAsync(WsLogTypeEnum.ERROR, null, "解析消息失败: " + errorMsg);
        }
    }

    /**
     * 处理价格变化（新格式：price_changes 数组）
     */
    private void handlePriceChange(PolymarketWsMessage message, String messageJson) {
        if (message.getPriceChanges() == null || message.getPriceChanges().isEmpty()) {
            log.debug("[handlePriceChange][price_changes 为空]");
            return;
        }

        for (PolymarketWsMessage.PriceChange change : message.getPriceChanges()) {
            String tokenId = change.getAssetId();
            if (tokenId == null) {
                continue;
            }

            // 获取最佳买卖价
            String bestBid = change.getBestBid();
            String bestAsk = change.getBestAsk();

            if (bestBid != null || bestAsk != null) {
                // 通过统一价格服务更新缓存
                priceService.updatePrice(tokenId, bestBid, bestAsk);

                log.debug("[handlePriceChange][价格更新 tokenId={}, bestBid={}, bestAsk={}]",
                        tokenId, bestBid, bestAsk);
            }
        }

        // 记录日志（取第一个 token 作为关联 ID）
        String firstTokenId = message.getPriceChanges().get(0).getAssetId();
        wsLogService.logAsync(WsLogTypeEnum.PRICE_CHANGE, firstTokenId, WsLogRefTypeEnum.TOKEN_ID,
                "price_change", messageJson,
                String.format("价格变化 %d 个资产", message.getPriceChanges().size()));
    }

    /**
     * 处理最佳买卖价变化
     */
    private void handleBestBidAsk(PolymarketWsMessage message, String messageJson) {
        String tokenId = message.getAssetId();
        if (tokenId == null) {
            return;
        }

        String bestBid = message.getBestBid();
        String bestAsk = message.getBestAsk();

        // 通过统一价格服务更新缓存
        priceService.updatePrice(tokenId, bestBid, bestAsk);

        log.debug("[handleBestBidAsk][价格更新 tokenId={}, bestBid={}, bestAsk={}, spread={}]",
                tokenId, bestBid, bestAsk, message.getSpread());

        // 记录日志
        wsLogService.logAsync(WsLogTypeEnum.PRICE_CHANGE, tokenId, WsLogRefTypeEnum.TOKEN_ID,
                "best_bid_ask", messageJson,
                String.format("最佳价格 bid=%s ask=%s", bestBid, bestAsk));
    }

    /**
     * 处理最新成交价
     */
    private void handleLastTradePrice(PolymarketWsMessage message, String messageJson) {
        String tokenId = message.getAssetId();
        if (tokenId == null) {
            return;
        }

        String price = message.getPrice();
        if (price != null) {
            // 记录成交价日志（成交价不更新 bid/ask 缓存）
            log.debug("[handleLastTradePrice][成交价更新 tokenId={}, price={}, size={}, side={}]",
                    tokenId, price, message.getSize(), message.getSide());

            // 记录日志
            wsLogService.logAsync(WsLogTypeEnum.TRADE, tokenId, WsLogRefTypeEnum.TOKEN_ID,
                    "last_trade_price", messageJson,
                    String.format("成交 price=%s size=%s side=%s", price, message.getSize(), message.getSide()));
        }
    }

    /**
     * 处理市场结算
     */
    private void handleMarketResolved(PolymarketWsMessage message, String messageJson) {
        String marketId = message.getMarket();
        if (marketId == null || message.getSettlement() == null) {
            return;
        }

        String winnerOutcome = message.getSettlement().getWinnerOutcome();
        log.info("[handleMarketResolved][市场结算 marketId={}, winnerOutcome={}]", marketId, winnerOutcome);

        // 记录日志
        wsLogService.logAsync(WsLogTypeEnum.SETTLEMENT, marketId, WsLogRefTypeEnum.MARKET_ID,
                "market_resolved", messageJson,
                String.format("市场结算 winnerOutcome=%s", winnerOutcome));

        // 根据 polymarketId 找到本地 marketId，创建结算记录
        try {
            PmMarketDO market = pmMarketService.getMarketByPolymarketId(marketId);
            if (market != null) {
                Long settlementId = pmSettlementService.createSettlement(
                        market.getId(), marketId, winnerOutcome, "polymarket_ws");
                log.info("[handleMarketResolved][自动创建结算记录 settlementId={}, localMarketId={}]",
                        settlementId, market.getId());
            } else {
                log.warn("[handleMarketResolved][未找到本地市场 polymarketId={}]", marketId);
            }
        } catch (Exception e) {
            log.error("[handleMarketResolved][创建结算记录失败 polymarketId={}, error={}]", marketId, e.getMessage(), e);
        }
    }

    /**
     * 处理新市场创建
     */
    private void handleNewMarket(PolymarketWsMessage message, String messageJson) {
        // 从消息中提取事件信息
        PolymarketWsMessage.EventMessage eventMessage = message.getEventMessage();
        if (eventMessage == null || eventMessage.getId() == null) {
            log.debug("[handleNewMarket][event_message 为空或无 id]");
            return;
        }

        String polymarketEventId = eventMessage.getId();
        String polymarketMarketId = message.getId();
        String conditionId = message.getMarket();
        String question = message.getQuestion();
        java.util.List<String> outcomes = message.getOutcomes();
        java.util.List<String> tokenIds = message.getAssetsIds();

        log.info("[handleNewMarket][收到新市场 polymarketEventId={}, polymarketMarketId={}, question={}]",
                polymarketEventId, polymarketMarketId, question);

        // 记录日志
        wsLogService.logAsync(WsLogTypeEnum.NEW_MARKET, polymarketMarketId, WsLogRefTypeEnum.MARKET_ID,
                "new_market", messageJson,
                String.format("新市场 eventId=%s question=%s", polymarketEventId, question));

        try {
            // 调用同步服务添加市场
            Long localMarketId = polymarketSyncService.addMarketToEvent(
                    polymarketEventId, polymarketMarketId, conditionId, question, outcomes, tokenIds);

            if (localMarketId != null) {
                log.info("[handleNewMarket][新市场导入成功 localMarketId={}]", localMarketId);

                // 如果市场进入 TRADING 状态，自动订阅价格更新
                PmMarketDO newMarket = pmMarketService.getMarket(localMarketId);
                if (newMarket != null && MarketStatusEnum.TRADING.getStatus().equals(newMarket.getStatus())) {
                    if (tokenIds != null && !tokenIds.isEmpty()) {
                        polymarketWsManager.subscribe(tokenIds);
                        log.info("[handleNewMarket][已自动订阅新市场价格 tokenIds={}]", tokenIds);
                    }
                }
            }
        } catch (Exception e) {
            log.error("[handleNewMarket][新市场导入失败 polymarketEventId={}, polymarketMarketId={}, error={}]",
                    polymarketEventId, polymarketMarketId, e.getMessage(), e);
        }
    }

}
