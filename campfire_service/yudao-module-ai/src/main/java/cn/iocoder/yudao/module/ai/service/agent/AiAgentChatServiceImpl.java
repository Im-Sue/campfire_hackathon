package cn.iocoder.yudao.module.ai.service.agent;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppAgentChatQuotaRespVO;
import cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppAgentChatSendReqVO;
import cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppAgentChatSessionRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiAgentChatConfigDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiAgentChatMessageDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiAgentChatSessionDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiAgentDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatRoleDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.mysql.agent.AiAgentChatConfigMapper;
import cn.iocoder.yudao.module.ai.dal.mysql.agent.AiAgentChatMessageMapper;
import cn.iocoder.yudao.module.ai.dal.mysql.agent.AiAgentChatSessionMapper;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.service.model.AiChatRoleService;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import cn.iocoder.yudao.module.ai.util.AiUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.resolution.ToolCallbackResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;

/**
 * AI Agent C端对话 Service 实现类
 *
 * @author campfire
 */
@Service
@Slf4j
public class AiAgentChatServiceImpl implements AiAgentChatService {

    @Resource
    private AiAgentChatSessionMapper sessionMapper;

    @Resource
    private AiAgentChatMessageMapper messageMapper;

    @Resource
    private AiAgentChatConfigMapper agentChatConfigMapper;

    @Resource
    private AiAgentService agentService;

    @Resource
    private AiChatRoleService roleService;

    @Resource
    private AiModelService modelService;

    @Resource
    private ToolCallbackResolver toolCallbackResolver;

    @Resource
    private cn.iocoder.yudao.module.ai.tool.chat.CEndChatToolService cEndChatToolService;

    // 配置缓存
    private Map<String, String> configCache = new HashMap<>();

    @Override
    public Flux<String> sendMessageStream(Long userId, AppAgentChatSendReqVO reqVO) {
        log.info("[sendMessageStream] userId={}, agentId={}, content={}", userId, reqVO.getAgentId(), reqVO.getContent());

        // 1. 检查配额
        boolean isFree = checkAndDeductQuota(userId);

        // 2. 获取或创建会话
        boolean forceNew = Boolean.TRUE.equals(reqVO.getForceNew());
        AiAgentChatSessionDO session = reqVO.getSessionId() != null
                ? sessionMapper.selectById(reqVO.getSessionId())
                : getOrCreateSession(userId, reqVO.getAgentId(), forceNew);

        if (session == null || !session.getUserId().equals(userId)) {
            throw exception(CHAT_CONVERSATION_NOT_EXISTS);
        }

        // 3. 获取Agent信息
        AiAgentDO agent = agentService.getAgent(reqVO.getAgentId());
        if (agent == null) {
            throw exception(AI_AGENT_NOT_EXISTS);
        }

        // 4. 保存用户消息
        AiAgentChatMessageDO userMessage = createUserMessage(session, userId, agent.getId(), reqVO);
        messageMapper.insert(userMessage);

        // 5. 构建消息列表
        List<Message> messages = buildPromptMessages(session, agent, reqVO);

        // 6. 获取模型并调用（Agent -> Role -> Model）
        AiChatRoleDO role = roleService.getChatRole(agent.getRoleId());
        if (role == null || role.getModelId() == null) {
            throw exception(AI_AGENT_NOT_EXISTS); // Agent配置不完整
        }
        AiModelDO model = modelService.validateModel(role.getModelId());
        ChatModel chatModel = modelService.getChatModel(role.getModelId());

        // 7. 获取C端工具列表
        List<ToolCallback> toolCallbacks = getCEndChatToolCallbacks();
        Map<String, Object> toolContext = CollUtil.isNotEmpty(toolCallbacks) 
                ? AiUtils.buildCommonToolContext() 
                : Map.of();

        // 8. 构建带工具的ChatOptions
        AiPlatformEnum platform = AiPlatformEnum.validatePlatform(model.getPlatform());
        ChatOptions chatOptions = AiUtils.buildChatOptions(platform, model.getModel(),
                0.7, 2000, toolCallbacks, toolContext);

        // 9. 流式调用
        StringBuilder contentBuilder = new StringBuilder();

        return Flux.create(sink -> {
            // 发送会话信息
            sink.next(String.format("{\"event\":\"start\",\"sessionId\":%d,\"messageId\":%d}", 
                    session.getId(), userMessage.getId()));

            // 发送思考状态
            sink.next("{\"event\":\"thinking\"}");

            try {
                Prompt prompt = new Prompt(messages, chatOptions);
                chatModel.stream(prompt).subscribe(
                        response -> {
                            if (response.getResult() != null && response.getResult().getOutput() != null) {
                                String content = response.getResult().getOutput().getText();
                                if (StrUtil.isNotEmpty(content)) {
                                    contentBuilder.append(content);
                                    sink.next(String.format("{\"event\":\"content\",\"delta\":\"%s\"}", 
                                            escapeJson(content)));
                                }
                            }
                        },
                        error -> {
                            log.error("[sendMessageStream] LLM调用失败", error);
                            sink.next("{\"event\":\"error\",\"message\":\"服务暂时繁忙，请稍后重试\"}");
                            sink.complete();
                        },
                        () -> {
                            // 保存Assistant消息
                            AiAgentChatMessageDO assistantMessage = createAssistantMessage(
                                    session, userId, agent.getId(), contentBuilder.toString(), null);
                            messageMapper.insert(assistantMessage);

                            // 更新会话统计
                            updateSessionStats(session.getId());

                            // 发送完成事件
                            AppAgentChatQuotaRespVO quota = getQuota(userId);
                            sink.next(String.format("{\"event\":\"done\",\"quotaRemaining\":%d}", 
                                    quota.getFreeQuotaRemaining()));
                            sink.complete();
                        }
                );
            } catch (Exception e) {
                log.error("[sendMessageStream] 异常", e);
                sink.next("{\"event\":\"error\",\"message\":\"服务异常\"}");
                sink.complete();
            }
        });
    }

