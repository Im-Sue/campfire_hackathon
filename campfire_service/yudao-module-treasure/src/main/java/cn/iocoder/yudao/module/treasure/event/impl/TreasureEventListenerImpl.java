package cn.iocoder.yudao.module.treasure.event.impl;

import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.treasure.config.TreasureProperties;
import cn.iocoder.yudao.module.treasure.dal.dataobject.TreasureEventSyncDO;
import cn.iocoder.yudao.module.treasure.dal.mysql.TreasureEventSyncMapper;
import cn.iocoder.yudao.module.treasure.enums.EventTypeEnum;
import cn.iocoder.yudao.module.treasure.enums.SyncStatusEnum;
import cn.iocoder.yudao.module.treasure.event.TreasureEventListener;
import cn.iocoder.yudao.module.treasure.event.handler.EventHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Log;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Treasure 事件监听器实现类
 *
 * @author Sue
 */
@Slf4j
@Service
public class TreasureEventListenerImpl implements TreasureEventListener {

    /**
     * 区块确认数（避免链重组影响）
     */
    private static final int CONFIRMATION_BLOCKS = 12;

    @Resource
    private Web3j web3j;

    @Resource
    private TreasureProperties treasureProperties;

    @Resource
    private cn.iocoder.yudao.module.treasure.service.config.TreasureConfigService treasureConfigService;

    @Resource
    private TreasureEventSyncMapper eventSyncMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private Map<String, EventHandler> eventHandlers;

    @Resource
    private ObjectMapper objectMapper;

    private ScheduledExecutorService scheduler;

    private static final String LAST_SYNCED_BLOCK_KEY = "treasure:last_synced_block";

    // 事件签名常量
    private static final String POOL_CREATED_EVENT_SIGNATURE;
    private static final String TICKET_PURCHASED_EVENT_SIGNATURE;
    private static final String DRAW_STARTED_EVENT_SIGNATURE;
    private static final String DRAW_COMPLETED_EVENT_SIGNATURE;
    private static final String PRIZE_CLAIMED_EVENT_SIGNATURE;

    static {
        // PoolCreated(uint256 indexed poolId, uint256 price, uint256 totalShares, uint256 winnerCount, uint256 endTime)
        Event poolCreatedEvent = new Event("PoolCreated",
                Arrays.asList(
                        new TypeReference<Uint256>(true) {},
                        new TypeReference<Uint256>() {},
                        new TypeReference<Uint256>() {},
                        new TypeReference<Uint256>() {},
                        new TypeReference<Uint256>() {}
                ));
        POOL_CREATED_EVENT_SIGNATURE = EventEncoder.encode(poolCreatedEvent);

        // TicketPurchased(uint256 indexed poolId, address indexed buyer, uint256 ticketIndex)
        Event ticketPurchasedEvent = new Event("TicketPurchased",
                Arrays.asList(
                        new TypeReference<Uint256>(true) {},
                        new TypeReference<Address>(true) {},
                        new TypeReference<Uint256>() {}
                ));
        TICKET_PURCHASED_EVENT_SIGNATURE = EventEncoder.encode(ticketPurchasedEvent);

        // DrawStarted(uint256 indexed poolId, bytes32 requestId)
        Event drawStartedEvent = new Event("DrawStarted",
                Arrays.asList(
                        new TypeReference<Uint256>(true) {},
                        new TypeReference<Bytes32>() {}
                ));
        DRAW_STARTED_EVENT_SIGNATURE = EventEncoder.encode(drawStartedEvent);

        // DrawCompleted(uint256 indexed poolId, address[] winners, uint256 prizePerWinner)
        Event drawCompletedEvent = new Event("DrawCompleted",
                Arrays.asList(
                        new TypeReference<Uint256>(true) {},
                        new TypeReference<DynamicArray<Address>>() {},
                        new TypeReference<Uint256>() {}
                ));
        DRAW_COMPLETED_EVENT_SIGNATURE = EventEncoder.encode(drawCompletedEvent);

        // PrizeClaimed(uint256 indexed poolId, address indexed winner, uint256 amount)
        Event prizeClaimedEvent = new Event("PrizeClaimed",
                Arrays.asList(
                        new TypeReference<Uint256>(true) {},
                        new TypeReference<Address>(true) {},
                        new TypeReference<Uint256>() {}
                ));
        PRIZE_CLAIMED_EVENT_SIGNATURE = EventEncoder.encode(prizeClaimedEvent);
    }

