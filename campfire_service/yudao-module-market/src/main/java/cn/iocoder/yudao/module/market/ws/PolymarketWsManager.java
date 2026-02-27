package cn.iocoder.yudao.module.market.ws;

import cn.iocoder.yudao.module.market.dal.dataobject.market.PmMarketDO;
import cn.iocoder.yudao.module.market.enums.ws.WsLogTypeEnum;
import cn.iocoder.yudao.module.market.service.market.PmMarketService;
import cn.iocoder.yudao.module.market.service.ws.PmWsLogService;
import cn.iocoder.yudao.module.market.ws.handler.WsMessageHandler;
import cn.iocoder.yudao.module.market.ws.vo.WsStatusVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Polymarket WebSocket 管理器
 * 
 * 负责：
 * 1. 连接管理（连接、断开、重连）
 * 2. 订阅管理（订阅、取消订阅）
 * 3. 消息分发
 * 4. 状态监控
 */
@Component
@Slf4j
public class PolymarketWsManager {

    @Resource
    private PolymarketWsConfig wsConfig;

    @Resource
    private PmMarketService pmMarketService;

    @Resource
    private WsMessageHandler wsMessageHandler;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private PmWsLogService wsLogService;

    /**
     * WebSocket 客户端
     */
    private volatile WebSocketClient wsClient;

    /**
     * 连接状态
     */
    private volatile WsStatusEnum status = WsStatusEnum.INIT;

    /**
     * 最后心跳时间
     */
    private volatile LocalDateTime lastHeartbeatTime;

    /**
     * 最后消息时间
     */
    private volatile LocalDateTime lastMessageTime;

    /**
     * 连接时间
     */
    private volatile LocalDateTime connectedTime;

    /**
     * 重连次数
     */
    private volatile int reconnectCount = 0;

    /**
     * 当前重连延迟
     */
    private volatile long currentReconnectDelay;

    /**
     * 已订阅的 Token IDs
     */
    private final Set<String> subscribedTokenIds = ConcurrentHashMap.newKeySet();

    /**
     * 重连调度器
     */
    private final ScheduledExecutorService reconnectScheduler = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    public void init() {
        log.info("[PolymarketWsManager][初始化 WebSocket 管理器]");
        currentReconnectDelay = wsConfig.getReconnectInitialMs();
        // 延迟启动，等待服务完全初始化
        reconnectScheduler.schedule(this::connectAndSubscribe, 5, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void destroy() {
        log.info("[PolymarketWsManager][销毁 WebSocket 管理器]");
        disconnect();
        reconnectScheduler.shutdown();
    }

    /**
     * 连接并订阅所有活跃市场
     */
    public void connectAndSubscribe() {
        try {
            connect();
            subscribeAllActiveMarkets();
        } catch (Exception e) {
            log.error("[connectAndSubscribe][连接失败]", e);
            scheduleReconnect();
        }
    }

    /**
     * 建立 WebSocket 连接
     */
    public void connect() {
        if (status == WsStatusEnum.CONNECTED || status == WsStatusEnum.CONNECTING) {
            log.info("[connect][已连接或连接中，跳过]");
            return;
        }

        try {
            status = WsStatusEnum.CONNECTING;
            URI uri = new URI(wsConfig.getEndpoint());

            wsClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    handleOpen();
                }

                @Override
                public void onMessage(String message) {
                    handleMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    handleClose(code, reason, remote);
                }

                @Override
                public void onError(Exception ex) {
                    handleError(ex);
                }
            };

            wsClient.connect();
            log.info("[connect][发起 WebSocket 连接: {}]", wsConfig.getEndpoint());

        } catch (Exception e) {
            log.error("[connect][创建 WebSocket 客户端失败]", e);
            status = WsStatusEnum.DISCONNECTED;
            throw new RuntimeException("WebSocket 连接失败", e);
        }
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        if (wsClient != null) {
            try {
                wsClient.close();
            } catch (Exception e) {
                log.warn("[disconnect][关闭连接异常]", e);
            }
        }
        status = WsStatusEnum.DISCONNECTED;
        subscribedTokenIds.clear();
    }