    @Override
    public AiAgentChatSessionDO getOrCreateSession(Long userId, Long agentId, boolean forceNew) {
        if (forceNew) {
            return createNewSession(userId, agentId);
        }

        // 查询最近会话
        AiAgentChatSessionDO lastSession = sessionMapper.selectLastSession(userId, agentId);

        if (lastSession != null) {
            // 后端检查24小时超时
            int timeoutHours = getConfigIntValue("session_timeout_hours", 24);
            LocalDateTime threshold = LocalDateTime.now().minusHours(timeoutHours);

            if (lastSession.getLastMessageTime() != null 
                    && lastSession.getLastMessageTime().isAfter(threshold)) {
                log.info("[getOrCreateSession] 继续使用现有会话 sessionId={}", lastSession.getId());
                return lastSession;
            }
        }

        // 超时或无会话，创建新会话
        return createNewSession(userId, agentId);
    }

    private AiAgentChatSessionDO createNewSession(Long userId, Long agentId) {
        AiAgentChatSessionDO session = AiAgentChatSessionDO.builder()
                .userId(userId)
                .agentId(agentId)
                .title("新对话")
                .messageCount(0)
                .lastMessageTime(LocalDateTime.now())
                .build();
        sessionMapper.insert(session);
        log.info("[createNewSession] 创建新会话 sessionId={}", session.getId());
        return session;
    }

