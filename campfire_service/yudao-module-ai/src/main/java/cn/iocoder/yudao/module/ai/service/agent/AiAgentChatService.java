package cn.iocoder.yudao.module.ai.service.agent;

import cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppAgentChatQuotaRespVO;
import cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppAgentChatSendReqVO;
import cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppAgentChatSessionRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiAgentChatMessageDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiAgentChatSessionDO;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * AI Agent C端对话 Service 接口
 *
 * @author campfire
 */
public interface AiAgentChatService {

    /**
     * 发送消息（流式响应）
     *
     * @param userId 用户编号
     * @param reqVO  请求
     * @return SSE事件流
     */
    Flux<String> sendMessageStream(Long userId, AppAgentChatSendReqVO reqVO);

    /**
     * 获取或创建会话（混合模式）
     *
     * 规则：
     * 1. 如果24小时内有会话，继续使用
     * 2. 如果超过24小时或无会话，创建新会话
     * 3. 如果forceNew=true，强制创建新会话
     *
     * @param userId  用户编号
     * @param agentId Agent编号
     * @param forceNew 是否强制新建
     * @return 会话
     */
    AiAgentChatSessionDO getOrCreateSession(Long userId, Long agentId, boolean forceNew);

    /**
     * 获取会话列表
     *
     * @param userId  用户编号
     * @param agentId Agent编号（可选）
     * @return 会话列表
     */
    List<AppAgentChatSessionRespVO> getSessionList(Long userId, Long agentId);

    /**
     * 获取会话消息历史
     *
     * @param sessionId 会话编号
     * @param userId    用户编号
     * @return 消息列表
     */
    List<AiAgentChatMessageDO> getMessageHistory(Long sessionId, Long userId);

    /**
     * 删除会话
     *
     * @param sessionId 会话编号
     * @param userId    用户编号
     */
    void deleteSession(Long sessionId, Long userId);

    /**
     * 获取用户剩余配额
     *
     * @param userId 用户编号
     * @return 配额信息
     */
    AppAgentChatQuotaRespVO getQuota(Long userId);

    /**
     * 检查并扣减配额
     *
     * @param userId 用户编号
     * @return 是否有配额（true=免费次数内, false=需要扣积分）
     * @throws cn.iocoder.yudao.framework.common.exception.ServiceException 如果无配额且积分不足
     */
    boolean checkAndDeductQuota(Long userId);

    /**
     * 获取配置值
     *
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    int getConfigIntValue(String key, int defaultValue);

}
