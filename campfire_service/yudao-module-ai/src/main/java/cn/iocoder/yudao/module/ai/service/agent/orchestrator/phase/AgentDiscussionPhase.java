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
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Phase 2: Agent讨论阶段
 * 各Agent分析市场、表达观点
 *
 * @author campfire
 */
@Component
@Slf4j
public class AgentDiscussionPhase implements DiscussionPhase {

    @Resource
    private AiEventRoomMessageService messageService;

    @Resource
    private AiChatRoleService chatRoleService;

    @Resource
    private AiModelService modelService;

    @Override
    public void execute(RoomContext context) {
        log.info("[AgentDiscussionPhase] 开始Agent讨论, round={}", context.getCurrentRound() + 1);

        // 顺序执行各Agent讨论
        for (AiAgentDO agent : context.getAgents()) {
            try {
                executeAgentDiscussion(context, agent);
            } catch (Exception e) {
                log.error("[AgentDiscussionPhase] Agent {} 讨论失败, 跳过", agent.getId(), e);
                // 失败不中断，继续下一个Agent
            }
        }

        log.info("[AgentDiscussionPhase] Agent讨论阶段完成");
    }

    private void executeAgentDiscussion(RoomContext context, AiAgentDO agent) {
        log.info("\n  ┌────────────────────────────────────────────────────────────────┐");
        log.info("  │  🤖 {} 开始发言                                                │", agent.getName());
        log.info("  └────────────────────────────────────────────────────────────────┘");

        // 1. 获取Agent关联的角色
        AiChatRoleDO role = chatRoleService.getChatRole(agent.getRoleId());
        if (role == null) {
            log.warn("  ⚠️ Agent {} 未关联角色, 使用默认人设", agent.getName());
        }

        // 2. 构建Prompt
        String systemPrompt = buildSystemPrompt(agent, role);
        String userPrompt = buildDiscussionPrompt(context, agent);

        // 3. 调用LLM
        String response = callLLM(role != null ? role.getModelId() : null, systemPrompt, userPrompt);

        // 4. 保存消息
        AiEventRoomMessageDO message = AiEventRoomMessageDO.builder()
                .roomId(context.getRoom().getId())
                .agentId(agent.getId())
                .round(context.getCurrentRound() + 1)
                .messageType(AiEventRoomMessageTypeEnum.DISCUSSION.getType())
                .content(response)
                .status(1)
                .createTime(LocalDateTime.now())
                .build();
        messageService.createMessage(message);

        // 5. 加入上下文供后续Agent参考
        context.addDiscussionOpinion(agent.getName() + ": " + response);

        // 6. 打印发言内容
        log.info("  ╭─────────────────────────────────────────────────────────────────╮");
        log.info("  │  💬 {} 说:                                                      │", agent.getName());
        log.info("  ├─────────────────────────────────────────────────────────────────┤");
        // 分行打印发言内容，每行最多60字符
        String[] lines = response.split("\n");
        for (String line : lines) {
            if (line.length() > 60) {
                // 长行分割
                for (int i = 0; i < line.length(); i += 60) {
                    String part = line.substring(i, Math.min(i + 60, line.length()));
                    log.info("  │  {}  │", part);
                }
            } else if (!line.isEmpty()) {
                log.info("  │  {}  │", line);
            }
        }
        log.info("  ╰─────────────────────────────────────────────────────────────────╯\n");
    }

