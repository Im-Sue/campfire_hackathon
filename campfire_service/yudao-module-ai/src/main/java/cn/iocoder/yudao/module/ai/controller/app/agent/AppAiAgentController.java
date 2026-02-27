package cn.iocoder.yudao.module.ai.controller.app.agent;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiAgentRespVO;
import cn.iocoder.yudao.module.ai.controller.app.agent.vo.*;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiAgentChatMessageDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiAgentDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiEventRoomDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiEventRoomMessageDO;
import cn.iocoder.yudao.module.ai.service.agent.AiAgentChatService;
import cn.iocoder.yudao.module.ai.service.agent.AiAgentService;
import cn.iocoder.yudao.module.ai.service.agent.AiEventRoomMessageService;
import cn.iocoder.yudao.module.ai.service.agent.AiEventRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import jakarta.annotation.security.PermitAll;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * AI Agent C端 Controller
 *
 * @author campfire
 */
@Tag(name = "用户端 - AI Agent")
@RestController
@RequestMapping("/ai/app/agent")
@Validated
public class AppAiAgentController {

    @Resource
    private AiAgentService agentService;

    @Resource
    private AiEventRoomService roomService;

    @Resource
    private AiEventRoomMessageService messageService;

    @GetMapping("/list")
    @Operation(summary = "获取Agent列表")
    @PermitAll
    public CommonResult<List<AiAgentRespVO>> getAgentList() {
        List<AiAgentDO> list = agentService.getEnabledAgentList();
        return success(BeanUtils.toBean(list, AiAgentRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获取Agent详情")
    @PermitAll
    @Parameter(name = "id", description = "Agent ID", required = true)
    public CommonResult<AiAgentRespVO> getAgent(@RequestParam("id") Long id) {
        AiAgentDO agent = agentService.getAgent(id);
        return success(BeanUtils.toBean(agent, AiAgentRespVO.class));
    }

    @GetMapping("/event-room")
    @Operation(summary = "获取事件的AI讨论房间")
    @Parameter(name = "eventId", description = "事件ID", required = true)
    public CommonResult<AiEventRoomDO> getEventRoom(@RequestParam("eventId") Long eventId) {
        return success(roomService.getRoomByEventId(eventId));
    }

    @GetMapping("/room/messages")
    @Operation(summary = "获取房间讨论消息")
    public CommonResult<List<AiEventRoomMessageDO>> getRoomMessages(
            @RequestParam("roomId") Long roomId,
            @RequestParam(value = "afterId", required = false) Long afterId) {
        if (afterId != null) {
            return success(messageService.getMessagesAfter(roomId, afterId));
        }
        return success(messageService.getMessagesByRoomId(roomId));
    }

    @GetMapping("/timeline")
    @Operation(summary = "获取 AI Agent 全局时间线")
    public CommonResult<PageResult<AppAgentTimelineRespVO>> getAgentTimeline(
            @Valid AppAgentTimelineReqVO reqVO) {
        return success(messageService.getGlobalTimeline(reqVO));
    }

    @GetMapping("/balance-chart")
    @Operation(summary = "获取 AI Agent 余额图表数据")
    public CommonResult<AppAgentBalanceChartRespVO> getBalanceChart(
            @Valid AppAgentBalanceChartReqVO reqVO) {
        return success(agentService.getBalanceChart(reqVO));
    }

    @GetMapping("/orders")
    @Operation(summary = "获取 AI Agent 订单记录")
    public CommonResult<PageResult<cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppAgentOrderRespVO>> getAgentOrders(
            @Valid cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppAgentOrderPageReqVO reqVO) {
        return success(agentService.getAgentOrderPage(reqVO));
    }

    @GetMapping("/positions")
    @Operation(summary = "获取 AI Agent 持仓记录")
    public CommonResult<PageResult<cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppAgentPositionRespVO>> getAgentPositions(
            @Valid cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppAgentPositionPageReqVO reqVO) {
        return success(agentService.getAgentPositionPage(reqVO));
    }

    // ========== Agent Chat 对话功能 ==========

    @Resource
    private AiAgentChatService chatService;

    @PostMapping("/chat/send")
    @Operation(summary = "发送消息（流式）")
    public Flux<ServerSentEvent<String>> sendMessage(
            @Valid @RequestBody AppAgentChatSendReqVO reqVO) {
        Long userId = getLoginUserId();
        return chatService.sendMessageStream(userId, reqVO)
                .map(data -> ServerSentEvent.<String>builder()
                        .data(data)
                        .build());
    }

    @GetMapping("/chat/sessions")
    @Operation(summary = "获取会话列表")
    @Parameter(name = "agentId", description = "Agent编号（可选）")
    public CommonResult<List<AppAgentChatSessionRespVO>> getChatSessions(
            @RequestParam(value = "agentId", required = false) Long agentId) {
        Long userId = getLoginUserId();
        return success(chatService.getSessionList(userId, agentId));
    }

    @GetMapping("/chat/history")
    @Operation(summary = "获取会话消息历史")
    @Parameter(name = "sessionId", description = "会话编号", required = true)
    public CommonResult<List<AiAgentChatMessageDO>> getChatHistory(
            @RequestParam("sessionId") Long sessionId) {
        Long userId = getLoginUserId();
        return success(chatService.getMessageHistory(sessionId, userId));
    }

    @DeleteMapping("/chat/sessions/{id}")
    @Operation(summary = "删除会话")
    @Parameter(name = "id", description = "会话编号", required = true)
    public CommonResult<Boolean> deleteSession(@PathVariable("id") Long id) {
        Long userId = getLoginUserId();
        chatService.deleteSession(id, userId);
        return success(true);
    }

    @GetMapping("/chat/quota")
    @Operation(summary = "获取剩余对话配额")
    public CommonResult<AppAgentChatQuotaRespVO> getChatQuota() {
        Long userId = getLoginUserId();
        return success(chatService.getQuota(userId));
    }

}

