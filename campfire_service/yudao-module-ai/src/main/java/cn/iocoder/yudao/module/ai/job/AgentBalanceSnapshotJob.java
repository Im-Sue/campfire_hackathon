package cn.iocoder.yudao.module.ai.job;

import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiAgentBalanceSnapshotDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiAgentDO;
import cn.iocoder.yudao.module.ai.dal.mysql.agent.AiAgentBalanceSnapshotMapper;
import cn.iocoder.yudao.module.ai.service.agent.AiAgentService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Agent 余额快照定时任务
 * 每小时执行一次，记录所有Agent的余额快照
 *
 * @author campfire
 */
@Component
@Slf4j
public class AgentBalanceSnapshotJob {

    @Resource
    private AiAgentService agentService;

    @Resource
    private AiAgentBalanceSnapshotMapper snapshotMapper;

    /**
     * 每5分钟执行一次余额快照
     */
    @Scheduled(cron = "0 */5 * * * ?")
    @TenantIgnore
    public void execute() {
        log.info("[AgentBalanceSnapshotJob] 开始执行余额快照...");
        
        List<AiAgentDO> agents = agentService.getEnabledAgentList();
        int count = 0;
        
        for (AiAgentDO agent : agents) {
            try {
                Long balance = agentService.getAvailableBalance(agent.getId());
                
                AiAgentBalanceSnapshotDO snapshot = new AiAgentBalanceSnapshotDO();
                snapshot.setAgentId(agent.getId());
                snapshot.setBalance(balance != null ? balance : 0L);
                snapshot.setTriggerType(1); // 定时触发
                snapshot.setSnapshotTime(LocalDateTime.now());
                
                snapshotMapper.insert(snapshot);
                count++;
            } catch (Exception e) {
                log.error("[AgentBalanceSnapshotJob] Agent {} 快照失败", agent.getId(), e);
            }
        }
        
        log.info("[AgentBalanceSnapshotJob] 余额快照完成，共处理 {} 个Agent", count);
    }

}
