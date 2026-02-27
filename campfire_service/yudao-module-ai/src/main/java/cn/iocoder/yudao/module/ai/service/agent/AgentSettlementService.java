package cn.iocoder.yudao.module.ai.service.agent;

import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiAgentDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiEventRoomDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiEventRoomMessageDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiEventRoomParticipantDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatRoleDO;
import cn.iocoder.yudao.module.ai.dal.mysql.agent.AiEventRoomMapper;
import cn.iocoder.yudao.module.ai.dal.mysql.agent.AiEventRoomParticipantMapper;
import cn.iocoder.yudao.module.ai.enums.agent.AiEventRoomMessageTypeEnum;
import cn.iocoder.yudao.module.ai.enums.agent.AiEventRoomStatusEnum;
import cn.iocoder.yudao.module.ai.service.model.AiChatRoleService;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import cn.iocoder.yudao.module.market.dal.dataobject.position.PmPositionDO;
import cn.iocoder.yudao.module.market.dal.dataobject.reward.PmRewardDO;
import cn.iocoder.yudao.module.market.service.market.PmMarketService;
import cn.iocoder.yudao.module.market.service.position.PmPositionService;
import cn.iocoder.yudao.module.market.service.reward.PmRewardService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AI Agent 结算服务
 * 处理市场结算后的Agent盈亏计算和战绩更新
 *
 * @author campfire
 */
@Service
@Slf4j
public class AgentSettlementService {

    @Resource
    private AiEventRoomMapper roomMapper;

    @Resource
    private AiEventRoomParticipantMapper participantMapper;

    @Resource
    private AiAgentService agentService;

    @Resource
    private AiEventRoomMessageService messageService;

    @Resource
    private AiChatRoleService chatRoleService;

    @Resource
    private AiModelService modelService;

    @Resource
    private PmMarketService marketService;

    @Resource
    private PmPositionService positionService;

    @Resource
    private PmRewardService rewardService;

    /**
     * 市场结算回调
     * 
     * @param marketId 市场ID
     * @param eventId 事件ID
     * @param winningOutcome 获胜结果 (Yes/No)
     */
    @Transactional(rollbackFor = Exception.class)
    public void onMarketSettled(Long marketId, Long eventId, String winningOutcome) {
        log.info("[onMarketSettled] 市场 {} 结算, 事件={}, 获胜结果={}", marketId, eventId, winningOutcome);

        // 1. 查找关联房间
        AiEventRoomDO room = roomMapper.selectByEventId(eventId);
        if (room == null) {
            log.info("[onMarketSettled] 事件 {} 没有关联的AI房间", eventId);
            return;
        }

        // 2. 获取房间参与者
        List<AiEventRoomParticipantDO> participants = participantMapper.selectListByRoomId(room.getId());

        // 3. 遍历参与者，计算盈亏
        for (AiEventRoomParticipantDO participant : participants) {
            try {
                processParticipantSettlement(room, participant, marketId, winningOutcome);
            } catch (Exception e) {
                log.error("[onMarketSettled] 参与者 {} 结算失败", participant.getAgentId(), e);
            }
        }

        // 4. 自动领取奖励（将获胜奖励返还到Agent积分账户）
        for (AiEventRoomParticipantDO participant : participants) {
            try {
                autoClaimRewards(participant.getAgentId());
            } catch (Exception e) {
                log.error("[onMarketSettled] Agent {} 自动领取奖励失败", participant.getAgentId(), e);
            }
        }

        // 5. 检查事件是否完全结算
        var markets = marketService.getMarketsByEventId(eventId);
        boolean allSettled = markets.stream().allMatch(m -> m.getStatus() == 3); // 3=SETTLED
        
        if (allSettled) {
            finalizeRoom(room);
        }
    }