    @PostConstruct
    public void init() {
        if (treasureProperties.getEventSync().getEnabled()) {  // 启动时仍用 YAML，因为 DB 可能还没初始化
            log.info("初始化 Treasure 事件监听器");
            // 补偿回放放到独立线程，避免在 @PostConstruct 中阻塞 Bean 创建导致死锁
            Thread replayThread = new Thread(() -> {
                try {
                    // 等待 Spring 上下文完全初始化
                    Thread.sleep(5000);
                    log.info("开始执行启动补偿回放...");
                    TenantUtils.execute(1L, this::compensateReplay);
                    log.info("启动补偿回放完成");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("补偿回放线程被中断");
                } catch (Exception e) {
                    log.error("启动补偿回放失败", e);
                }
            }, "treasure-compensate-replay");
            replayThread.setDaemon(true);
            replayThread.start();
            start();
        } else {
            log.info("Treasure 事件监听器已禁用");
        }
    }

    @PreDestroy
    public void destroy() {
        stop();
    }

    @Override
    public void start() {
        log.info("启动 Treasure 事件监听器");

        // 创建定时任务线程池
        scheduler = new ScheduledThreadPoolExecutor(1, r -> {
            Thread thread = new Thread(r, "treasure-event-listener");
            thread.setDaemon(true);
            return thread;
        });

        // 定时扫描事件
        Long interval = treasureConfigService.getEventSyncInterval();
        scheduler.scheduleWithFixedDelay(() -> TenantUtils.execute(1L, this::scanAndProcessEvents), 0, interval, TimeUnit.MILLISECONDS);

        log.info("Treasure 事件监听器已启动，扫描间隔: {}ms", interval);
    }

    @Override
    public void stop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            log.info("停止 Treasure 事件监听器");
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 扫描并处理事件（定时任务调用）
     */
    private void scanAndProcessEvents() {
        try {
            // 获取安全区块高度（最新区块 - 确认块数）
            BigInteger latestBlock = web3j.ethBlockNumber().send().getBlockNumber();
            BigInteger safeBlock = latestBlock.subtract(BigInteger.valueOf(CONFIRMATION_BLOCKS));
            if (safeBlock.compareTo(BigInteger.ZERO) < 0) {
                safeBlock = BigInteger.ZERO;
            }

            // 获取上次同步的区块高度
            BigInteger lastSyncedBlock = getLastSyncedBlock();

            // 如果是首次同步，从配置的起始区块开始
            if (lastSyncedBlock.equals(BigInteger.ZERO)) {
                Long startBlock = treasureProperties.getEventSync().getStartBlock();  // 首次同步用 YAML 配置
                if (startBlock > 0) {
                    lastSyncedBlock = BigInteger.valueOf(startBlock - 1);
                } else {
                    // 从安全区块开始
                    lastSyncedBlock = safeBlock;
                }
            }

            // 计算本次扫描的区块范围
            BigInteger fromBlock = lastSyncedBlock.add(BigInteger.ONE);
            Integer batchSize = treasureConfigService.getBlockBatchSize();
            BigInteger toBlock = fromBlock.add(BigInteger.valueOf(batchSize - 1));

            // 不超过安全区块
            if (toBlock.compareTo(safeBlock) > 0) {
                toBlock = safeBlock;
            }

            // 如果没有新区块，跳过
            if (fromBlock.compareTo(toBlock) > 0) {
                return;
            }

            log.info("扫描区块范围: {} - {}", fromBlock, toBlock);

            // 扫描事件
            List<Log> logs = scanEvents(fromBlock, toBlock);

            log.info("扫描到 {} 个事件", logs.size());

            // 处理事件
            for (Log eventLog : logs) {
                try {
                    processEvent(eventLog);
                } catch (Exception e) {
                    log.error("处理事件失败: txHash={}, logIndex={}", eventLog.getTransactionHash(), eventLog.getLogIndex(), e);
                }
            }

            // 更新最新同步区块
            updateLastSyncedBlock(toBlock);

        } catch (Exception e) {
            log.error("扫描事件失败", e);
        }
    }