    /**
     * 订阅所有活跃市场
     */
    public void subscribeAllActiveMarkets() {
        List<PmMarketDO> tradingMarkets = pmMarketService.getTradingMarkets();
        List<String> tokenIds = new ArrayList<>();

        for (PmMarketDO market : tradingMarkets) {
            if (market.getClobTokenIds() != null) {
                tokenIds.addAll(market.getClobTokenIds());
            }
        }

        if (!tokenIds.isEmpty()) {
            subscribe(tokenIds);
        }

        log.info("[subscribeAllActiveMarkets][订阅 {} 个市场，{} 个 TokenIds]",
                tradingMarkets.size(), tokenIds.size());
    }

    /**
     * 订阅 Token IDs
     */
    public void subscribe(List<String> tokenIds) {
        if (status != WsStatusEnum.CONNECTED) {
            log.warn("[subscribe][未连接，无法订阅]");
            return;
        }

        if (tokenIds == null || tokenIds.isEmpty()) {
            return;
        }

        try {
            // 首次订阅或增量订阅
            Map<String, Object> message = new HashMap<>();
            if (subscribedTokenIds.isEmpty()) {
                // 首次订阅
                message.put("type", "market");
                message.put("assets_ids", tokenIds);
                message.put("custom_feature_enabled", false);
            } else {
                // 增量订阅
                message.put("assets_ids", tokenIds);
                message.put("operation", "subscribe");
            }

            String json = objectMapper.writeValueAsString(message);
            wsClient.send(json);
            subscribedTokenIds.addAll(tokenIds);

            log.info("[subscribe][订阅成功，新增 {} 个，总计 {} 个]",
                    tokenIds.size(), subscribedTokenIds.size());
            // 记录日志
            wsLogService.logAsync(WsLogTypeEnum.SUBSCRIBE, json,
                    String.format("订阅成功，新增 %d 个，总计 %d 个", tokenIds.size(), subscribedTokenIds.size()));

        } catch (Exception e) {
            log.error("[subscribe][发送订阅消息失败]", e);
        }
    }

    /**
     * 取消订阅 Token IDs
     */
    public void unsubscribe(List<String> tokenIds) {
        if (status != WsStatusEnum.CONNECTED) {
            return;
        }

        if (tokenIds == null || tokenIds.isEmpty()) {
            return;
        }

        try {
            Map<String, Object> message = new HashMap<>();
            message.put("assets_ids", tokenIds);
            message.put("operation", "unsubscribe");

            String json = objectMapper.writeValueAsString(message);
            wsClient.send(json);
            subscribedTokenIds.removeAll(tokenIds);

            log.info("[unsubscribe][取消订阅 {} 个，剩余 {} 个]",
                    tokenIds.size(), subscribedTokenIds.size());
            // 记录日志
            wsLogService.logAsync(WsLogTypeEnum.UNSUBSCRIBE, json,
                    String.format("取消订阅 %d 个，剩余 %d 个", tokenIds.size(), subscribedTokenIds.size()));

        } catch (Exception e) {
            log.error("[unsubscribe][发送取消订阅消息失败]", e);
        }
    }

    /**
     * 处理连接打开
     */
    private void handleOpen() {
        status = WsStatusEnum.CONNECTED;
        connectedTime = LocalDateTime.now();
        lastHeartbeatTime = LocalDateTime.now();
        lastMessageTime = LocalDateTime.now();
        reconnectCount = 0;
        currentReconnectDelay = wsConfig.getReconnectInitialMs();

        log.info("[handleOpen][WebSocket 连接成功]");
        // 记录日志
        wsLogService.logAsync(WsLogTypeEnum.CONNECT, "WebSocket 连接成功");

        // 重连后重新订阅
        if (!subscribedTokenIds.isEmpty()) {
            List<String> tokenIds = new ArrayList<>(subscribedTokenIds);
            subscribedTokenIds.clear();
            subscribe(tokenIds);
        }
    }

    /**
     * 处理收到消息
     */
    private void handleMessage(String message) {
        lastMessageTime = LocalDateTime.now();

        try {
            wsMessageHandler.handleMessage(message);
        } catch (Exception e) {
            log.error("[handleMessage][处理消息失败: {}]", message, e);
        }
    }

    /**
     * 处理连接关闭
     */
    private void handleClose(int code, String reason, boolean remote) {
        log.warn("[handleClose][WebSocket 连接关闭 code={}, reason={}, remote={}]", code, reason, remote);
        status = WsStatusEnum.DISCONNECTED;
        // 记录日志
        wsLogService.logAsync(WsLogTypeEnum.DISCONNECT,
                String.format("code=%d, reason=%s, remote=%s", code, reason, remote));
        scheduleReconnect();
    }

