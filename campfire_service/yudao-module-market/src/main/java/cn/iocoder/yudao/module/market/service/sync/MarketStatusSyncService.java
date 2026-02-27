package cn.iocoder.yudao.module.market.service.sync;

/**
 * 市场状态同步服务
 * 
 * 统一处理市场状态变更检测和同步
 */
public interface MarketStatusSyncService {

    /**
     * 异步同步市场状态（按 marketId）
     * 带防抖：5分钟内同一市场只处理一次
     * 
     * @param marketId 市场ID
     */
    void syncMarketStatusAsync(Long marketId);

    /**
     * 异步同步市场状态（按 tokenId）
     * 用于 404 检测场景，需要先查找对应的 marketId
     * 
     * @param tokenId CLOB Token ID
     */
    void syncMarketStatusByTokenIdAsync(String tokenId);

    /**
     * 同步执行市场状态检查（供定时任务批量调用）
     * 
     * @param marketId 市场ID
     * @return true 如果检测到状态变更并处理
     */
    boolean syncMarketStatus(Long marketId);
}
