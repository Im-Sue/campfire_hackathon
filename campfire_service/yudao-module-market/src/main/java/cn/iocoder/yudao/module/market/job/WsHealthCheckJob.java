package cn.iocoder.yudao.module.market.job;

import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.market.dal.dataobject.market.PmMarketDO;
import cn.iocoder.yudao.module.market.service.market.PmMarketService;
import cn.iocoder.yudao.module.market.ws.PolymarketWsManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * WebSocket 健康检查任务
 */
@Component
@Slf4j
public class WsHealthCheckJob {

    @Resource
    private PolymarketWsManager polymarketWsManager;

    @Resource
    private PmMarketService pmMarketService;

    /**
     * 每分钟检查 WebSocket 连接状态和订阅一致性
     */
    @Scheduled(cron = "0 */1 * * * ?")
    @TenantIgnore
    public void execute() {
        // 检查连接状态
        if (!polymarketWsManager.isConnected()) {
            log.warn("[WsHealthCheckJob][WebSocket 未连接，尝试重连]");
            polymarketWsManager.connectAndSubscribe();
            return;
        }

        // 检查订阅一致性
        checkSubscriptionConsistency();
    }

    /**
     * 检查订阅一致性
     */
    private void checkSubscriptionConsistency() {
        try {
            // 获取数据库中交易中的市场
            List<PmMarketDO> tradingMarkets = pmMarketService.getTradingMarkets();
            List<String> expectedTokenIds = new ArrayList<>();
            for (PmMarketDO market : tradingMarkets) {
                if (market.getClobTokenIds() != null) {
                    expectedTokenIds.addAll(market.getClobTokenIds());
                }
            }

            // 获取实际订阅的 Token IDs
            Set<String> actualSubscribed = polymarketWsManager.getSubscribedTokenIds();

            // 找出缺少订阅的
            List<String> missing = new ArrayList<>();
            for (String tokenId : expectedTokenIds) {
                if (!actualSubscribed.contains(tokenId)) {
                    missing.add(tokenId);
                }
            }

            // 找出多余订阅的
            List<String> extra = new ArrayList<>();
            for (String tokenId : actualSubscribed) {
                if (!expectedTokenIds.contains(tokenId)) {
                    extra.add(tokenId);
                }
            }

            // 修正差异
            if (!missing.isEmpty()) {
                log.warn("[WsHealthCheckJob][发现 {} 个缺少订阅，自动补充]", missing.size());
                polymarketWsManager.subscribe(missing);
            }

            if (!extra.isEmpty()) {
                log.warn("[WsHealthCheckJob][发现 {} 个多余订阅，自动取消]", extra.size());
                polymarketWsManager.unsubscribe(extra);
            }

        } catch (Exception e) {
            log.error("[WsHealthCheckJob][检查订阅一致性失败]", e);
        }
    }

}
