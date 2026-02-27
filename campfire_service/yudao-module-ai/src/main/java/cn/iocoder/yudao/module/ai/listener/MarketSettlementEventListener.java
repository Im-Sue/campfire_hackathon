package cn.iocoder.yudao.module.ai.listener;

import cn.iocoder.yudao.module.ai.service.agent.AgentSettlementService;
import cn.iocoder.yudao.module.market.event.MarketSettledEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 市场结算事件监听器
 * 监听管理员手动执行的市场结算事件,触发Agent结算流程
 *
 * @author Sue
 */
@Slf4j
@Component
public class MarketSettlementEventListener {

    @Resource
    private AgentSettlementService agentSettlementService;

    /**
     * 处理市场结算事件
     * 当管理员在后台执行结算操作后,触发Agent结算流程
     *
     * @param event 市场结算事件
     */
    @Async
    @EventListener
    public void handleMarketSettled(MarketSettledEvent event) {
        log.info("[MarketSettlementEventListener] 收到市场结算事件: marketId={}, eventId={}, winnerOutcome={}, settlementId={}",
                event.getMarketId(), event.getEventId(), event.getWinnerOutcome(), event.getSettlementId());

        try {
            // 触发Agent结算流程
            agentSettlementService.onMarketSettled(
                    event.getMarketId(),
                    event.getEventId(),
                    event.getWinnerOutcome()
            );
            log.info("[MarketSettlementEventListener] Agent结算流程触发成功: eventId={}, marketId={}",
                    event.getEventId(), event.getMarketId());
        } catch (Exception e) {
            log.error("[MarketSettlementEventListener] Agent结算流程触发失败: eventId={}, marketId={}",
                    event.getEventId(), event.getMarketId(), e);
        }
    }
}
