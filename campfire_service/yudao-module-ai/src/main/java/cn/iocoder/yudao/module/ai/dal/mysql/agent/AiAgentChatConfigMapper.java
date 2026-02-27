package cn.iocoder.yudao.module.ai.dal.mysql.agent;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiAgentChatConfigDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI Agent 对话配置 Mapper
 */
@Mapper
public interface AiAgentChatConfigMapper extends BaseMapperX<AiAgentChatConfigDO> {

    /**
     * 根据配置键查询配置
     *
     * @param configKey 配置键
     * @return 配置
     */
    default AiAgentChatConfigDO selectByConfigKey(String configKey) {
        return selectOne(AiAgentChatConfigDO::getConfigKey, configKey);
    }

}
