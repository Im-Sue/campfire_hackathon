package cn.iocoder.yudao.module.market.service.sync;

import org.springframework.context.ApplicationEvent;

/**
 * 市场状态同步事件
 * 
 * 用于在检测到需要同步市场状态时发布事件，避免循环依赖
 */
public class MarketStatusSyncEvent extends ApplicationEvent {

    /**
     * Token ID（CLOB Token ID）
     */
    private final String tokenId;

    public MarketStatusSyncEvent(Object source, String tokenId) {
        super(source);
        this.tokenId = tokenId;
    }

    public String getTokenId() {
        return tokenId;
    }
}
