package cn.iocoder.yudao.module.ai.service.agent;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiEventRoomCreateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiEventRoomPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiEventRoomDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiEventRoomParticipantDO;
import cn.iocoder.yudao.module.ai.dal.mysql.agent.AiEventRoomMapper;
import cn.iocoder.yudao.module.ai.dal.mysql.agent.AiEventRoomParticipantMapper;
import cn.iocoder.yudao.module.ai.enums.agent.AiEventRoomStatusEnum;
import cn.iocoder.yudao.module.ai.service.agent.dto.AgentParticipantInfo;
import cn.iocoder.yudao.module.ai.service.agent.event.RoomLifecycleEvent;
import cn.iocoder.yudao.module.market.dal.dataobject.event.PmEventDO;
import cn.iocoder.yudao.module.market.service.event.PmEventService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;

/**
 * AI 事件房间 Service 实现类
 *
 * @author campfire
 */
@Service
@Validated
@Slf4j
public class AiEventRoomServiceImpl implements AiEventRoomService {

    @Resource
    private AiEventRoomMapper roomMapper;

    @Resource
    private AiEventRoomParticipantMapper participantMapper;

    @Resource
    private ApplicationEventPublisher eventPublisher;

    @Resource
    private PmEventService eventService;

    @Resource
    private AiAgentService agentService;

    @Resource
    private cn.iocoder.yudao.module.market.service.market.PmMarketService marketService;

    @Resource
    private cn.iocoder.yudao.module.market.service.price.PmPriceService priceService;

    @Resource
    private cn.iocoder.yudao.module.ai.dal.mysql.agent.AiEventRoomOrderMapper orderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRoom(AiEventRoomCreateReqVO createReqVO, List<AgentParticipantInfo> participantInfos) {
        // 1. 校验事件是否已有房间
        AiEventRoomDO existRoom = roomMapper.selectByEventId(createReqVO.getEventId());
        if (existRoom != null) {
            throw exception(AI_EVENT_ROOM_ALREADY_EXISTS);
        }

        // 2. 校验事件状态
        PmEventDO event = eventService.getEvent(createReqVO.getEventId());
        if (event == null) {
            throw exception(AI_EVENT_NOT_EXISTS);
        }
        // 事件必须已上架才能创建AI讨论房间
        if (event.getStatus() != 1) { // 1=已上架
            throw exception(AI_EVENT_NOT_TRADING);
        }

        // 3. 校验参与者数量
        if (participantInfos.size() < 2) {
            throw exception(AI_AGENT_COUNT_NOT_ENOUGH);
        }

        // 4. 创建房间
        AiEventRoomDO room = AiEventRoomDO.builder()
                .eventId(createReqVO.getEventId())
                .status(AiEventRoomStatusEnum.PENDING.getStatus())
                .currentRound(0)
                .discussionInterval(createReqVO.getDiscussionInterval() != null ? createReqVO.getDiscussionInterval() : 5)
                .build();
        roomMapper.insert(room);

        // 5. 创建参与者记录 (使用传入的余额信息)
        for (AgentParticipantInfo info : participantInfos) {
            AiEventRoomParticipantDO participant = AiEventRoomParticipantDO.builder()
                    .roomId(room.getId())
                    .agentId(info.getAgentId())
                    .initialBalance(info.getInitialBalance())
                    .profit(0L)
                    .joinTime(LocalDateTime.now())
                    .deleted(false)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();
            participantMapper.insert(participant);
        }

        log.info("[createRoom] 创建房间成功, roomId={}, eventId={}, participants={}",
                room.getId(), createReqVO.getEventId(), participantInfos.size());

        // 6. 是否立即开始
        if (Boolean.TRUE.equals(createReqVO.getStartImmediately())) {
            startRoom(room.getId());
        }

        return room.getId();
    }

