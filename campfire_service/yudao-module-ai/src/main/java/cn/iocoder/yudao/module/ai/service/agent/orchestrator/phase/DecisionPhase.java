package cn.iocoder.yudao.module.ai.service.agent.orchestrator.phase;

import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiAgentDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiEventRoomMessageDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatRoleDO;
import cn.iocoder.yudao.module.ai.enums.agent.AiEventRoomMessageTypeEnum;
import cn.iocoder.yudao.module.ai.service.agent.AiEventRoomMessageService;
import cn.iocoder.yudao.module.ai.service.agent.orchestrator.DiscussionPhase;
import cn.iocoder.yudao.module.ai.service.agent.orchestrator.RoomContext;
import cn.iocoder.yudao.module.ai.service.model.AiChatRoleService;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Phase 3: 决策生成阶段
 * 各Agent做出交易决策
 *
 * @author campfire
 */
@Component
@Slf4j
public class DecisionPhase implements DiscussionPhase {

    @Resource
    private AiEventRoomMessageService messageService;

    @Resource
    private AiChatRoleService chatRoleService;

    @Resource
    private AiModelService modelService;

    @Resource
    private cn.iocoder.yudao.module.market.service.position.PmPositionService positionService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void execute(RoomContext context) {
        log.info("[DecisionPhase] 开始决策阶段, round={}", context.getCurrentRound() + 1);

        List<AgentDecision> decisions = new ArrayList<>();

        // 各Agent生成决策
        for (AiAgentDO agent : context.getAgents()) {
            try {
                AgentDecision decision = executeAgentDecision(context, agent);
                if (decision != null) {
                    decisions.add(decision);
                }
            } catch (Exception e) {
                log.error("[DecisionPhase] Agent {} 决策失败", agent.getId(), e);
            }
        }

        // 保存决策到上下文，供执行阶段使用
        context.setMarketData("decisions", decisions);

        log.info("[DecisionPhase] 决策阶段完成, 共 {} 个决策", decisions.size());
    }

    private AgentDecision executeAgentDecision(RoomContext context, AiAgentDO agent) {
        log.info("[DecisionPhase] Agent {} ({}) 开始决策", agent.getId(), agent.getName());

        // 1. 从Context获取Agent余额
        Long balance = context.getAgentBalance(agent.getId());
        if (balance == null || balance <= 0) {
            log.info("[DecisionPhase] Agent {} 余额不足，进入观察者模式", agent.getId());
            saveObserverMessage(context, agent);
            return null;
        }

        // 2. 获取Agent关联的角色
        AiChatRoleDO role = chatRoleService.getChatRole(agent.getRoleId());

        // 3. 构建决策Prompt
        String systemPrompt = buildSystemPrompt(agent, role);
        String userPrompt = buildDecisionPrompt(context, agent, balance);

        // 4. 调用LLM
        String response = callLLM(role != null ? role.getModelId() : null, systemPrompt, userPrompt);

        // 5. 解析决策
        AgentDecision decision = parseDecision(response, agent);
        if (decision == null) {
            log.warn("[DecisionPhase] Agent {} 决策解析失败, 使用默认hold", agent.getId());
            decision = createHoldDecision(agent);
        }

        // 6. 系统校验并调整决策
        validateAndAdjustDecision(decision, agent, balance);

        // 7. 保存决策消息
        saveDecisionMessage(context, agent, decision, response);

        return decision;
    }

    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt(AiAgentDO agent, AiChatRoleDO role) {
        StringBuilder sb = new StringBuilder();
        
        if (role != null && role.getSystemMessage() != null) {
            sb.append(role.getSystemMessage()).append("\n\n");
        }
        
        sb.append("## 你的身份\n");
        sb.append("你是 ").append(agent.getName());
        if (agent.getPersonality() != null) {
            sb.append("，").append(agent.getPersonality());
        }
        sb.append("\n\n");
        
        sb.append("## 风险偏好\n");
        String riskDesc = switch (agent.getRiskLevel()) {
            case 1 -> "非常保守，倾向于观望不下注";
            case 2 -> "偏保守，只在高确定性时小额下注";
            case 3 -> "中性，根据分析合理决策";
            case 4 -> "偏激进，愿意承担风险追求收益";
            case 5 -> "非常激进，追求高回报";
            default -> "中性";
        };
        sb.append(riskDesc).append("\n");
        
        return sb.toString();
    }

