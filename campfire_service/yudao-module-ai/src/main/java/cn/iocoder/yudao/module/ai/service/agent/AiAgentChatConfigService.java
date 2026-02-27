package cn.iocoder.yudao.module.ai.service.agent;

import cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiAgentChatConfigUpdateReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiAgentChatConfigDO;

import java.util.List;

/**
 * AI Agent 对话配置 Service 接口
 *
 * @author campfire
 */
public interface AiAgentChatConfigService {

    /**
     * 获取所有配置列表
     *
     * @return 配置列表
     */
    List<AiAgentChatConfigDO> getConfigList();

    /**
     * 根据配置键获取配置
     *
     * @param configKey 配置键
     * @return 配置
     */
    AiAgentChatConfigDO getConfig(String configKey);

    /**
     * 获取配置值（字符串）
     *
     * @param configKey    配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    String getConfigValue(String configKey, String defaultValue);

    /**
     * 获取配置值（整数）
     *
     * @param configKey    配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    Integer getConfigIntValue(String configKey, Integer defaultValue);

    /**
     * 更新配置
     *
     * @param reqVO 配置更新请求
     */
    void updateConfig(AiAgentChatConfigUpdateReqVO reqVO);

    /**
     * 批量更新配置
     *
     * @param reqVOs 配置更新请求列表
     */
    void batchUpdateConfig(List<AiAgentChatConfigUpdateReqVO> reqVOs);

}