    @Override
    public void startRoom(Long roomId) {
        AiEventRoomDO room = validateRoomExists(roomId);
        if (!AiEventRoomStatusEnum.PENDING.getStatus().equals(room.getStatus()) &&
            !AiEventRoomStatusEnum.PAUSED.getStatus().equals(room.getStatus())) {
            throw exception(AI_EVENT_ROOM_STATUS_ERROR);
        }

        // 更新状态
        AiEventRoomDO updateObj = new AiEventRoomDO();
        updateObj.setId(roomId);
        updateObj.setStatus(AiEventRoomStatusEnum.RUNNING.getStatus());
        updateObj.setStartTime(LocalDateTime.now());
        roomMapper.updateById(updateObj);

        // 发布房间启动事件 (解耦!)
        eventPublisher.publishEvent(new RoomLifecycleEvent(this, roomId, RoomLifecycleEvent.RoomAction.START));
        log.info("[startRoom] 启动房间 {}", roomId);
    }

    @Override
    public void pauseRoom(Long roomId) {
        AiEventRoomDO room = validateRoomExists(roomId);
        if (!AiEventRoomStatusEnum.RUNNING.getStatus().equals(room.getStatus())) {
            throw exception(AI_EVENT_ROOM_STATUS_ERROR);
        }

        AiEventRoomDO updateObj = new AiEventRoomDO();
        updateObj.setId(roomId);
        updateObj.setStatus(AiEventRoomStatusEnum.PAUSED.getStatus());
        roomMapper.updateById(updateObj);

        // 发布房间暂停事件 (解耦!)
        eventPublisher.publishEvent(new RoomLifecycleEvent(this, roomId, RoomLifecycleEvent.RoomAction.PAUSE));
        log.info("[pauseRoom] 暂停房间 {}", roomId);
    }

    @Override
    public void stopRoom(Long roomId) {
        AiEventRoomDO room = validateRoomExists(roomId);

        AiEventRoomDO updateObj = new AiEventRoomDO();
        updateObj.setId(roomId);
        updateObj.setStatus(AiEventRoomStatusEnum.FINISHED.getStatus());
        updateObj.setEndTime(LocalDateTime.now());
        roomMapper.updateById(updateObj);

        // 发布房间停止事件 (解耦!)
        eventPublisher.publishEvent(new RoomLifecycleEvent(this, roomId, RoomLifecycleEvent.RoomAction.STOP));
        log.info("[stopRoom] 停止房间 {}", roomId);
    }

    @Override
    public AiEventRoomDO getRoom(Long id) {
        return roomMapper.selectById(id);
    }

    @Override
    public AiEventRoomDO getRoomByEventId(Long eventId) {
        return roomMapper.selectByEventId(eventId);
    }