    /**
     * 构建系统提示词（角色人设 + Agent属性）
     */
    private String buildSystemPrompt(AiAgentDO agent, AiChatRoleDO role) {
        StringBuilder sb = new StringBuilder();
        
        // 角色人设
        if (role != null && role.getSystemMessage() != null) {
            sb.append(role.getSystemMessage()).append("\n\n");
        }
        
        // Agent属性注入
        sb.append("## 你的身份\n");
        sb.append("你是 ").append(agent.getName());
        if (agent.getPersonality() != null) {
            sb.append("，").append(agent.getPersonality());
        }
        sb.append("\n");
        
        if (agent.getDescription() != null) {
            sb.append(agent.getDescription()).append("\n");
        }
        
        // 风险偏好
        sb.append("\n## 你的风险偏好\n");
        String riskDesc = switch (agent.getRiskLevel()) {
            case 1 -> "非常保守，倾向于观望";
            case 2 -> "偏保守，谨慎下注";
            case 3 -> "中性，根据分析决策";
            case 4 -> "偏激进，愿意承担风险";
            case 5 -> "非常激进，追求高回报";
            default -> "中性";
        };
        sb.append(riskDesc).append("\n");
        
        // 战绩信息
        sb.append("\n## 你的战绩\n");
        sb.append("- 总参与: ").append(agent.getTotalEvents()).append("场\n");
        if (agent.getTotalEvents() > 0) {
            int winRate = (int) ((double) agent.getWinCount() / agent.getTotalEvents() * 100);
            sb.append("- 胜率: ").append(winRate).append("%\n");
        }
        sb.append("- 累计盈亏: ").append(agent.getTotalProfit()).append("积分\n");
        
        return sb.toString();
    }

    /**
     * 构建用户提示词（市场数据 + 讨论任务）
     */
    private String buildDiscussionPrompt(RoomContext context, AiAgentDO agent) {
        StringBuilder sb = new StringBuilder();

        // 市场数据
        sb.append("## 当前市场数据\n");
        sb.append(context.getMarketData().toString()).append("\n\n");

        // 外部数据 (新闻/热点)
        if (!context.getExternalData().isEmpty()) {
            sb.append("## 相关信息\n");
            sb.append(context.getExternalData().toString()).append("\n\n");
        }

        // 前序Agent观点
        if (!context.getDiscussionOpinions().isEmpty()) {
            sb.append("## 其他分析师观点\n");
            for (String opinion : context.getDiscussionOpinions()) {
                sb.append("- ").append(opinion).append("\n");
            }
            sb.append("\n");
        }

        sb.append("## 任务\n");
        sb.append("请分析当前市场数据，结合你的风险偏好和专业判断，发表你对该市场走势的观点。\n");
        sb.append("要求：\n");
        sb.append("1. 分析要有理有据\n");
        sb.append("2. 表达你独特的风格\n");
        sb.append("3. 观点要简洁明了（100-200字）\n");

        return sb.toString();
    }

    /**
     * 调用LLM获取回复
     */
    private String callLLM(Long modelId, String systemPrompt, String userPrompt) {
        // 诊断日志：打印 prompt 长度
        log.info("[callLLM] modelId={}, systemPrompt长度={}, userPrompt长度={}, 总长度={}", 
                modelId, systemPrompt.length(), userPrompt.length(), 
                systemPrompt.length() + userPrompt.length());
        try {
            // 获取ChatModel
            ChatModel chatModel;
            if (modelId != null) {
                chatModel = modelService.getChatModel(modelId);
            } else {
                // 使用默认模型
                var defaultModel = modelService.getRequiredDefaultModel(1); // 1=对话模型
                chatModel = modelService.getChatModel(defaultModel.getId());
            }

            // 构建消息列表
            SystemMessage systemMessage = new SystemMessage(systemPrompt);
            UserMessage userMessage = new UserMessage(userPrompt);
            Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

            // 调用LLM
            ChatResponse response = chatModel.call(prompt);
            String content = response.getResult().getOutput().getText();
            
            log.debug("[AgentDiscussionPhase] LLM响应: {}", content);
            return content;
            
        } catch (Exception e) {
            log.error("[AgentDiscussionPhase] LLM调用失败", e);
            return "【系统提示】AI服务暂时不可用，无法生成讨论内容";
        }
    }

    @Override
    public String getName() {
        return "Agent讨论";
    }

    @Override
    public int getOrder() {
        return 2;
    }

}