    /**
     * 构建决策提示词
     */
    private String buildDecisionPrompt(RoomContext context, AiAgentDO agent, Long balance) {
        StringBuilder sb = new StringBuilder();

        sb.append("## 你的账户信息\n");
        sb.append("- 当前余额: ").append(balance).append(" 积分\n");
        sb.append("- 单次下注范围: ").append(agent.getMinBetAmount()).append(" - ").append(agent.getMaxBetAmount()).append(" 积分\n");
        sb.append("- 最大下注比例: ").append(agent.getMaxBetRatio().multiply(new java.math.BigDecimal(100))).append("%\n\n");

        sb.append("## 市场数据\n");
        sb.append(context.getMarketData().toString()).append("\n\n");

        sb.append("## 讨论汇总\n");
        for (String opinion : context.getDiscussionOpinions()) {
            sb.append("- ").append(opinion).append("\n");
        }

        sb.append("\n## 任务\n");
        sb.append("请基于以上信息做出交易决策。\n\n");

        // 动态构建outcome选项说明
        sb.append("**重要提示**: 每个市场的outcome选项不同，请务必从市场数据中查看实际的outcomes列表，并使用准确的outcome名称。\n\n");

        sb.append("**请严格以JSON格式输出决策**，格式如下：\n");
        sb.append("```json\n");
        sb.append("{\n");
        sb.append("  \"action\": \"buy\", // buy(买入), sell(卖出), hold(观望)\n");
        sb.append("  \"marketId\": 1, // 市场ID\n");
        sb.append("  \"outcome\": \"具体的outcome名称\", // 必须从市场数据的outcomes列表中选择\n");
        sb.append("  \"amount\": 100, // 下注金额(积分)\n");
        sb.append("  \"reason\": \"简短的理由\"\n");
        sb.append("}\n");
        sb.append("```\n");

        return sb.toString();
    }

    /**
     * 调用LLM
     */
    private String callLLM(Long modelId, String systemPrompt, String userPrompt) {
        // 诊断日志：打印 prompt 长度
        log.info("[callLLM] modelId={}, systemPrompt长度={}, userPrompt长度={}, 总长度={}", 
                modelId, systemPrompt.length(), userPrompt.length(), 
                systemPrompt.length() + userPrompt.length());
        try {
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
            String content = response.getResult().getOutput().getText();
            
            log.debug("[DecisionPhase] LLM响应: {}", content);
            return content;
            
        } catch (Exception e) {
            log.error("[DecisionPhase] LLM调用失败", e);
            return null;
        }
    }