    /**
     * 扫描事件日志
     */
    @Override
    public List<Log> scanEvents(BigInteger fromBlock, BigInteger toBlock) throws Exception {
        String contractAddress = treasureConfigService.getContractAddress();

        // 创建事件过滤器
        EthFilter filter = new EthFilter(
                DefaultBlockParameter.valueOf(fromBlock),
                DefaultBlockParameter.valueOf(toBlock),
                contractAddress
        );

        // 获取事件日志
        EthLog ethLog = web3j.ethGetLogs(filter).send();

        // 检查 RPC 调用是否返回错误
        if (ethLog.hasError()) {
            int errorCode = ethLog.getError().getCode();
            String errorMsg = ethLog.getError().getMessage();
            log.error("获取事件日志失败: code={}, message={}, address={}", errorCode, errorMsg, contractAddress);

            // Monad 等链的节点可能不保留历史数据，查询旧区块会返回 -32602
            if (errorCode == -32602) {
                try {
                    BigInteger latestBlock = web3j.ethBlockNumber().send().getBlockNumber();
                    BigInteger safeBlock = latestBlock.subtract(BigInteger.valueOf(CONFIRMATION_BLOCKS));
                    log.warn("区块范围 {} - {} 不可用，直接跳到最新安全区块: {}", fromBlock, toBlock, safeBlock);
                    updateLastSyncedBlock(safeBlock);
                } catch (Exception ex) {
                    log.error("获取最新区块号失败，仅跳过当前批次", ex);
                    updateLastSyncedBlock(toBlock);
                }
            }
            return new ArrayList<>();
        }

        List<Log> logs = new ArrayList<>();
        List<EthLog.LogResult> logResults = ethLog.getLogs();
        if (logResults != null) {
            for (EthLog.LogResult<?> logResult : logResults) {
                if (logResult instanceof EthLog.LogObject) {
                    logs.add(((EthLog.LogObject) logResult).get());
                }
            }
        }

        return logs;
    }

    @Override
    public void processEvent(Log eventLog) throws Exception {
        String contractAddress = treasureConfigService.getContractAddress();
        Integer chainId = treasureConfigService.getChainId();

        // 检查是否已处理
        TreasureEventSyncDO existingEvent = eventSyncMapper.selectByTxHashAndLogIndex(
                eventLog.getTransactionHash(),
                eventLog.getLogIndex().intValue(),
                contractAddress,
                chainId
        );

        if (existingEvent != null) {
            log.debug("事件已处理，跳过: txHash={}, logIndex={}", eventLog.getTransactionHash(), eventLog.getLogIndex());
            return;
        }

        // 解析事件类型
        String eventSignature = eventLog.getTopics().get(0);
        EventTypeEnum eventType = getEventType(eventSignature);

        if (eventType == null) {
            log.warn("未知的事件类型: signature={}", eventSignature);
            return;
        }

        // 保存事件到数据库
        TreasureEventSyncDO eventSync = TreasureEventSyncDO.builder()
                .contractAddress(contractAddress)
                .chainId(chainId)
                .eventType(eventType.getEventName())
                .txHash(eventLog.getTransactionHash())
                .blockNumber(eventLog.getBlockNumber().longValue())
                .logIndex(eventLog.getLogIndex().intValue())
                .eventData(objectMapper.writeValueAsString(eventLog))
                .syncStatus(SyncStatusEnum.PENDING.getStatus())
                .retryCount(0)
                .build();


        eventSyncMapper.insert(eventSync);

        log.info("保存事件: type={}, txHash={}, logIndex={}", eventType.getEventName(), eventLog.getTransactionHash(), eventLog.getLogIndex());

        // 调用事件处理器
        EventHandler handler = eventHandlers.get(eventType.getEventName() + "Handler");
        if (handler != null) {
            try {
                // 更新状态为处理中
                eventSync.setSyncStatus(SyncStatusEnum.PROCESSING.getStatus());
                eventSyncMapper.updateById(eventSync);

                // 处理事件
                handler.handle(eventLog);

                // 更新状态为已完成
                eventSync.setSyncStatus(SyncStatusEnum.COMPLETED.getStatus());
                eventSync.setProcessedTime(LocalDateTime.now());
                eventSyncMapper.updateById(eventSync);

                log.info("事件处理成功: type={}, txHash={}", eventType.getEventName(), eventLog.getTransactionHash());

            } catch (Exception e) {
                log.error("事件处理失败: type={}, txHash={}", eventType.getEventName(), eventLog.getTransactionHash(), e);

                // 更新状态为失败
                eventSync.setSyncStatus(SyncStatusEnum.FAILED.getStatus());
                eventSync.setRetryCount(eventSync.getRetryCount() + 1);
                eventSync.setErrorMessage(e.getMessage());
                eventSyncMapper.updateById(eventSync);
            }
        } else {
            log.warn("未找到事件处理器: type={}", eventType.getEventName());
        }
    }

