package cn.iocoder.yudao.module.ai.service.agent.orchestrator.phase;

import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiAgentDO;
import cn.iocoder.yudao.module.ai.service.agent.orchestrator.DiscussionPhase;
import cn.iocoder.yudao.module.ai.service.agent.orchestrator.RoomContext;
import cn.iocoder.yudao.module.market.controller.app.order.vo.AppOrderCreateReqVO;
import cn.iocoder.yudao.module.market.service.market.PmMarketService;
import cn.iocoder.yudao.module.market.service.order.PmOrderService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Phase 4: 执行下注阶段
 * 执行Agent的交易决策
 *
 * @author campfire
 */
@Component
@Slf4j
public class ExecutionPhase implements DiscussionPhase {

    @Resource
    private PmOrderService orderService;

    @Resource
    private PmMarketService marketService;

    @Resource
    private cn.iocoder.yudao.module.ai.dal.mysql.agent.AiEventRoomOrderMapper roomOrderMapper;

    @Resource
    private cn.iocoder.yudao.module.ai.dal.mysql.agent.AiEventRoomParticipantMapper participantMapper;

    @Override
    @SuppressWarnings("unchecked")
    public void execute(RoomContext context) {
        log.info("[ExecutionPhase] 开始执行下注, round={}", context.getCurrentRound() + 1);

        // 获取决策列表
        Object decisionsObj = context.getMarketData().get("decisions");
        if (decisionsObj == null) {
            log.info("[ExecutionPhase] 没有决策需要执行");
            return;
        }

        List<DecisionPhase.AgentDecision> decisions = (List<DecisionPhase.AgentDecision>) decisionsObj;

        int successCount = 0;
        int failCount = 0;

        for (DecisionPhase.AgentDecision decision : decisions) {
            if (!"buy".equals(decision.getAction()) && !"sell".equals(decision.getAction())) {
                continue; // 跳过 hold
            }

            try {
                executeOrder(context, decision);
                successCount++;
            } catch (Exception e) {
                log.error("[ExecutionPhase] Agent {} 下注失败", decision.getAgentId(), e);
                failCount++;
            }
        }

        log.info("[ExecutionPhase] 执行完成, 成功={}, 失败={}", successCount, failCount);
    }

    private void executeOrder(RoomContext context, DecisionPhase.AgentDecision decision) {
        log.info("[ExecutionPhase] 执行下注: Agent={}, market={}, outcome={}, amount={}", 
                decision.getAgentId(), decision.getMarketId(), decision.getOutcome(), decision.getAmount());

        // 1. 检查市场状态
        var market = marketService.getMarket(decision.getMarketId());
        if (market == null || market.getStatus() != 1) { // 1=TRADING
            log.warn("[ExecutionPhase] 市场 {} 不存在或已封盘，跳过", decision.getMarketId());
            return;
        }

        // 2. 从Context获取Agent的walletUserId
        AiAgentDO agent = context.getAgents().stream()
                .filter(a -> a.getId().equals(decision.getAgentId()))
                .findFirst()
                .orElse(null);
        if (agent == null) {
            log.warn("[ExecutionPhase] Agent {} 不存在，跳过", decision.getAgentId());
            return;
        }
        Long walletUserId = agent.getWalletUserId();

        // 3. 调用下单接口
        AppOrderCreateReqVO orderReq = new AppOrderCreateReqVO();
        orderReq.setMarketId(decision.getMarketId());
        orderReq.setOutcome(decision.getOutcome());
        orderReq.setAmount(decision.getAmount());
        orderReq.setOrderType(1); // 市价单

        // 设置买卖方向
        if ("buy".equals(decision.getAction())) {
            orderReq.setSide(1); // 1=买入
        } else if ("sell".equals(decision.getAction())) {
            orderReq.setSide(2); // 2=卖出
        } else {
            log.warn("[ExecutionPhase] 未知的action类型: {}, 跳过", decision.getAction());
            return;
        }

        Long orderId = orderService.createOrder(walletUserId, orderReq);
        log.info("[ExecutionPhase] 下注成功: Agent={}, orderId={}", decision.getAgentId(), orderId);

        // 4. 记录竞赛订单关联
        try {
            cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiEventRoomOrderDO roomOrder =
                cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiEventRoomOrderDO.builder()
                    .roomId(context.getRoom().getId())
                    .agentId(decision.getAgentId())
                    .orderId(orderId)
                    .round(context.getCurrentRound() + 1)
                    .decisionAction(decision.getAction())
                    .decisionReason(decision.getReason())
                    .orderAmount(decision.getAmount())
                    .orderOutcome(decision.getOutcome())
                    .orderStatus(1) // 1=已成交（市价单立即成交）
                    .tenantId(1L)
                    .build();
            roomOrderMapper.insert(roomOrder);
            log.info("[ExecutionPhase] 记录竞赛订单关联成功: roomOrderId={}", roomOrder.getId());
        } catch (Exception e) {
            log.error("[ExecutionPhase] 记录竞赛订单关联失败", e);
        }

        // 5. 更新参与者订单统计
        try {
            cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiEventRoomParticipantDO participant =
                participantMapper.selectOne("room_id", context.getRoom().getId(), "agent_id", decision.getAgentId());
            if (participant != null) {
                cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiEventRoomParticipantDO updateObj =
                    new cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiEventRoomParticipantDO();
                updateObj.setId(participant.getId());
                updateObj.setOrderCount(participant.getOrderCount() != null ? participant.getOrderCount() + 1 : 1);
                participantMapper.updateById(updateObj);
                log.info("[ExecutionPhase] 更新参与者订单统计成功: Agent={}, orderCount={}",
                        decision.getAgentId(), updateObj.getOrderCount());
            }
        } catch (Exception e) {
            log.error("[ExecutionPhase] 更新参与者订单统计失败", e);
        }
    }

    @Override
    public String getName() {
        return "执行下注";
    }

    @Override
    public int getOrder() {
        return 4;
    }

}

