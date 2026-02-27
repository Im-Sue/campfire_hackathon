package cn.iocoder.yudao.module.ai.service.agent;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiEventRoomMessagePageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiEventRoomMessageRespVO;
import cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppAgentTimelineReqVO;
import cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppAgentTimelineRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiAgentDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiEventRoomMessageDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatRoleDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.mysql.agent.AiEventRoomMessageMapper;
import cn.iocoder.yudao.module.ai.service.model.AiChatRoleService;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AI 房间消息 Service 实现类
 *
 * @author campfire
 */
@Service
@Validated
@Slf4j
public class AiEventRoomMessageServiceImpl implements AiEventRoomMessageService {

    @Resource
    private AiEventRoomMessageMapper messageMapper;

    @Resource
    private AiAgentService agentService;

    @Resource
    private AiChatRoleService chatRoleService;

    @Resource
    private AiModelService modelService;

    @Override
    public Long createMessage(AiEventRoomMessageDO message) {
        messageMapper.insert(message);
        return message.getId();
    }

    @Override
    public PageResult<AiEventRoomMessageDO> getMessagePage(AiEventRoomMessagePageReqVO pageReqVO) {
        return messageMapper.selectPage(pageReqVO);
    }

    @Override
    public List<AiEventRoomMessageDO> getMessagesByRoomAndRound(Long roomId, Integer round) {
        return messageMapper.selectListByRoomIdAndRound(roomId, round);
    }

    @Override
    public List<AiEventRoomMessageDO> getMessagesByRoomId(Long roomId) {
        return messageMapper.selectListByRoomId(roomId);
    }

    @Override
    public List<AiEventRoomMessageDO> getMessagesAfter(Long roomId, Long afterMessageId) {
        return messageMapper.selectListByRoomIdAfterMessageId(roomId, afterMessageId);
    }

    @Override
    public List<AiEventRoomMessageRespVO> getMessagesByRoomIdWithDetails(Long roomId) {
        List<AiEventRoomMessageDO> messages = messageMapper.selectListByRoomId(roomId);
        return convertToRespVOList(messages);
    }

    @Override
    public List<AiEventRoomMessageRespVO> getMessagesAfterWithDetails(Long roomId, Long afterMessageId) {
        List<AiEventRoomMessageDO> messages = messageMapper.selectListByRoomIdAfterMessageId(roomId, afterMessageId);
        return convertToRespVOList(messages);
    }

    /**
     * 转换消息列表为响应VO列表（包含Agent信息）
     */
    private List<AiEventRoomMessageRespVO> convertToRespVOList(List<AiEventRoomMessageDO> messages) {
        List<AiEventRoomMessageRespVO> result = new ArrayList<>();
        for (AiEventRoomMessageDO message : messages) {
            AiEventRoomMessageRespVO vo = cn.iocoder.yudao.framework.common.util.object.BeanUtils.toBean(
                message, AiEventRoomMessageRespVO.class);

            // 关联查询Agent信息
            AiAgentDO agent = agentService.getAgent(message.getAgentId());
            if (agent != null) {
                vo.setAgentName(agent.getName());
                vo.setAgentAvatar(agent.getAvatar());
            }

            result.add(vo);
        }
        return result;
    }

    @Override
    public PageResult<AppAgentTimelineRespVO> getGlobalTimeline(AppAgentTimelineReqVO reqVO) {
        // 1. 构建查询条件
        LambdaQueryWrapper<AiEventRoomMessageDO> wrapper = new LambdaQueryWrapper<>();

        // 只查询正常状态的消息
        wrapper.eq(AiEventRoomMessageDO::getStatus, 1);

        // 消息类型筛选
        wrapper.eq(reqVO.getMessageType() != null,
                   AiEventRoomMessageDO::getMessageType, reqVO.getMessageType());

        // Agent 筛选
        wrapper.eq(reqVO.getAgentId() != null,
                   AiEventRoomMessageDO::getAgentId, reqVO.getAgentId());

        // 按创建时间倒序排序（最新的在前）
        wrapper.orderByDesc(AiEventRoomMessageDO::getCreateTime);

        // 2. 分页查询
        PageResult<AiEventRoomMessageDO> pageResult = messageMapper.selectPage(reqVO, wrapper);

        // 3. 批量获取 Agent 信息（避免 N+1 问题）
        Set<Long> agentIds = pageResult.getList().stream()
            .map(AiEventRoomMessageDO::getAgentId)
            .collect(Collectors.toSet());

        Map<Long, AiAgentDO> agentMap = agentService.getAgentsByIds(new ArrayList<>(agentIds))
            .stream()
            .collect(Collectors.toMap(AiAgentDO::getId, Function.identity()));

        // 5. 批量获取 Role 信息（避免 N+1 问题）
        Set<Long> roleIds = agentMap.values().stream()
            .map(AiAgentDO::getRoleId)
            .filter(roleId -> roleId != null)
            .collect(Collectors.toSet());

        Map<Long, AiChatRoleDO> roleMap = chatRoleService.getChatRoleList(new ArrayList<>(roleIds))
            .stream()
            .collect(Collectors.toMap(AiChatRoleDO::getId, Function.identity()));

        // 6. 批量获取 Model 信息（避免 N+1 问题）
        Set<Long> modelIds = roleMap.values().stream()
            .map(AiChatRoleDO::getModelId)
            .filter(modelId -> modelId != null)
            .collect(Collectors.toSet());

        Map<Long, AiModelDO> modelMap = modelIds.isEmpty() ? new java.util.HashMap<>() :
            modelIds.stream()
                .map(modelService::getModel)
                .filter(model -> model != null)
                .collect(Collectors.toMap(AiModelDO::getId, Function.identity()));

        // 7. 转换为 VO
        List<AppAgentTimelineRespVO> voList = pageResult.getList().stream()
            .map(message -> convertToTimelineVO(message, agentMap, roleMap, modelMap))
            .collect(Collectors.toList());

        return new PageResult<>(voList, pageResult.getTotal());
    }

    private AppAgentTimelineRespVO convertToTimelineVO(
            AiEventRoomMessageDO message,
            Map<Long, AiAgentDO> agentMap,
            Map<Long, AiChatRoleDO> roleMap,
            Map<Long, AiModelDO> modelMap) {

        AppAgentTimelineRespVO vo = new AppAgentTimelineRespVO();
        vo.setId(message.getId());
        vo.setAgentId(message.getAgentId());
        vo.setMessageType(message.getMessageType());
        vo.setContent(message.getContent());
        vo.setStructuredData(message.getStructuredData());
        vo.setCreateTime(message.getCreateTime());

        // 获取 Agent 信息
        AiAgentDO agent = agentMap.get(message.getAgentId());
        if (agent != null) {
            vo.setAgentName(agent.getName());
            vo.setAgentAvatar(agent.getAvatar());
            
            // 获取模型名称：Agent -> Role -> Model
            AiChatRoleDO role = roleMap.get(agent.getRoleId());
            if (role != null && role.getModelId() != null) {
                AiModelDO model = modelMap.get(role.getModelId());
                if (model != null) {
                    vo.setAgentModelName(model.getName());
                }
            }
        }

        // 消息类型描述
        vo.setMessageTypeDesc(getMessageTypeDesc(message.getMessageType()));

        return vo;
    }

    private String getMessageTypeDesc(Integer messageType) {
        return switch (messageType) {
            case 1 -> "市场数据";
            case 2 -> "讨论";
            case 3 -> "决策";
            case 4 -> "执行";
            default -> "未知";
        };
    }

}