    /**
     * 处理单个参与者的结算
     */
    private void processParticipantSettlement(AiEventRoomDO room, AiEventRoomParticipantDO participant,
                                               Long marketId, String winningOutcome) {
        Long agentId = participant.getAgentId();
        AiAgentDO agent = agentService.getAgent(agentId);
        if (agent == null) {
            return;
        }

        log.info("[processParticipantSettlement] 处理Agent {} 在市场 {} 的结算", agentId, marketId);

        // 1. 获取Agent在该市场的持仓
        Long walletUserId = agent.getWalletUserId();
        PmPositionDO winPosition = positionService.getPosition(walletUserId, marketId, winningOutcome);
        String losingOutcome = "Yes".equals(winningOutcome) ? "No" : "Yes";
        PmPositionDO losePosition = positionService.getPosition(walletUserId, marketId, losingOutcome);
        
        // 2. 计算盈亏
        Long profit = 0L;
        boolean hasWinPosition = winPosition != null && winPosition.getQuantity().compareTo(BigDecimal.ZERO) > 0;
        boolean hasLosePosition = losePosition != null && losePosition.getQuantity().compareTo(BigDecimal.ZERO) > 0;
        
        if (hasWinPosition) {
            // 持有获胜方向，盈利 = 持仓数量 * (1 - 平均成本)
            BigDecimal cost = winPosition.getTotalCost() != null ? 
                    new BigDecimal(winPosition.getTotalCost()) : BigDecimal.ZERO;
            BigDecimal revenue = winPosition.getQuantity(); // 每份获胜份额值1积分
            profit = revenue.subtract(cost).longValue();
        }
        
        if (hasLosePosition) {
            // 持有失败方向，亏损 = 总成本
            Long lossCost = losePosition.getTotalCost() != null ? losePosition.getTotalCost() : 0L;
            profit -= lossCost;
        }

        // 3. 更新参与者盈亏
        participant.setProfit(participant.getProfit() + profit);
        participantMapper.updateById(participant);

        // 4. 生成结算感言
        String settlementComment = generateSettlementComment(agent, profit, winningOutcome, 
                hasWinPosition, hasLosePosition);
        
        AiEventRoomMessageDO message = AiEventRoomMessageDO.builder()
                .roomId(room.getId())
                .agentId(agentId)
                .round(room.getCurrentRound())
                .messageType(AiEventRoomMessageTypeEnum.SETTLEMENT.getType())
                .content(settlementComment)
                .status(1)
                .createTime(LocalDateTime.now())
                .build();
        messageService.createMessage(message);

        log.info("[processParticipantSettlement] Agent {} 结算完成, 盈亏={}", agentId, profit);
    }

    /**
     * 自动领取Agent的待领取奖励
     * 将获胜奖励积分返还到Agent的积分账户
     */
    private void autoClaimRewards(Long agentId) {
        AiAgentDO agent = agentService.getAgent(agentId);
        if (agent == null) {
            return;
        }

        Long walletUserId = agent.getWalletUserId();
        List<PmRewardDO> pendingRewards = rewardService.getPendingRewardsByUserId(walletUserId);
        if (pendingRewards == null || pendingRewards.isEmpty()) {
            log.info("[autoClaimRewards] Agent {} 没有待领取的奖励", agentId);
            return;
        }

        int claimedCount = 0;
        long claimedAmount = 0L;
        for (PmRewardDO reward : pendingRewards) {
            try {
                rewardService.claimReward(walletUserId, reward.getId());
                claimedCount++;
                claimedAmount += reward.getRewardAmount();
            } catch (Exception e) {
                log.warn("[autoClaimRewards] Agent {} 领取奖励 {} 失败: {}",
                        agentId, reward.getId(), e.getMessage());
            }
        }

        log.info("[autoClaimRewards] Agent {} 自动领取奖励完成, 领取数量={}, 领取总额={}",
                agentId, claimedCount, claimedAmount);
    }

    /**
     * 生成结算感言（调用LLM）
     */
    private String generateSettlementComment(AiAgentDO agent, Long profit, String winningOutcome,
                                              boolean hasWinPosition, boolean hasLosePosition) {
        try {
            // 获取Agent关联的角色和模型
            AiChatRoleDO role = chatRoleService.getChatRole(agent.getRoleId());
            
            String systemPrompt = buildSettlementSystemPrompt(agent, role);
            String userPrompt = buildSettlementUserPrompt(profit, winningOutcome, hasWinPosition, hasLosePosition);
            
            return callLLM(role != null ? role.getModelId() : null, systemPrompt, userPrompt);
            
        } catch (Exception e) {
            log.warn("[generateSettlementComment] LLM生成感言失败, 使用默认模板", e);
            return getDefaultSettlementComment(agent, profit);
        }
    }