    @Override
    public BigInteger getLastSyncedBlock() {
        String value = stringRedisTemplate.opsForValue().get(LAST_SYNCED_BLOCK_KEY);
        if (value != null) {
            return new BigInteger(value);
        }

        // 从数据库查询
        String contractAddress = treasureConfigService.getContractAddress();
        Integer chainId = treasureConfigService.getChainId();
        Long maxBlock = eventSyncMapper.selectMaxBlockNumber(contractAddress, chainId);

        if (maxBlock != null) {
            return BigInteger.valueOf(maxBlock);
        }

        return BigInteger.ZERO;
    }

    @Override
    public void updateLastSyncedBlock(BigInteger blockNumber) {
        stringRedisTemplate.opsForValue().set(LAST_SYNCED_BLOCK_KEY, blockNumber.toString());
    }

    /**
     * 启动补偿回放（分批扫描，每批不超过 blockBatchSize）
     */
    private void compensateReplay() {
        try {
            BigInteger latestBlock = web3j.ethBlockNumber().send().getBlockNumber();
            BigInteger safeBlock = latestBlock.subtract(BigInteger.valueOf(CONFIRMATION_BLOCKS));
            if (safeBlock.compareTo(BigInteger.ZERO) < 0) {
                safeBlock = BigInteger.ZERO;
            }

            BigInteger lastSyncedBlock = getLastSyncedBlock();
            BigInteger fromBlock;
            if (lastSyncedBlock.compareTo(BigInteger.ZERO) > 0) {
                fromBlock = lastSyncedBlock.add(BigInteger.ONE);
            } else {
                Long startBlock = treasureProperties.getEventSync().getStartBlock();  // 首次同步用 YAML 配置
                fromBlock = startBlock != null && startBlock > 0
                        ? BigInteger.valueOf(startBlock)
                        : safeBlock;
            }

            if (fromBlock.compareTo(safeBlock) > 0) {
                return;
            }

            // 分批扫描，每批不超过 blockBatchSize（Monad 限制 eth_getLogs 最大 100 区块）
            Integer batchSize = treasureConfigService.getBlockBatchSize();
            log.info("执行启动补偿回放，区块范围: {} - {}，每批 {} 个区块", fromBlock, safeBlock, batchSize);

            BigInteger currentFrom = fromBlock;
            while (currentFrom.compareTo(safeBlock) <= 0) {
                BigInteger currentTo = currentFrom.add(BigInteger.valueOf(batchSize - 1));
                if (currentTo.compareTo(safeBlock) > 0) {
                    currentTo = safeBlock;
                }

                List<Log> logs = scanEvents(currentFrom, currentTo);
                for (Log eventLog : logs) {
                    try {
                        processEvent(eventLog);
                    } catch (Exception e) {
                        log.error("补偿回放处理事件失败: txHash={}, logIndex={}", eventLog.getTransactionHash(), eventLog.getLogIndex(), e);
                    }
                }

                updateLastSyncedBlock(currentTo);
                currentFrom = currentTo.add(BigInteger.ONE);
            }

            log.info("启动补偿回放完成");
        } catch (Exception e) {
            log.error("启动补偿回放失败", e);
        }
    }

    @Override
    public void replayOnce() {
        compensateReplay();
    }

    /**
     * 根据事件签名获取事件类型
     */
    private EventTypeEnum getEventType(String signature) {
        if (signature.equals(POOL_CREATED_EVENT_SIGNATURE)) {
            return EventTypeEnum.POOL_CREATED;
        }
        if (signature.equals(TICKET_PURCHASED_EVENT_SIGNATURE)) {
            return EventTypeEnum.TICKET_PURCHASED;
        }
        if (signature.equals(DRAW_STARTED_EVENT_SIGNATURE)) {
            return EventTypeEnum.DRAW_STARTED;
        }
        if (signature.equals(DRAW_COMPLETED_EVENT_SIGNATURE)) {
            return EventTypeEnum.DRAW_COMPLETED;
        }
        if (signature.equals(PRIZE_CLAIMED_EVENT_SIGNATURE)) {
            return EventTypeEnum.PRIZE_CLAIMED;
        }

        return null;
    }
}