    @Override
    public PageResult<AiEventRoomDO> getRoomPage(AiEventRoomPageReqVO pageReqVO) {
        return roomMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiEventRoomRespVO> getRoomPageWithDetails(AiEventRoomPageReqVO pageReqVO) {
        // 1. 先获取原始分页数据
        PageResult<AiEventRoomDO> pageResult = roomMapper.selectPage(pageReqVO);

        // 2. 转换为带详情的VO列表
        List<cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiEventRoomRespVO> voList = new java.util.ArrayList<>();
        for (AiEventRoomDO room : pageResult.getList()) {
            cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiEventRoomRespVO vo = getRoomWithDetails(room.getId());
            if (vo != null) {
                voList.add(vo);
            }
        }

        // 3. 返回带详情的分页结果
        return new PageResult<>(voList, pageResult.getTotal());
    }

    @Override
    public List<AiEventRoomParticipantDO> getRoomParticipants(Long roomId) {
        return participantMapper.selectListByRoomId(roomId);
    }

    @Override
    public void updateCurrentRound(Long roomId, Integer round) {
        AiEventRoomDO updateObj = new AiEventRoomDO();
        updateObj.setId(roomId);
        updateObj.setCurrentRound(round);
        roomMapper.updateById(updateObj);
    }

    @Override
    @cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore
    public void recoverRoomsOnStartup() {
        // 1. 查询所有运行中的房间
        List<AiEventRoomDO> runningRooms = roomMapper.selectListByStatus(AiEventRoomStatusEnum.RUNNING.getStatus());

        if (runningRooms.isEmpty()) {
            log.info("┌──────────────────────────────────────────────────────────────────┐");
            log.info("│  ℹ️  没有需要恢复的房间                                            │");
            log.info("└──────────────────────────────────────────────────────────────────┘");
            return;
        }

        log.info("┌──────────────────────────────────────────────────────────────────┐");
        log.info("│  📋 发现 {} 个运行中的房间需要恢复                                 │", runningRooms.size());
        log.info("└──────────────────────────────────────────────────────────────────┘");

        // 2. 逐个恢复房间
        int successCount = 0;
        int failCount = 0;

        for (AiEventRoomDO room : runningRooms) {
            try {
                log.info("  → 正在恢复房间 [ID: {}, 事件ID: {}, 当前轮次: {}]",
                        room.getId(), room.getEventId(), room.getCurrentRound());

                // 发布房间启动事件来恢复房间 (解耦!)
                eventPublisher.publishEvent(new RoomLifecycleEvent(this, room.getId(), RoomLifecycleEvent.RoomAction.START));

                // 等待一小段时间，让事件处理完成
                Thread.sleep(200);

                successCount++;
                log.info("  ✅ 房间 {} 恢复成功", room.getId());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("  ❌ 房间 {} 恢复被中断", room.getId(), e);
                failCount++;
            } catch (Exception e) {
                log.error("  ❌ 房间 {} 恢复失败", room.getId(), e);
                failCount++;
            }
        }

        // 3. 输出恢复结果统计
        log.info("┌──────────────────────────────────────────────────────────────────┐");
        log.info("│  📊 恢复结果统计:                                                  │");
        log.info("│     总数: {}  成功: {}  失败: {}                                   │",
                runningRooms.size(), successCount, failCount);
        log.info("└──────────────────────────────────────────────────────────────────┘");

        if (failCount > 0) {
            log.warn("⚠️  有 {} 个房间恢复失败，请检查日志排查原因", failCount);
        }
    }

    private AiEventRoomDO validateRoomExists(Long id) {
        AiEventRoomDO room = roomMapper.selectById(id);
        if (room == null) {
            throw exception(AI_EVENT_ROOM_NOT_EXISTS);
        }
        return room;
    }

    @Override
    public List<cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiEventRoomParticipantRespVO> getRoomParticipantsWithDetails(Long roomId) {
        // 1. 查询参与者记录
        List<AiEventRoomParticipantDO> participants = participantMapper.selectListByRoomId(roomId);

        // 2. 转换为VO并填充额外信息
        List<cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiEventRoomParticipantRespVO> result = new java.util.ArrayList<>();
        for (AiEventRoomParticipantDO participant : participants) {
            cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiEventRoomParticipantRespVO vo =
                cn.iocoder.yudao.framework.common.util.object.BeanUtils.toBean(participant,
                    cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiEventRoomParticipantRespVO.class);

            // 3. 关联查询Agent信息
            cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiAgentDO agent = agentService.getAgent(participant.getAgentId());
            if (agent != null) {
                vo.setAgentName(agent.getName());
                vo.setAgentAvatar(agent.getAvatar());
                vo.setPersonality(agent.getPersonality());

                // 4. 查询当前余额
                Long balance = agentService.getAvailableBalance(participant.getAgentId());
                vo.setBalance(balance);

                // 5. 计算状态（余额充足且Agent启用则为正常）
                vo.setStatus((balance != null && balance > 0 && agent.getStatus() == 0) ? 1 : 0);
            } else {
                vo.setStatus(0); // Agent不存在，标记为异常
            }

            // 6. 统计该Agent在此房间的下单数
            vo.setOrderCount(orderMapper.countByRoomIdAndAgentId(roomId, participant.getAgentId()));

            result.add(vo);
        }

        return result;
    }

    @Override
    public cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiEventRoomRespVO getRoomWithDetails(Long id) {
        // 1. 获取房间基本信息
        AiEventRoomDO room = getRoom(id);
        if (room == null) {
            return null;
        }

        // 2. 转换为VO
        cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiEventRoomRespVO vo =
            cn.iocoder.yudao.framework.common.util.object.BeanUtils.toBean(room,
                cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiEventRoomRespVO.class);

        // 3. 关联查询事件信息
        cn.iocoder.yudao.module.market.dal.dataobject.event.PmEventDO event = eventService.getEvent(room.getEventId());
        if (event != null) {
            vo.setEventTitle(event.getTitle());
            vo.setEventCoverUrl(event.getImageUrl());
            vo.setMarketCount(event.getMarketCount());
        }

        // 4. 查询参与者列表
        List<cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiEventRoomParticipantRespVO> participants =
            getRoomParticipantsWithDetails(id);
        vo.setParticipants(participants);

        // 5. 计算下一轮开始时间
        if (room.getStatus() == 1 && room.getStartTime() != null && room.getDiscussionInterval() != null) {
            long nextRoundMillis = room.getStartTime().plusMinutes((long) room.getCurrentRound() * room.getDiscussionInterval())
                .atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
            vo.setNextRoundTime(nextRoundMillis);
        }

        // 6. 统计交易数据
        vo.setTotalOrders(orderMapper.countByRoomId(id));
        vo.setTotalAmount(orderMapper.sumAmountByRoomId(id));
        // 计算总盈亏（各参与者盈亏之和）
        Long totalProfit = participants.stream()
                .filter(p -> p.getProfit() != null)
                .mapToLong(cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiEventRoomParticipantRespVO::getProfit)
                .sum();
        vo.setTotalProfit(totalProfit);

        return vo;
    }

    @Override
    public List<cn.iocoder.yudao.module.ai.controller.admin.agent.vo.RoomMarketRespVO> getRoomMarkets(Long roomId) {
        // 1. 获取房间信息
        AiEventRoomDO room = getRoom(roomId);
        if (room == null) {
            return new java.util.ArrayList<>();
        }

        // 2. 查询该事件下的所有市场
        List<cn.iocoder.yudao.module.market.dal.dataobject.market.PmMarketDO> markets =
            marketService.getMarketsByEventId(room.getEventId());

        // 3. 转换为VO
        List<cn.iocoder.yudao.module.ai.controller.admin.agent.vo.RoomMarketRespVO> result = new java.util.ArrayList<>();
        for (cn.iocoder.yudao.module.market.dal.dataobject.market.PmMarketDO market : markets) {
            // 跳过非交易状态的市场（避免对已结束/已结算的市场调用价格API导致超时）
            if (market.getStatus() != 1) {  // 1=TRADING
                log.info("[getRoomMarkets] 跳过非交易状态市场: marketId={}, status={}",
                        market.getId(), market.getStatus());
                continue;
            }

            cn.iocoder.yudao.module.ai.controller.admin.agent.vo.RoomMarketRespVO vo =
                new cn.iocoder.yudao.module.ai.controller.admin.agent.vo.RoomMarketRespVO();
            vo.setId(market.getId());
            vo.setQuestion(market.getQuestion());

            // 4. 获取所有选项的价格
            java.util.Map<Integer, cn.iocoder.yudao.module.market.service.price.PriceInfo> prices =
                priceService.getAllPrices(market.getId());

            // 5. 构建选项列表
            List<cn.iocoder.yudao.module.ai.controller.admin.agent.vo.MarketOutcomeVO> outcomes = new java.util.ArrayList<>();
            if (prices != null && !prices.isEmpty()) {
                for (java.util.Map.Entry<Integer, cn.iocoder.yudao.module.market.service.price.PriceInfo> entry : prices.entrySet()) {
                    cn.iocoder.yudao.module.market.service.price.PriceInfo priceInfo = entry.getValue();
                    if (priceInfo != null) {
                        cn.iocoder.yudao.module.ai.controller.admin.agent.vo.MarketOutcomeVO outcomeVO =
                            new cn.iocoder.yudao.module.ai.controller.admin.agent.vo.MarketOutcomeVO();
                        outcomeVO.setOutcomeName(priceInfo.getOutcomeName());
                        outcomeVO.setOutcomeIndex(priceInfo.getOutcomeIndex());
                        outcomeVO.setBestBid(priceInfo.getBestBid());
                        outcomeVO.setBestAsk(priceInfo.getBestAsk());
                        outcomeVO.setMidPrice(priceInfo.getMidPrice());
                        outcomes.add(outcomeVO);
                    }
                }
            }
            vo.setOutcomes(outcomes);
            result.add(vo);
        }

        return result;
    }

}