    @Override
    public List<AppAgentChatSessionRespVO> getSessionList(Long userId, Long agentId) {
        List<AiAgentChatSessionDO> sessions = sessionMapper.selectListByUserAndAgent(userId, agentId);

        return sessions.stream().map(session -> {
            AppAgentChatSessionRespVO vo = BeanUtils.toBean(session, AppAgentChatSessionRespVO.class);
            vo.setSessionId(session.getId());

            // 获取Agent名称
            AiAgentDO agent = agentService.getAgent(session.getAgentId());
            if (agent != null) {
                vo.setAgentName(agent.getName());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<AiAgentChatMessageDO> getMessageHistory(Long sessionId, Long userId) {
        // 验证会话归属
        AiAgentChatSessionDO session = sessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            throw exception(CHAT_CONVERSATION_NOT_EXISTS);
        }

        return messageMapper.selectListBySessionId(sessionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSession(Long sessionId, Long userId) {
        // 验证会话归属
        AiAgentChatSessionDO session = sessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            throw exception(CHAT_CONVERSATION_NOT_EXISTS);
        }

        // 删除会话（软删除）
        sessionMapper.deleteById(sessionId);

        log.info("[deleteSession] 删除会话 sessionId={}, userId={}", sessionId, userId);
    }

    @Override
    public AppAgentChatQuotaRespVO getQuota(Long userId) {
        int freeQuotaTotal = getConfigIntValue("free_quota_daily", 10);
        int costPerMessage = getConfigIntValue("paid_cost_per_message", 5);

        // 今日已使用次数
        Long usedCount = messageMapper.selectTodayCountByUserId(userId);
        int freeQuotaUsed = usedCount != null ? usedCount.intValue() : 0;

        // TODO: 获取积分余额（需要集成point模块）
        Long pointBalance = 0L;

        return AppAgentChatQuotaRespVO.builder()
                .freeQuotaTotal(freeQuotaTotal)
                .freeQuotaUsed(freeQuotaUsed)
                .freeQuotaRemaining(Math.max(0, freeQuotaTotal - freeQuotaUsed))
                .pointBalance(pointBalance)
                .costPerMessage(costPerMessage)
                .build();
    }

    @Override
    public boolean checkAndDeductQuota(Long userId) {
        AppAgentChatQuotaRespVO quota = getQuota(userId);

        if (quota.getFreeQuotaRemaining() > 0) {
            // 有免费次数
            return true;
        }

        // 无免费次数，检查积分
        // TODO: 集成point模块扣减积分
        // 目前简化处理：如果无免费次数则抛出异常
        throw exception(AI_CHAT_QUOTA_EXCEEDED);
    }

    @Override
    public int getConfigIntValue(String key, int defaultValue) {
        // 先查缓存
        String cachedValue = configCache.get(key);
        if (cachedValue != null) {
            try {
                return Integer.parseInt(cachedValue);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        // 查数据库
        AiAgentChatConfigDO config = agentChatConfigMapper.selectByConfigKey(key);
        if (config != null && StrUtil.isNotEmpty(config.getConfigValue())) {
            configCache.put(key, config.getConfigValue());
            try {
                return Integer.parseInt(config.getConfigValue());
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        return defaultValue;
    }

    // ========== 私有方法 ==========

    private List<Message> buildPromptMessages(AiAgentChatSessionDO session, AiAgentDO agent, 
                                               AppAgentChatSendReqVO reqVO) {
        List<Message> messages = new ArrayList<>();

        // 1. 构建System Message（Agent人设）
        String systemPrompt = buildAgentSystemPrompt(agent, reqVO);
        messages.add(new SystemMessage(systemPrompt));

        // 2. 加载上下文消息
        int contextRounds = getConfigIntValue("context_rounds", 10);
        List<AiAgentChatMessageDO> historyMessages = messageMapper.selectRecentMessages(
                session.getId(), contextRounds * 2);

        // 倒序变正序
        if (CollUtil.isNotEmpty(historyMessages)) {
            CollUtil.reverse(historyMessages);
            for (AiAgentChatMessageDO msg : historyMessages) {
                if ("user".equals(msg.getType())) {
                    messages.add(new UserMessage(msg.getContent()));
                } else if ("assistant".equals(msg.getType())) {
                    messages.add(new AssistantMessage(msg.getContent()));
                }
            }
        }

        // 3. 添加当前用户消息
        messages.add(new UserMessage(reqVO.getContent()));

        return messages;
    }

    private String buildAgentSystemPrompt(AiAgentDO agent, AppAgentChatSendReqVO reqVO) {
        StringBuilder sb = new StringBuilder();

        // 获取角色设定
        if (agent.getRoleId() != null) {
            AiChatRoleDO role = roleService.getChatRole(agent.getRoleId());
            if (role != null && StrUtil.isNotEmpty(role.getSystemMessage())) {
                sb.append(role.getSystemMessage()).append("\n\n");
            }
        }

        // Agent身份
        sb.append("你是AI预测分析师 ").append(agent.getName()).append("。\n");

        // Agent属性
        if (StrUtil.isNotEmpty(agent.getPersonality())) {
            sb.append("你的分析风格：").append(agent.getPersonality()).append("\n");
        }
        if (agent.getRiskLevel() != null) {
            sb.append("你的风险偏好等级：").append(agent.getRiskLevel()).append("（1-5，5为最激进）\n");
        }

        // Agent战绩
        sb.append("\n你的历史战绩：\n");
        sb.append("- 参与事件数：").append(agent.getTotalEvents() != null ? agent.getTotalEvents() : 0).append("\n");
        sb.append("- 胜利次数：").append(agent.getWinCount() != null ? agent.getWinCount() : 0).append("\n");

        // 行为指引
        sb.append("\n请注意：\n");
        sb.append("1. 你可以使用工具检索市场数据进行分析\n");
        sb.append("2. 提供专业分析，但提醒用户自行决策\n");
        sb.append("3. 回复控制在300字以内\n");

        return sb.toString();
    }

    private AiAgentChatMessageDO createUserMessage(AiAgentChatSessionDO session, Long userId, 
                                                    Long agentId, AppAgentChatSendReqVO reqVO) {
        AiAgentChatMessageDO message = AiAgentChatMessageDO.builder()
                .sessionId(session.getId())
                .userId(userId)
                .agentId(agentId)
                .type("user")
                .content(reqVO.getContent())
                .contextEventId(reqVO.getEventId())
                .contextMarketId(reqVO.getMarketId())
                .tokensUsed(0)
                .build();

        // 更新会话标题（首条消息）
        if (session.getMessageCount() == 0) {
            String title = reqVO.getContent().length() > 20 
                    ? reqVO.getContent().substring(0, 20) + "..." 
                    : reqVO.getContent();
            session.setTitle(title);
            sessionMapper.updateById(session);
        }

        return message;
    }

    private AiAgentChatMessageDO createAssistantMessage(AiAgentChatSessionDO session, Long userId,
                                                         Long agentId, String content, 
                                                         List<Map<String, Object>> toolCalls) {
        return AiAgentChatMessageDO.builder()
                .sessionId(session.getId())
                .userId(userId)
                .agentId(agentId)
                .type("assistant")
                .content(content)
                .toolCalls(toolCalls)
                .tokensUsed(0) // TODO: 记录实际token消耗
                .build();
    }

    private void updateSessionStats(Long sessionId) {
        Long count = messageMapper.selectCountBySessionId(sessionId);
        sessionMapper.updateMessageStats(sessionId, count != null ? count.intValue() : 0, LocalDateTime.now());
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    /**
     * 获取C端对话可用的工具回调列表
     * 只返回只读工具（搜索事件、搜索市场、获取持仓、推荐市场）
     * 不包含下单等操作性工具，确保C端用户无法通过对话执行敏感操作
     */
    private List<ToolCallback> getCEndChatToolCallbacks() {
        try {
            ToolCallback[] callbacks = org.springframework.ai.support.ToolCallbacks.from(cEndChatToolService);
            log.info("[getCEndChatToolCallbacks] 加载C端工具数量: {}", callbacks.length);
            return Arrays.asList(callbacks);
        } catch (Exception e) {
            log.error("[getCEndChatToolCallbacks] 加载工具异常", e);
            return new ArrayList<>();
        }
    }

}
