package cn.iocoder.yudao.module.market.job;

import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.market.dal.dataobject.market.PmMarketDO;
import cn.iocoder.yudao.module.market.service.market.PmMarketService;
import cn.iocoder.yudao.module.market.service.sync.MarketStatusSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 市场结算状态同步定时任务
 * 
 * 定期扫描本地 TRADING 状态的市场，检查 Polymarket 是否已结算，
 * 作为 WebSocket 的兜底机制。
 * 
 * 委托 MarketStatusSyncService 执行具体的同步逻辑。
 */
@Component
@Slf4j
public class MarketSettlementSyncJob {

    @Resource
    private PmMarketService pmMarketService;

    @Resource
    private MarketStatusSyncService marketStatusSyncService;

    /**
     * 每 5 分钟扫描一次
     */
    @Scheduled(fixedRate = 300000)
    @TenantIgnore
    public void execute() {
        log.info("[MarketSettlementSyncJob][开始执行结算状态同步]");

        try {
            // 1. 获取所有交易中的市场
            List<PmMarketDO> tradingMarkets = pmMarketService.getTradingMarkets();

            if (tradingMarkets == null || tradingMarkets.isEmpty()) {
                log.debug("[MarketSettlementSyncJob][没有交易中的市场需要检查]");
                return;
            }

            log.info("[MarketSettlementSyncJob][检查 {} 个交易中的市场]", tradingMarkets.size());

            int settledCount = 0;

            // 2. 遍历每个市场，委托 MarketStatusSyncService 处理
            for (PmMarketDO market : tradingMarkets) {
                try {
                    boolean settled = marketStatusSyncService.syncMarketStatus(market.getId());
                    if (settled) {
                        settledCount++;
                    }
                } catch (Exception e) {
                    log.warn("[MarketSettlementSyncJob][检查市场 {} 失败: {}]",
                            market.getId(), e.getMessage());
                }

                // 避免请求过于频繁，每个市场间隔 500ms
                Thread.sleep(500);
            }

            if (settledCount > 0) {
                log.info("[MarketSettlementSyncJob][检测到 {} 个市场已结算]", settledCount);
            }

        } catch (Exception e) {
            log.error("[MarketSettlementSyncJob][结算状态同步任务失败]", e);
        }
    }

}