    /**
     * 处理错误
     */
    private void handleError(Exception ex) {
        log.error("[handleError][WebSocket 错误]", ex);
        status = WsStatusEnum.STALE;
        // 记录日志
        wsLogService.logAsync(WsLogTypeEnum.ERROR, ex.getMessage());
    }

    /**
     * 调度重连
     */
    private void scheduleReconnect() {
        if (status == WsStatusEnum.RECONNECTING) {
            return;
        }

        status = WsStatusEnum.RECONNECTING;
        reconnectCount++;

        log.info("[scheduleReconnect][{}ms 后重连，第 {} 次]", currentReconnectDelay, reconnectCount);
        // 记录日志
        wsLogService.logAsync(WsLogTypeEnum.RECONNECT,
                String.format("%dms 后重连，第 %d 次", currentReconnectDelay, reconnectCount));

        reconnectScheduler.schedule(() -> {
            try {
                connect();
                // 重连成功后重新订阅
                if (status == WsStatusEnum.CONNECTED) {
                    subscribeAllActiveMarkets();
                }
            } catch (Exception e) {
                log.error("[scheduleReconnect][重连失败]", e);
                // 指数退避
                currentReconnectDelay = Math.min(currentReconnectDelay * 2, wsConfig.getReconnectMaxMs());
                scheduleReconnect();
            }
        }, currentReconnectDelay, TimeUnit.MILLISECONDS);
    }

    /**
     * 心跳检查（每 10 秒）
     */
    @Scheduled(fixedRate = 10000)
    public void heartbeatCheck() {
        if (status != WsStatusEnum.CONNECTED) {
            return;
        }

        // 发送 ping
        if (wsClient != null && wsClient.isOpen()) {
            try {
                wsClient.sendPing();
                lastHeartbeatTime = LocalDateTime.now();
            } catch (Exception e) {
                log.warn("[heartbeatCheck][发送心跳失败]", e);
            }
        }

        // 检查心跳超时
        if (lastHeartbeatTime != null) {
            long seconds = Duration.between(lastHeartbeatTime, LocalDateTime.now()).getSeconds();
            if (seconds > wsConfig.getStaleThresholdSec()) {
                log.warn("[heartbeatCheck][心跳超时 {} 秒]", seconds);
                status = WsStatusEnum.STALE;
            }
        }

        // 检查消息超时
        if (lastMessageTime != null) {
            long seconds = Duration.between(lastMessageTime, LocalDateTime.now()).getSeconds();
            if (seconds > wsConfig.getMessageTimeoutSec()) {
                log.warn("[heartbeatCheck][消息超时 {} 秒]", seconds);
            }
        }
    }

    /**
     * 获取状态信息
     */
    public WsStatusVO getStatusInfo() {
        WsStatusVO vo = new WsStatusVO();
        vo.setStatus(status.name());
        vo.setLastHeartbeatTime(lastHeartbeatTime);
        vo.setLastMessageTime(lastMessageTime);
        vo.setReconnectCount(reconnectCount);
        vo.setSubscribedCount(subscribedTokenIds.size());

        if (connectedTime != null && status == WsStatusEnum.CONNECTED) {
            vo.setConnectedDurationSec(Duration.between(connectedTime, LocalDateTime.now()).getSeconds());
        }

        return vo;
    }

    /**
     * 获取已订阅的 Token IDs
     */
    public Set<String> getSubscribedTokenIds() {
        return Collections.unmodifiableSet(subscribedTokenIds);
    }

    /**
     * 获取交易中市场未订阅的 Token IDs
     */
    public Set<String> getUnsubscribedTokenIds() {
        // 1. 获取所有交易中的市场
        List<PmMarketDO> tradingMarkets = pmMarketService.getTradingMarkets();

        // 2. 收集所有交易中市场的 Token IDs
        Set<String> allTokenIds = new HashSet<>();
        for (PmMarketDO market : tradingMarkets) {
            if (market.getClobTokenIds() != null) {
                allTokenIds.addAll(market.getClobTokenIds());
            }
        }

        // 3. 排除已订阅的 Token IDs
        Set<String> unsubscribed = new HashSet<>(allTokenIds);
        unsubscribed.removeAll(subscribedTokenIds);

        return unsubscribed;
    }

    /**
     * 是否已连接
     */
    public boolean isConnected() {
        return status == WsStatusEnum.CONNECTED;
    }

}
