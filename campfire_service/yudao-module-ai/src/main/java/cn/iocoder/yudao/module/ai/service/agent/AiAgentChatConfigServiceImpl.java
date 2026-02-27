package cn.iocoder.yudao.module.ai.service.agent;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiAgentChatConfigUpdateReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiAgentChatConfigDO;
import cn.iocoder.yudao.module.ai.dal.mysql.agent.AiAgentChatConfigMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI Agent 对话配置 Service 实现类
 *
 * @author campfire
 */
@Service
@Slf4j
public class AiAgentChatConfigServiceImpl implements AiAgentChatConfigService {

    @Resource
    private AiAgentChatConfigMapper agentChatConfigMapper;

    /**
     * 配置缓存
     */
    private final ConcurrentHashMap<String, String> configCache = new ConcurrentHashMap<>();

    @Override
    public List<AiAgentChatConfigDO> getConfigList() {
        return agentChatConfigMapper.selectList();
    }

    @Override
    public AiAgentChatConfigDO getConfig(String configKey) {
        return agentChatConfigMapper.selectByConfigKey(configKey);
    }

    @Override
    public String getConfigValue(String configKey, String defaultValue) {
        // 优先从缓存获取
        String cachedValue = configCache.get(configKey);
        if (cachedValue != null) {
            return cachedValue;
        }

        // 查数据库
        AiAgentChatConfigDO config = agentChatConfigMapper.selectByConfigKey(configKey);
        if (config != null && StrUtil.isNotEmpty(config.getConfigValue())) {
            configCache.put(configKey, config.getConfigValue());
            return config.getConfigValue();
        }

        return defaultValue;
    }

    @Override
    public Integer getConfigIntValue(String configKey, Integer defaultValue) {
        String value = getConfigValue(configKey, null);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("[getConfigIntValue] 配置值转换整数失败, key={}, value={}", configKey, value);
            return defaultValue;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(AiAgentChatConfigUpdateReqVO reqVO) {
        AiAgentChatConfigDO config = agentChatConfigMapper.selectByConfigKey(reqVO.getConfigKey());
        if (config == null) {
            // 不存在则创建
            config = AiAgentChatConfigDO.builder()
                    .configKey(reqVO.getConfigKey())
                    .configValue(reqVO.getConfigValue())
                    .description(reqVO.getDescription())
                    .build();
            agentChatConfigMapper.insert(config);
        } else {
            // 存在则更新
            config.setConfigValue(reqVO.getConfigValue());
            if (reqVO.getDescription() != null) {
                config.setDescription(reqVO.getDescription());
            }
            agentChatConfigMapper.updateById(config);
        }

        // 清除缓存
        configCache.remove(reqVO.getConfigKey());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateConfig(List<AiAgentChatConfigUpdateReqVO> reqVOs) {
        for (AiAgentChatConfigUpdateReqVO reqVO : reqVOs) {
            updateConfig(reqVO);
        }
    }

}
