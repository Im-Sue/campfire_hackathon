package cn.iocoder.yudao.module.ai.service.agent.orchestrator;

import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiAgentDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiEventRoomDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiEventRoomParticipantDO;
import cn.iocoder.yudao.module.ai.dal.mysql.agent.AiAgentMapper;
import cn.iocoder.yudao.module.ai.dal.mysql.agent.AiEventRoomMapper;
import cn.iocoder.yudao.module.ai.dal.mysql.agent.AiEventRoomParticipantMapper;
import cn.iocoder.yudao.module.ai.enums.agent.AiEventRoomStatusEnum;
import cn.iocoder.yudao.module.ai.service.agent.event.RoomLifecycleEvent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 房间编排器
 * 负责管理房间线程、执行讨论轮次
 *
 * @author campfire
 */
@Component
@Slf4j
public class RoomOrchestrator {

    @Resource
    private AiEventRoomMapper roomMapper;

    @Resource
    private AiEventRoomParticipantMapper participantMapper;

    @Resource
    private AiAgentMapper agentMapper;

    @Resource
    private cn.iocoder.yudao.module.ai.service.agent.AiAgentService agentService;

    @Resource
    private List<DiscussionPhase> phases;

    @Resource(name = "agentRoomExecutor")
    private ThreadPoolTaskExecutor roomExecutor;

    /**
     * 正在运行的房间
     */
    private final Map<Long, Boolean> runningRooms = new ConcurrentHashMap<>();

    /**
     * 启动房间
     */
    public void startRoom(Long roomId) {
        if (runningRooms.containsKey(roomId)) {
            log.warn("  ⚠️  [Orchestrator] 房间 {} 已在运行中，跳过启动", roomId);
            return;
        }

        log.info("  🎬 [Orchestrator] 准备启动房间 {} 的主循环线程...", roomId);
        runningRooms.put(roomId, true);
        roomExecutor.submit(() -> runRoomLoop(roomId));
        log.info("  ✅ [Orchestrator] 房间 {} 主循环线程已提交到线程池", roomId);
    }

    /**
     * 暂停房间
     */
    public void pauseRoom(Long roomId) {
        runningRooms.remove(roomId);
        log.info("  ⏸️  [Orchestrator] 房间 {} 已从运行列表移除（暂停）", roomId);
    }

    /**
     * 停止房间
     */
    public void stopRoom(Long roomId) {
        runningRooms.remove(roomId);
        log.info("  🛑 [Orchestrator] 房间 {} 已从运行列表移除（停止）", roomId);
    }

    /**
     * 房间主循环
     */
    private void runRoomLoop(Long roomId) {
        log.info("\n╔══════════════════════════════════════════════════════════════════╗");
        log.info("║  🎯 竞赛房间 [{}] 启动                                            ║", roomId);
        log.info("╚══════════════════════════════════════════════════════════════════╝");

        try {
            while (runningRooms.containsKey(roomId)) {
                // 1. 获取房间信息 (直接从数据库获取,解耦!)
                AiEventRoomDO room = roomMapper.selectById(roomId);
                if (room == null || !AiEventRoomStatusEnum.RUNNING.getStatus().equals(room.getStatus())) {
                    log.info("⚠️ [Room-{}] 房间状态异常或已停止,退出循环", roomId);
                    break;
                }

                // 2. 构建上下文
                RoomContext context = buildContext(room);

                // 3. 检查是否所有市场都已封盘
                if (context.isAllMarketsClosed()) {
                    log.info("🏁 [Room-{}] 所有市场已封盘，竞赛结束", roomId);
                    break;
                }

                log.info("\n┌──────────────────────────────────────────────────────────────────┐");
                log.info("│  📢 第 {} 轮讨论开始   房间ID: {}                              │", 
                        context.getCurrentRound() + 1, roomId);
                log.info("└──────────────────────────────────────────────────────────────────┘");

                // 4. 执行单轮讨论
                executeRound(context);

                // 5. 更新轮次 (直接更新数据库,解耦!)
                AiEventRoomDO updateObj = new AiEventRoomDO();
                updateObj.setId(roomId);
                updateObj.setCurrentRound(context.getCurrentRound() + 1);
                roomMapper.updateById(updateObj);

                // 6. 等待间隔
                long intervalMs = room.getDiscussionInterval() * 60 * 1000L;
                log.info("\n✅ [Room-{}] 第 {} 轮完成，休息 {} 分钟后开始下一轮...\n", 
                        roomId, context.getCurrentRound() + 1, room.getDiscussionInterval());
                Thread.sleep(intervalMs);
            }
        } catch (InterruptedException e) {
            log.info("🛑 [Room-{}] 线程被中断", roomId);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("❌ [Room-{}] 运行异常", roomId, e);
        } finally {
            runningRooms.remove(roomId);
            log.info("\n╔══════════════════════════════════════════════════════════════════╗");
            log.info("║  🏁 竞赛房间 [{}] 运行结束                                        ║", roomId);
            log.info("╚══════════════════════════════════════════════════════════════════╝\n");
        }
    }

    /**
     * 执行单轮讨论
     */
    public void executeRound(RoomContext context) {
        log.info("[executeRound] 房间 {} 开始第 {} 轮讨论", 
                context.getRoom().getId(), context.getCurrentRound() + 1);

        // 按顺序执行各阶段
        List<DiscussionPhase> sortedPhases = phases.stream()
                .sorted(Comparator.comparingInt(DiscussionPhase::getOrder))
                .toList();

        for (DiscussionPhase phase : sortedPhases) {
            try {
                log.info("[executeRound] 执行阶段: {}", phase.getName());
                phase.execute(context);
            } catch (Exception e) {
                log.error("[executeRound] 阶段 {} 执行失败", phase.getName(), e);
                // 失败不中断，继续下一阶段
            }
        }
    }

    /**
     * 构建房间上下文
     */
    private RoomContext buildContext(AiEventRoomDO room) {
        RoomContext context = new RoomContext();
        context.setRoom(room);
        context.setEventId(room.getEventId());
        context.setCurrentRound(room.getCurrentRound());

        // 获取参与者和Agent信息 (直接从数据库获取,解耦!)
        List<AiEventRoomParticipantDO> participants = participantMapper.selectListByRoomId(room.getId());
        context.setParticipants(participants);

        // 直接使用 Mapper 获取 Agent 信息，避免依赖 Service
        List<Long> agentIds = participants.stream()
                .map(AiEventRoomParticipantDO::getAgentId)
                .toList();
        List<AiAgentDO> agents = agentMapper.selectBatchIds(agentIds);
        context.setAgents(agents);

        // 填充余额信息 (直接查询wallet_user的实时余额)
        Map<Long, Long> balances = new HashMap<>();
        for (AiEventRoomParticipantDO participant : participants) {
            // 直接查询Agent的实时余额（从wallet_user体系获取）
            Long currentBalance = agentService.getAvailableBalance(participant.getAgentId());
            balances.put(participant.getAgentId(), currentBalance);
        }
        context.setAgentBalances(balances);

        return context;
    }

}