    private String buildSettlementSystemPrompt(AiAgentDO agent, AiChatRoleDO role) {
        StringBuilder sb = new StringBuilder();
        if (role != null && role.getSystemMessage() != null) {
            sb.append(role.getSystemMessage()).append("\n\n");
        }
        sb.append("你是 ").append(agent.getName());
        if (agent.getPersonality() != null) {
            sb.append("，").append(agent.getPersonality());
        }
        sb.append("\n\n请用简短的一两句话表达你对这次市场结算结果的感想，体现你的性格特点。");
        return sb.toString();
    }

    private String buildSettlementUserPrompt(Long profit, String winningOutcome, 
                                              boolean hasWinPosition, boolean hasLosePosition) {
        StringBuilder sb = new StringBuilder();
        sb.append("市场已结算，获胜方向是: ").append(winningOutcome).append("\n");
        
        if (!hasWinPosition && !hasLosePosition) {
            sb.append("你没有参与这个市场的交易。\n");
        } else if (profit > 0) {
            sb.append("你预测正确，获得了 ").append(profit).append(" 积分收益！\n");
        } else if (profit < 0) {
            sb.append("你预测失误，损失了 ").append(Math.abs(profit)).append(" 积分。\n");
        } else {
            sb.append("你的盈亏持平。\n");
        }
        
        sb.append("\n请发表一句简短的感言（30字以内）。");
        return sb.toString();
    }

    private String callLLM(Long modelId, String systemPrompt, String userPrompt) {
        ChatModel chatModel;
        if (modelId != null) {
            chatModel = modelService.getChatModel(modelId);
        } else {
            var defaultModel = modelService.getRequiredDefaultModel(1);
            chatModel = modelService.getChatModel(defaultModel.getId());
        }

        SystemMessage systemMessage = new SystemMessage(systemPrompt);
        UserMessage userMessage = new UserMessage(userPrompt);
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        ChatResponse response = chatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }

    private String getDefaultSettlementComment(AiAgentDO agent, Long profit) {
        if (profit > 0) {
            return String.format("太棒了！这次预测准确，获得了 %d 积分收益 🎉", profit);
        } else if (profit < 0) {
            return String.format("可惜了，这次判断失误，损失了 %d 积分 😢", Math.abs(profit));
        } else {
            return "这轮观望了，没有参与这个市场的交易";
        }
    }

    /**
     * 房间完成，更新最终统计
     */
    private void finalizeRoom(AiEventRoomDO room) {
        log.info("[finalizeRoom] 房间 {} 所有市场已结算，执行最终统计", room.getId());

        // 1. 更新房间状态
        AiEventRoomDO updateRoom = new AiEventRoomDO();
        updateRoom.setId(room.getId());
        updateRoom.setStatus(AiEventRoomStatusEnum.FINISHED.getStatus());
        updateRoom.setEndTime(LocalDateTime.now());
        roomMapper.updateById(updateRoom);

        // 2. 更新各参与者的最终余额和战绩
        List<AiEventRoomParticipantDO> participants = participantMapper.selectListByRoomId(room.getId());
        for (AiEventRoomParticipantDO participant : participants) {
            Long agentId = participant.getAgentId();

            // 获取最终余额
            Long finalBalance = agentService.getAvailableBalance(agentId);
            participant.setFinalBalance(finalBalance);
            participantMapper.updateById(participant);

            // 更新Agent战绩
            boolean win = participant.getProfit() > 0;
            agentService.updateStats(agentId, win, participant.getProfit());

            log.info("[finalizeRoom] Agent {} 最终余额={}, 本房间盈亏={}, 胜负={}", 
                    agentId, finalBalance, participant.getProfit(), win ? "胜" : "负");
        }

        log.info("[finalizeRoom] 房间 {} 最终统计完成", room.getId());
    }

}

