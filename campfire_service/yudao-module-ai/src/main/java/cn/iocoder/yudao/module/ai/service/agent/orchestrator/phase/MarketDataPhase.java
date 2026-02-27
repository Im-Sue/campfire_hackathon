package cn.iocoder.yudao.module.ai.service.agent.orchestrator.phase;

import cn.iocoder.yudao.module.ai.service.agent.orchestrator.DiscussionPhase;
import cn.iocoder.yudao.module.ai.service.agent.orchestrator.RoomContext;
import cn.iocoder.yudao.module.market.dal.dataobject.event.PmEventDO;
import cn.iocoder.yudao.module.market.service.event.PmEventService;
import cn.iocoder.yudao.module.market.service.market.PmMarketService;
import cn.iocoder.yudao.module.market.service.price.PmPriceService;
import cn.iocoder.yudao.module.market.service.price.PriceInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Phase 1: 市场数据收集阶段
 * 收集事件信息、市场价格、外部数据（新闻/热点）
 *
 * @author campfire
 */
@Component
@Slf4j
public class MarketDataPhase implements DiscussionPhase {

    @Resource
    private PmEventService eventService;

    @Resource
    private PmMarketService marketService;

    @Resource
    private PmPriceService priceService;

    @Override
    public void execute(RoomContext context) {
        log.info("[MarketDataPhase] 开始收集市场数据, eventId={}", context.getEventId());

        // 1. 获取事件信息
        PmEventDO event = eventService.getEvent(context.getEventId());
        if (event == null) {
            log.error("[MarketDataPhase] 事件不存在, eventId={}", context.getEventId());
            return;
        }
        
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("id", event.getId());
        eventData.put("title", event.getTitle());
        eventData.put("category", event.getCategory());
        eventData.put("status", event.getStatus());
        eventData.put("endDate", event.getEndDate());
        context.setMarketData("event", eventData);

        // 2. 获取市场列表和价格
        var markets = marketService.getMarketsByEventId(context.getEventId());
        boolean allClosed = true;
        
        for (var market : markets) {
            Map<String, Object> marketInfo = new HashMap<>();
            marketInfo.put("id", market.getId());
            marketInfo.put("question", market.getQuestion());
            marketInfo.put("status", market.getStatus());
            marketInfo.put("outcomes", market.getOutcomes()); // 添加outcomes信息

            // 获取价格
            Map<Integer, PriceInfo> prices = priceService.getAllPrices(market.getId());
            marketInfo.put("prices", prices);

            context.setMarketData("market_" + market.getId(), marketInfo);
            
            // 检查是否还在交易
            if (market.getStatus() == 1) { // TRADING
                allClosed = false;
            }
        }
        
        context.setAllMarketsClosed(allClosed);
        
        // 3. 外部工具数据 (TODO: 后续接入新闻/热点搜索)
        // 预留扩展点

        log.info("[MarketDataPhase] 市场数据收集完成, markets={}, allClosed={}", 
                markets.size(), allClosed);
    }

    @Override
    public String getName() {
        return "市场数据收集";
    }

    @Override
    public int getOrder() {
        return 1;
    }

}

