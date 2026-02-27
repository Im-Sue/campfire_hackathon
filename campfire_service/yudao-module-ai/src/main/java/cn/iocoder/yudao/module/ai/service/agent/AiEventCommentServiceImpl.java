package cn.iocoder.yudao.module.ai.service.agent;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiEventCommentPageReqVO;
import cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppAiEventCommentRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiAgentDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiEventCommentDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatRoleDO;
import cn.iocoder.yudao.module.ai.dal.mysql.agent.AiEventCommentMapper;
import cn.iocoder.yudao.module.ai.enums.agent.AiEventCommentStatusEnum;
import cn.iocoder.yudao.module.ai.service.model.AiChatRoleService;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import cn.iocoder.yudao.module.market.dal.dataobject.event.PmEventDO;
import cn.iocoder.yudao.module.market.dal.dataobject.market.PmMarketDO;
import cn.iocoder.yudao.module.market.service.event.PmEventService;
import cn.iocoder.yudao.module.market.service.market.PmMarketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI Agent 事件评论 Service 实现类
 *
 * @author campfire
 */
@Slf4j
@Service
public class AiEventCommentServiceImpl implements AiEventCommentService {

    @Resource
    private AiEventCommentMapper aiEventCommentMapper;
    @Resource
    private AiAgentService aiAgentService;
    @Resource
    private PmEventService pmEventService;
    @Resource
    private PmMarketService pmMarketService;
    @Resource
    private AiChatRoleService aiChatRoleService;
    @Resource
    private AiModelService modelService;

    @Override
    public void generateCommentForEvent(Long eventId) {
        // 1. 获取事件信息
        PmEventDO event = pmEventService.getEvent(eventId);
        if (event == null) {
            log.error("[generateCommentForEvent] 事件不存在: eventId={}", eventId);
            return;
        }

        // 2. 获取事件下的市场列表
        List<PmMarketDO> markets = pmMarketService.getMarketsByEventId(eventId);

        // 3. 获取所有启用的 Agent
        List<AiAgentDO> agents = aiAgentService.getEnabledAgentList();
        if (agents.isEmpty()) {
            log.warn("[generateCommentForEvent] 无启用的 Agent, 跳过评论生成: eventId={}", eventId);
            return;
        }

        log.info("[generateCommentForEvent] 开始为事件生成 Agent 评论: eventId={}, agentCount={}",
                eventId, agents.size());

        // 4. 遍历每个 Agent 生成评论
        for (AiAgentDO agent : agents) {
            try {
                generateSingleAgentComment(event, markets, agent);
            } catch (Exception e) {
                log.error("[generateCommentForEvent] Agent 评论生成失败: eventId={}, agentId={}",
                        eventId, agent.getId(), e);
            }
        }
    }

    private void generateSingleAgentComment(PmEventDO event, List<PmMarketDO> markets, AiAgentDO agent) {
        // 幂等检查
        AiEventCommentDO existing = aiEventCommentMapper.selectByEventAndAgent(event.getId(), agent.getId());
        if (existing != null && AiEventCommentStatusEnum.NORMAL.getStatus().equals(existing.getStatus())) {
            log.info("[generateSingleAgentComment] Agent 已评论该事件, 跳过: eventId={}, agentId={}",
                    event.getId(), agent.getId());
            return;
        }

        // 获取 Agent 关联的角色人设
        AiChatRoleDO role = aiChatRoleService.getChatRole(agent.getRoleId());
        String systemMessageText = role != null ? role.getSystemMessage() : "";

        // 构建 Prompt
        String userMessageText = buildUserMessage(event, markets, agent);

        // 调用 LLM
        List<Message> messages = new ArrayList<>();
        if (StrUtil.isNotBlank(systemMessageText)) {
            messages.add(new SystemMessage(systemMessageText));
        }
        messages.add(new UserMessage(userMessageText));

        try {
            // 获取 ChatModel
            ChatModel chatModel;
            if (role != null && role.getModelId() != null) {
                chatModel = modelService.getChatModel(role.getModelId());
            } else {
                // 使用默认对话模型
                var defaultModel = modelService.getRequiredDefaultModel(1);
                chatModel = modelService.getChatModel(defaultModel.getId());
            }

            String response = chatModel.call(new Prompt(messages))
                    .getResult()
                    .getOutput()
                    .getText();

            // 解析响应并保存
            saveComment(event.getId(), agent.getId(), response);
            log.info("[generateSingleAgentComment] 评论生成成功: eventId={}, agentId={}",
                    event.getId(), agent.getId());

        } catch (Exception e) {
            // 保存失败记录
            saveFailedComment(event.getId(), agent.getId(), e.getMessage());
            log.error("[generateSingleAgentComment] LLM 调用失败: eventId={}, agentId={}",
                    event.getId(), agent.getId(), e);
        }
    }

