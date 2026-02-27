package cn.iocoder.yudao.module.ai.controller.admin.agent;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiAgentChatConfigRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiAgentChatConfigUpdateReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiAgentChatConfigDO;
import cn.iocoder.yudao.module.ai.dal.mysql.agent.AiAgentChatConfigMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * AI Agent 对话配置管理 Controller
 *
 * @author campfire
 */
@Tag(name = "管理后台 - AI Agent 对话配置")
@RestController
@RequestMapping("/ai/agent/chat-config")
@Validated
public class AiAgentChatConfigController {

    @Resource
    private AiAgentChatConfigMapper agentChatConfigMapper;

    @GetMapping("/list")
    @Operation(summary = "获取所有配置")
    @PreAuthorize("@ss.hasPermission('ai:agent:query')")
    public CommonResult<List<AiAgentChatConfigRespVO>> getConfigList() {
        List<AiAgentChatConfigDO> configs = agentChatConfigMapper.selectList();
        
        List<AiAgentChatConfigRespVO> result = configs.stream()
                .map(config -> new AiAgentChatConfigRespVO()
                        .setId(config.getId())
                        .setConfigKey(config.getConfigKey())
                        .setConfigValue(config.getConfigValue())
                        .setDescription(config.getDescription()))
                .collect(Collectors.toList());
        
        return success(result);
    }

    @PutMapping("/update")
    @Operation(summary = "更新配置")
    @PreAuthorize("@ss.hasPermission('ai:agent:update')")
    public CommonResult<Boolean> updateConfig(@Valid @RequestBody AiAgentChatConfigUpdateReqVO reqVO) {
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
        return success(true);
    }

    @PutMapping("/batch-update")
    @Operation(summary = "批量更新配置")
    @PreAuthorize("@ss.hasPermission('ai:agent:update')")
    public CommonResult<Boolean> batchUpdateConfig(@Valid @RequestBody List<AiAgentChatConfigUpdateReqVO> reqVOs) {
        for (AiAgentChatConfigUpdateReqVO reqVO : reqVOs) {
            AiAgentChatConfigDO config = agentChatConfigMapper.selectByConfigKey(reqVO.getConfigKey());
            if (config == null) {
                config = AiAgentChatConfigDO.builder()
                        .configKey(reqVO.getConfigKey())
                        .configValue(reqVO.getConfigValue())
                        .description(reqVO.getDescription())
                        .build();
                agentChatConfigMapper.insert(config);
            } else {
                config.setConfigValue(reqVO.getConfigValue());
                if (reqVO.getDescription() != null) {
                    config.setDescription(reqVO.getDescription());
                }
                agentChatConfigMapper.updateById(config);
            }
        }
        return success(true);
    }

}
