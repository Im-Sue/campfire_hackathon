package cn.iocoder.yudao.module.market.job;

import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.market.dal.dataobject.settlement.PmSettlementDO;
import cn.iocoder.yudao.module.market.service.settlement.PmSettlementService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 结算自动确认与执行定时任务
 *
 * 每10分钟执行一次，自动处理待确认和待执行的结算记录。
 * 作为后台管理页面手动操作的补充，两者互不冲突：
 * - confirmSettlement 有状态检查，只处理 PENDING 状态
 * - executeSettlement 有幂等保护，COMPLETED 状态直接跳过
 *
 * @author campfire
 */
@Component
@Slf4j
public class SettlementAutoProcessJob {

    @Resource
    private PmSettlementService pmSettlementService;

    /**
     * 系统自动确认使用的管理员ID
     */
    private static final Long SYSTEM_ADMIN_ID = 0L;

    /**
     * 每10分钟执行一次
     */
    @Scheduled(cron = "0 */10 * * * ?")
    @TenantIgnore
    public void execute() {
        log.info("[SettlementAutoProcessJob] 开始检查待处理结算...");

        int confirmedCount = 0;
        int executedCount = 0;

        // 阶段1: 自动确认 — 将 PENDING(0) 状态改为 CONFIRMED(1)
        try {
            List<PmSettlementDO> pendingList = pmSettlementService.getPendingSettlements();
            if (!pendingList.isEmpty()) {
                log.info("[SettlementAutoProcessJob] 发现 {} 条待确认结算", pendingList.size());
                for (PmSettlementDO settlement : pendingList) {
                    try {
                        pmSettlementService.confirmSettlement(settlement.getId(), SYSTEM_ADMIN_ID);
                        confirmedCount++;
                        log.info("[SettlementAutoProcessJob] 自动确认结算 settlementId={}, marketId={}",
                                settlement.getId(), settlement.getMarketId());
                    } catch (Exception e) {
                        log.warn("[SettlementAutoProcessJob] 确认结算 {} 失败: {}",
                                settlement.getId(), e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("[SettlementAutoProcessJob] 查询待确认结算失败", e);
        }

        // 阶段2: 自动执行 — 将 CONFIRMED(1) 状态改为 COMPLETED(2)，生成奖励
        try {
            List<PmSettlementDO> confirmedList = pmSettlementService.getConfirmedSettlements();
            if (!confirmedList.isEmpty()) {
                log.info("[SettlementAutoProcessJob] 发现 {} 条待执行结算", confirmedList.size());
                for (PmSettlementDO settlement : confirmedList) {
                    try {
                        pmSettlementService.executeSettlement(settlement.getId());
                        executedCount++;
                        log.info("[SettlementAutoProcessJob] 自动执行结算 settlementId={}, marketId={}",
                                settlement.getId(), settlement.getMarketId());
                    } catch (Exception e) {
                        log.warn("[SettlementAutoProcessJob] 执行结算 {} 失败: {}",
                                settlement.getId(), e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("[SettlementAutoProcessJob] 查询待执行结算失败", e);
        }

        if (confirmedCount > 0 || executedCount > 0) {
            log.info("[SettlementAutoProcessJob] 处理完成，自动确认 {} 条，自动执行 {} 条",
                    confirmedCount, executedCount);
        } else {
            log.info("[SettlementAutoProcessJob] 检查完成，无待处理结算");
        }
    }

}