    private String buildUserMessage(PmEventDO event, List<PmMarketDO> markets, AiAgentDO agent) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是 ").append(agent.getName()).append("，一位预测市场分析师。\n");
        sb.append("你的性格特点是: ").append(agent.getPersonality()).append("\n");
        sb.append("你的风险偏好等级: ").append(agent.getRiskLevel()).append(" (1=保守, 2=中性, 3=激进)\n\n");

        sb.append("现在有一个新的预测市场事件上架，请你进行分析和评论：\n\n");

        sb.append("## 事件信息\n");
        sb.append("- **标题**: ").append(event.getTitle()).append("\n");
        sb.append("- **分类**: ").append(event.getCategory() != null ? event.getCategory() : "未分类").append("\n");
        sb.append("- **截止时间**: ").append(event.getEndDate()).append("\n\n");

        sb.append("## 市场列表\n");
        for (PmMarketDO market : markets) {
            sb.append("### ").append(market.getQuestion()).append("\n");
            sb.append("- 选项: ");
            List<String> outcomes = market.getOutcomes();
            if (outcomes != null) {
                sb.append(String.join(", ", outcomes));
            }
            sb.append("\n\n");
        }

        sb.append("## 输出要求\n");
        sb.append("请以你独特的风格和视角，对这个事件发表分析评论。\n");
        sb.append("输出必须是以下 JSON 格式：\n\n");
        sb.append("```json\n");
        sb.append("{\n");
        sb.append("    \"content\": \"你的评论内容(200-500字，体现你的性格特点)\",\n");
        sb.append("    \"stance\": \"bullish 或 bearish 或 neutral\",\n");
        sb.append("    \"confidence\": 0-100之间的整数,\n");
        sb.append("    \"keyPoints\": [\"关键分析点1\", \"关键分析点2\", \"关键分析点3\"],\n");
        sb.append("    \"summary\": \"一句话总结你的观点\"\n");
        sb.append("}\n");
        sb.append("```\n");

        return sb.toString();
    }

    private void saveComment(Long eventId, Long agentId, String response) {
        // 解析 JSON
        Map<String, Object> parsed = parseJsonResponse(response);

        AiEventCommentDO comment = AiEventCommentDO.builder()
                .eventId(eventId)
                .agentId(agentId)
                .content((String) parsed.getOrDefault("content", response))
                .structuredData(parsed)
                .status(AiEventCommentStatusEnum.NORMAL.getStatus())
                .build();

        aiEventCommentMapper.insert(comment);
    }

    private void saveFailedComment(Long eventId, Long agentId, String errorMessage) {
        AiEventCommentDO comment = AiEventCommentDO.builder()
                .eventId(eventId)
                .agentId(agentId)
                .content("")
                .status(AiEventCommentStatusEnum.FAILED.getStatus())
                .errorMessage(StrUtil.sub(errorMessage, 0, 500))
                .build();

        aiEventCommentMapper.insert(comment);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJsonResponse(String response) {
        try {
            // 去除 markdown 代码块
            String json = response;
            if (json.contains("```json")) {
                json = json.substring(json.indexOf("```json") + 7);
                json = json.substring(0, json.indexOf("```"));
            } else if (json.contains("```")) {
                json = json.substring(json.indexOf("```") + 3);
                json = json.substring(0, json.indexOf("```"));
            }
            return JSONUtil.toBean(json.trim(), Map.class);
        } catch (Exception e) {
            log.warn("[parseJsonResponse] JSON 解析失败, 使用降级处理: {}", e.getMessage());
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("content", response);
            fallback.put("stance", "neutral");
            fallback.put("confidence", 50);
            return fallback;
        }
    }

    @Override
    public void regenerateComment(Long eventId, Long agentId) {
        // 删除现有评论
        if (agentId != null) {
            aiEventCommentMapper.deleteByEventAndAgent(eventId, agentId);
        } else {
            aiEventCommentMapper.deleteByEventId(eventId);
        }

        // 重新生成
        generateCommentForEvent(eventId);
    }

    @Override
    public List<AppAiEventCommentRespVO> getCommentsByEventId(Long eventId) {
        List<AiEventCommentDO> comments = aiEventCommentMapper.selectByEventId(eventId,
                AiEventCommentStatusEnum.NORMAL.getStatus());

        return comments.stream().map(comment -> {
            AiAgentDO agent = aiAgentService.getAgent(comment.getAgentId());
            return AppAiEventCommentRespVO.builder()
                    .id(comment.getId())
                    .eventId(comment.getEventId())
                    .agentId(comment.getAgentId())
                    .agentName(agent != null ? agent.getName() : "")
                    .agentAvatar(agent != null ? agent.getAvatar() : "")
                    .agentPersonality(agent != null ? agent.getPersonality() : "")
                    .content(comment.getContent())
                    .structuredData(comment.getStructuredData())
                    .createTime(comment.getCreateTime())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public void deleteComment(Long id) {
        aiEventCommentMapper.deleteById(id);
    }

    @Override
    public PageResult<AiEventCommentDO> getCommentPage(AiEventCommentPageReqVO reqVO) {
        return aiEventCommentMapper.selectPage(reqVO);
    }
}
