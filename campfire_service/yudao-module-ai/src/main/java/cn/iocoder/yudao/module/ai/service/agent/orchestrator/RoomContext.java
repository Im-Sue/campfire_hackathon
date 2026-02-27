package cn.iocoder.yudao.module.ai.service.agent.orchestrator;

import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiAgentDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiEventRoomDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiEventRoomParticipantDO;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 房间运行上下文
 *
 * @author campfire
 */
@Data
public class RoomContext {

    /**
     * 房间信息
     */
    private AiEventRoomDO room;

    /**
     * 事件ID
     */
    private Long eventId;

    /**
     * 当前轮次
     */
    private Integer currentRound;

    /**
     * 参与的Agent列表
     */
    private List<AiAgentDO> agents;

    /**
     * 参与者信息
     */
    private List<AiEventRoomParticipantDO> participants;

    /**
     * 市场数据 (Phase 1 收集)
     */
    private Map<String, Object> marketData = new HashMap<>();

    /**
     * 外部数据 (工具收集的新闻/热点等)
     */
    private Map<String, Object> externalData = new HashMap<>();

    /**
     * 本轮讨论观点 (供后续Agent参考)
     */
    private List<String> discussionOpinions = new java.util.ArrayList<>();

    /**
     * 是否所有市场都已封盘
     */
    private boolean allMarketsClosed = false;

    /**
     * Agent余额映射 (agentId -> balance)
     */
    private Map<Long, Long> agentBalances = new HashMap<>();

    /**
     * 添加市场数据
     */
    public void setMarketData(String key, Object value) {
        this.marketData.put(key, value);
    }

    /**
     * 获取Agent余额
     */
    public Long getAgentBalance(Long agentId) {
        return agentBalances.get(agentId);
    }

    /**
     * 添加外部数据
     */
    public void setExternalData(String key, Object value) {
        this.externalData.put(key, value);
    }

    /**
     * 添加讨论观点
     */
    public void addDiscussionOpinion(String opinion) {
        this.discussionOpinions.add(opinion);
    }

}
