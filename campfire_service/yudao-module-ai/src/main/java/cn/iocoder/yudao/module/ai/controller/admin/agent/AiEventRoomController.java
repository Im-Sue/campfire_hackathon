package cn.iocoder.yudao.module.ai.controller.admin.agent;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.agent.vo.*;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiAgentDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiEventRoomDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiEventRoomMessageDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiEventRoomParticipantDO;
import cn.iocoder.yudao.module.ai.service.agent.AiAgentService;
import cn.iocoder.yudao.module.ai.service.agent.AiEventRoomMessageService;
import cn.iocoder.yudao.module.ai.service.agent.AiEventRoomService;
import cn.iocoder.yudao.module.ai.service.agent.dto.AgentParticipantInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * AI 事件房间 管理后台 Controller
 *
 * @author campfire
 */
@Tag(name = "管理后台 - AI 事件房间")
@RestController
@RequestMapping("/ai/event-room")
@Validated
public class AiEventRoomController {

    @Resource
    private AiEventRoomService roomService;

    @Resource
    private AiEventRoomMessageService messageService;

    @Resource
    private AiAgentService agentService;

    @PostMapping("/create")
    @Operation(summary = "创建房间")
    @PreAuthorize("@ss.hasPermission('ai:event-room:create')")
    public CommonResult<Long> createRoom(@Valid @RequestBody AiEventRoomCreateReqVO createReqVO) {
        // 1. 准备Agent参与者信息
        List<AgentParticipantInfo> participantInfos = prepareAgentParticipants(createReqVO);

        // 2. 调用Service创建房间
        return success(roomService.createRoom(createReqVO, participantInfos));
    }

    /**
     * 准备Agent参与者信息
     */
    private List<AgentParticipantInfo> prepareAgentParticipants(AiEventRoomCreateReqVO createReqVO) {
        // 获取参与的Agent列表
        List<AiAgentDO> agents;
        if (createReqVO.getAgentIds() != null && !createReqVO.getAgentIds().isEmpty()) {
            // 逐个获取指定的Agent
            agents = new ArrayList<>();
            for (Long agentId : createReqVO.getAgentIds()) {
                AiAgentDO agent = agentService.getAgent(agentId);
                if (agent != null) {
                    agents.add(agent);
                }
            }
        } else {
            agents = agentService.getEnabledAgentList();
        }

        // 为每个Agent查询余额
        List<AgentParticipantInfo> participantInfos = new ArrayList<>();
        for (AiAgentDO agent : agents) {
            AgentParticipantInfo info = new AgentParticipantInfo();
            info.setAgentId(agent.getId());
            info.setInitialBalance(agentService.getAvailableBalance(agent.getId()));
            participantInfos.add(info);
        }

        return participantInfos;
    }

    @PostMapping("/start")
    @Operation(summary = "启动房间")
    @Parameter(name = "id", description = "房间ID", required = true)
    @PreAuthorize("@ss.hasPermission('ai:event-room:update')")
    public CommonResult<Boolean> startRoom(@RequestParam("id") Long id) {
        roomService.startRoom(id);
        return success(true);
    }

    @PostMapping("/pause")
    @Operation(summary = "暂停房间")
    @Parameter(name = "id", description = "房间ID", required = true)
    @PreAuthorize("@ss.hasPermission('ai:event-room:update')")
    public CommonResult<Boolean> pauseRoom(@RequestParam("id") Long id) {
        roomService.pauseRoom(id);
        return success(true);
    }

    @PostMapping("/stop")
    @Operation(summary = "停止房间")
    @Parameter(name = "id", description = "房间ID", required = true)
    @PreAuthorize("@ss.hasPermission('ai:event-room:update')")
    public CommonResult<Boolean> stopRoom(@RequestParam("id") Long id) {
        roomService.stopRoom(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取房间")
    @Parameter(name = "id", description = "房间ID", required = true)
    @PreAuthorize("@ss.hasPermission('ai:event-room:query')")
    public CommonResult<AiEventRoomDO> getRoom(@RequestParam("id") Long id) {
        return success(roomService.getRoom(id));
    }

    @GetMapping("/get-by-event")
    @Operation(summary = "根据事件ID获取房间")
    @Parameter(name = "eventId", description = "事件ID", required = true)
    @PreAuthorize("@ss.hasPermission('ai:event-room:query')")
    public CommonResult<AiEventRoomDO> getRoomByEventId(@RequestParam("eventId") Long eventId) {
        return success(roomService.getRoomByEventId(eventId));
    }

    @GetMapping("/page")
    @Operation(summary = "获取房间分页")
    @PreAuthorize("@ss.hasPermission('ai:event-room:query')")
    public CommonResult<PageResult<AiEventRoomRespVO>> getRoomPage(@Valid AiEventRoomPageReqVO pageReqVO) {
        return success(roomService.getRoomPageWithDetails(pageReqVO));
    }

    @GetMapping("/participants")
    @Operation(summary = "获取房间参与者")
    @Parameter(name = "roomId", description = "房间ID", required = true)
    @PreAuthorize("@ss.hasPermission('ai:event-room:query')")
    public CommonResult<List<AiEventRoomParticipantRespVO>> getRoomParticipants(@RequestParam("roomId") Long roomId) {
        return success(roomService.getRoomParticipantsWithDetails(roomId));
    }

    @GetMapping("/get-with-details")
    @Operation(summary = "获取房间详情（包含扩展信息）")
    @Parameter(name = "id", description = "房间ID", required = true)
    @PreAuthorize("@ss.hasPermission('ai:event-room:query')")
    public CommonResult<AiEventRoomRespVO> getRoomWithDetails(@RequestParam("id") Long id) {
        return success(roomService.getRoomWithDetails(id));
    }

    @GetMapping("/markets")
    @Operation(summary = "获取房间关联的市场价格")
    @Parameter(name = "roomId", description = "房间ID", required = true)
    @PreAuthorize("@ss.hasPermission('ai:event-room:query')")
    public CommonResult<List<RoomMarketRespVO>> getRoomMarkets(@RequestParam("roomId") Long roomId) {
        return success(roomService.getRoomMarkets(roomId));
    }

    @GetMapping("/messages")
    @Operation(summary = "获取房间消息")
    @PreAuthorize("@ss.hasPermission('ai:event-room:query')")
    public CommonResult<List<AiEventRoomMessageRespVO>> getRoomMessages(
            @RequestParam("roomId") Long roomId,
            @RequestParam(value = "afterId", required = false) Long afterId) {
        if (afterId != null) {
            return success(messageService.getMessagesAfterWithDetails(roomId, afterId));
        }
        return success(messageService.getMessagesByRoomIdWithDetails(roomId));
    }

    @GetMapping("/message/page")
    @Operation(summary = "获取消息分页")
    @PreAuthorize("@ss.hasPermission('ai:event-room:query')")
    public CommonResult<PageResult<AiEventRoomMessageDO>> getMessagePage(@Valid AiEventRoomMessagePageReqVO pageReqVO) {
        return success(messageService.getMessagePage(pageReqVO));
    }

}
