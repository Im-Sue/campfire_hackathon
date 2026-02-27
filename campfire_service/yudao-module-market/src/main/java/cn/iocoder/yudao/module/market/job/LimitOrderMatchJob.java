package cn.iocoder.yudao.module.market.job;

import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.market.dal.dataobject.market.PmMarketDO;
import cn.iocoder.yudao.module.market.service.market.PmMarketService;
import cn.iocoder.yudao.module.market.service.order.PmOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 限价单撮合定时任务
 * 
 * 定期扫描所有活跃市场，根据当前价格撮合待成交的限价单
 */
@Component
@Slf4j
public class LimitOrderMatchJob {

    @Resource
    private PmOrderService pmOrderService;

    @Resource
    private PmMarketService pmMarketService;

    /**
     * 每 10 秒扫描一次限价单
     */
    @Scheduled(fixedRate = 10000)
    @TenantIgnore
    public void execute() {
        try {
            // 1. 获取所有交易中的市场
            List<PmMarketDO> tradingMarkets = pmMarketService.getTradingMarkets();

            if (tradingMarkets == null || tradingMarkets.isEmpty()) {
                return;
            }

            int matchedCount = 0;

            // 2. 遍历每个市场
            for (PmMarketDO market : tradingMarkets) {
                try {
                    // 3. 获取市场当前价格
                    Map<String, BigDecimal> prices = pmMarketService.getMarketPrices(market.getId());
                    if (prices == null || prices.isEmpty()) {
                        continue;
                    }

                    // 4. 执行撮合（传递完整的价格Map，由service根据订单outcome获取对应价格）
                    pmOrderService.processLimitOrders(market.getId(), prices);

                } catch (Exception e) {
                    log.warn("[LimitOrderMatchJob][处理市场 {} 失败]", market.getId(), e);
                }
            }

            if (matchedCount > 0) {
                log.info("[LimitOrderMatchJob][本次撮合完成，共处理 {} 个市场]", tradingMarkets.size());
            }

        } catch (Exception e) {
            log.error("[LimitOrderMatchJob][限价单撮合任务失败]", e);
        }
    }

}
