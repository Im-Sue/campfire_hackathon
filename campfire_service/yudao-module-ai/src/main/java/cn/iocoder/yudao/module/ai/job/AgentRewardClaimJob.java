package cn.iocoder.yudao.module.ai.job;

import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiAgentDO;
import cn.iocoder.yudao.module.ai.service.agent.AiAgentService;
import cn.iocoder.yudao.module.market.dal.dataobject.reward.PmRewardDO;
import cn.iocoder.yudao.module.market.service.reward.PmRewardService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Agent 奖励自动领取补偿定时任务
 * 每15分钟执行一次，检查所有Agent是否有待领取的奖励，自动领取
 * 作为结算事件回调的补偿机制，防止因异常导致奖励未领取
 *
 * @author campfire
 */
@Component
@Slf4j
public class AgentRewardClaimJob {

    @Resource
    private AiAgentService agentService;

    @Resource
    private PmRewardService rewardService;

    /**
     * 每15分钟执行一次
     */
    @Scheduled(cron = "0 */15 * * * ?")
    @TenantIgnore
    public void execute() {
        log.info("[AgentRewardClaimJob] 开始检查Agent待领取奖励...");

        List<AiAgentDO> agents = agentService.getEnabledAgentList();
        int totalClaimed = 0;
        long totalAmount = 0L;

        for (AiAgentDO agent : agents) {
            try {
                Long walletUserId = agent.getWalletUserId();
                List<PmRewardDO> pendingRewards = rewardService.getPendingRewardsByUserId(walletUserId);
                if (pendingRewards == null || pendingRewards.isEmpty()) {
                    continue;
                }

                log.info("[AgentRewardClaimJob] Agent {} (walletUserId={}) 有 {} 条待领取奖励",
                        agent.getId(), walletUserId, pendingRewards.size());

                for (PmRewardDO reward : pendingRewards) {
                    try {
                        rewardService.claimReward(walletUserId, reward.getId());
                        totalClaimed++;
                        totalAmount += reward.getRewardAmount();
                    } catch (Exception e) {
                        log.warn("[AgentRewardClaimJob] Agent {} 领取奖励 {} 失败: {}",
                                agent.getId(), reward.getId(), e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.error("[AgentRewardClaimJob] 处理Agent {} 失败", agent.getId(), e);
            }
        }

        if (totalClaimed > 0) {
            log.info("[AgentRewardClaimJob] 补偿领取完成，共领取 {} 条奖励，总额 {} 积分", totalClaimed, totalAmount);
        } else {
            log.info("[AgentRewardClaimJob] 检查完成，无待领取奖励");
        }
    }

}