    /**
     * 解析LLM返回的决策JSON
     */
    private AgentDecision parseDecision(String response, AiAgentDO agent) {
        if (response == null || response.isEmpty()) {
            return null;
        }

        try {
            // 尝试从响应中提取JSON
            String json = extractJson(response);
            if (json == null) {
                return null;
            }

            JsonNode node = objectMapper.readTree(json);
            
            AgentDecision decision = new AgentDecision();
            decision.setAgentId(agent.getId());
            decision.setAction(node.has("action") ? node.get("action").asText("hold") : "hold");
            decision.setMarketId(node.has("marketId") ? node.get("marketId").asLong(1L) : 1L);
            decision.setOutcome(node.has("outcome") ? node.get("outcome").asText("Yes") : "Yes");
            decision.setAmount(node.has("amount") ? node.get("amount").asLong(0L) : 0L);
            decision.setReason(node.has("reason") ? node.get("reason").asText() : "");
            
            return decision;
            
        } catch (Exception e) {
            log.warn("[DecisionPhase] 解析决策JSON失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从LLM响应中提取JSON
     */
    private String extractJson(String text) {
        // 尝试匹配 ```json ... ``` 格式
        Pattern pattern = Pattern.compile("```json\\s*([\\s\\S]*?)\\s*```");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        // 尝试匹配 { ... } 格式
        pattern = Pattern.compile("\\{[\\s\\S]*?\\}");
        matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group().trim();
        }

        return null;
    }

    /**
     * 创建默认的hold决策
     */
    private AgentDecision createHoldDecision(AiAgentDO agent) {
        AgentDecision decision = new AgentDecision();
        decision.setAgentId(agent.getId());
        decision.setAction("hold");
        decision.setMarketId(0L);
        decision.setOutcome("");
        decision.setAmount(0L);
        decision.setReason("本轮观望");
        return decision;
    }

    private void validateAndAdjustDecision(AgentDecision decision, AiAgentDO agent, Long balance) {
        if ("hold".equals(decision.getAction())) {
            return;
        }

        // 校验卖出操作的持仓
        if ("sell".equals(decision.getAction())) {
            try {
                // 查询Agent在该市场的持仓
                cn.iocoder.yudao.module.market.dal.dataobject.position.PmPositionDO position =
                    positionService.getPosition(agent.getWalletUserId(), decision.getMarketId(), decision.getOutcome());

                if (position == null || position.getQuantity().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                    log.info("[DecisionPhase] Agent {} 尝试卖出但无持仓 (market={}, outcome={}), 改为hold",
                            agent.getId(), decision.getMarketId(), decision.getOutcome());
                    decision.setAction("hold");
                    decision.setAmount(0L);
                    decision.setReason("无持仓，观望");
                    return;
                }

                log.debug("[DecisionPhase] Agent {} 持仓校验通过: quantity={}",
                        agent.getId(), position.getQuantity());

            } catch (Exception e) {
                log.error("[DecisionPhase] Agent {} 持仓查询失败，改为hold", agent.getId(), e);
                decision.setAction("hold");
                decision.setAmount(0L);
                decision.setReason("持仓查询失败，观望");
                return;
            }
        }

        // 校验金额
        if (decision.getAmount() > balance) {
            log.info("[DecisionPhase] Agent {} 决策金额 {} 超过余额 {}, 调整为余额",
                    agent.getId(), decision.getAmount(), balance);
            decision.setAmount(balance);
        }

        long maxAllowed = (long) (balance * agent.getMaxBetRatio().doubleValue());
        if (decision.getAmount() > maxAllowed) {
            log.info("[DecisionPhase] Agent {} 决策金额 {} 超过比例限制 {}, 调整",
                    agent.getId(), decision.getAmount(), maxAllowed);
            decision.setAmount(maxAllowed);
        }

        if (decision.getAmount() > agent.getMaxBetAmount()) {
            decision.setAmount(agent.getMaxBetAmount());
        }

        if (decision.getAmount() < agent.getMinBetAmount()) {
            log.info("[DecisionPhase] Agent {} 决策金额 {} 低于最小下注额 {}, 改为hold",
                    agent.getId(), decision.getAmount(), agent.getMinBetAmount());
            decision.setAction("hold");
            decision.setAmount(0L);
        }
    }

    private void saveDecisionMessage(RoomContext context, AiAgentDO agent, AgentDecision decision, String rawResponse) {
        Map<String, Object> structuredData = new HashMap<>();
        structuredData.put("action", decision.getAction());
        structuredData.put("marketId", decision.getMarketId());
        structuredData.put("outcome", decision.getOutcome());
        structuredData.put("amount", decision.getAmount());
        structuredData.put("reason", decision.getReason());

        String content;
        if ("hold".equals(decision.getAction())) {
            content = agent.getName() + " 决定: 本轮观望 - " + decision.getReason();
        } else {
            content = String.format("%s 决定: %s %s %d积分 - %s", 
                    agent.getName(), decision.getAction(), decision.getOutcome(), 
                    decision.getAmount(), decision.getReason());
        }

        AiEventRoomMessageDO message = AiEventRoomMessageDO.builder()
                .roomId(context.getRoom().getId())
                .agentId(agent.getId())
                .round(context.getCurrentRound() + 1)
                .messageType(AiEventRoomMessageTypeEnum.DECISION.getType())
                .content(content)
                .structuredData(structuredData)
                .status(1)
                .createTime(LocalDateTime.now())
                .build();
        messageService.createMessage(message);
    }

    private void saveObserverMessage(RoomContext context, AiAgentDO agent) {
        Map<String, Object> structuredData = new HashMap<>();
        structuredData.put("observer", true);
        structuredData.put("action", "hold");

        AiEventRoomMessageDO message = AiEventRoomMessageDO.builder()
                .roomId(context.getRoom().getId())
                .agentId(agent.getId())
                .round(context.getCurrentRound() + 1)
                .messageType(AiEventRoomMessageTypeEnum.DECISION.getType())
                .content("弹尽粮绝，只能在场边为你们加油了 😅")
                .structuredData(structuredData)
                .status(1)
                .createTime(LocalDateTime.now())
                .build();
        messageService.createMessage(message);
    }

    @Override
    public String getName() {
        return "决策生成";
    }

    @Override
    public int getOrder() {
        return 3;
    }

    /**
     * Agent决策
     */
    @Data
    public static class AgentDecision {
        private Long agentId;
        private String action; // buy, sell, hold
        private Long marketId;
        private String outcome; // Yes, No
        private Long amount;
        private String reason;
    }

}

