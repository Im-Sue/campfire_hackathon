package cn.iocoder.yudao.module.market.ws;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Polymarket WebSocket 配置
 */
@Component
@ConfigurationProperties(prefix = "polymarket.ws")
@Data
public class PolymarketWsConfig {

    /**
     * WebSocket 端点
     */
    private String endpoint = "wss://ws-subscriptions-clob.polymarket.com/ws/market";

    /**
     * 心跳间隔（毫秒）
     */
    private Long heartbeatIntervalMs = 10000L;

    /**
     * 初始重连延迟（毫秒）
     */
    private Long reconnectInitialMs = 1000L;

    /**
     * 最大重连延迟（毫秒）
     */
    private Long reconnectMaxMs = 60000L;

    /**
     * 心跳超时阈值（秒）
     */
    private Integer staleThresholdSec = 15;

    /**
     * 消息超时阈值（秒）
     */
    private Integer messageTimeoutSec = 60;

}
