package cn.iocoder.yudao.module.ai.job;

import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiAgentDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiAgentPeriodStatsDO;
import cn.iocoder.yudao.module.ai.dal.mysql.agent.AiAgentPeriodStatsMapper;
import cn.iocoder.yudao.module.ai.service.agent.AiAgentService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;

/**
 * Agent 赛季统计定时任务
 * 每日0点执行，统计日榜/周榜
 *
 * @author campfire
 */
@Component
@Slf4j
public class SeasonStatsJob {

    @Resource
    private AiAgentService agentService;

    @Resource
    private AiAgentPeriodStatsMapper periodStatsMapper;

    /**
     * 每日0点执行统计
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @TenantIgnore
    public void execute() {
        log.info("[SeasonStatsJob] 开始执行赛季统计...");
        
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String dailyKey = yesterday.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String weeklyKey = getWeekKey(yesterday);
        String monthlyKey = yesterday.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        
        List<AiAgentDO> agents = agentService.getEnabledAgentList();
        
        for (AiAgentDO agent : agents) {
            try {
                // 更新日统计
                updateOrCreatePeriodStats(agent, 1, dailyKey);
                
                // 如果是周一,创建新的周统计
                if (yesterday.getDayOfWeek().getValue() == 7) {
                    updateOrCreatePeriodStats(agent, 2, weeklyKey);
                }
                
                // 如果是月末,创建新的月统计
                if (yesterday.getDayOfMonth() == yesterday.lengthOfMonth()) {
                    updateOrCreatePeriodStats(agent, 3, monthlyKey);
                }
            } catch (Exception e) {
                log.error("[SeasonStatsJob] Agent {} 统计失败", agent.getId(), e);
            }
        }
        
        // 计算排名
        calculateRankings(1, dailyKey);
        
        log.info("[SeasonStatsJob] 赛季统计完成");
    }

    private void updateOrCreatePeriodStats(AiAgentDO agent, Integer periodType, String periodKey) {
        AiAgentPeriodStatsDO existing = periodStatsMapper.selectByAgentAndPeriod(
                agent.getId(), periodType, periodKey);
        
        if (existing == null) {
            AiAgentPeriodStatsDO stats = new AiAgentPeriodStatsDO();
            stats.setAgentId(agent.getId());
            stats.setPeriodType(periodType);
            stats.setPeriodKey(periodKey);
            stats.setEventCount(agent.getTotalEvents());
            stats.setWinCount(agent.getWinCount());
            stats.setProfit(agent.getTotalProfit());
            stats.setCreateTime(LocalDateTime.now());
            stats.setUpdateTime(LocalDateTime.now());
            periodStatsMapper.insert(stats);
        } else {
            existing.setEventCount(agent.getTotalEvents());
            existing.setWinCount(agent.getWinCount());
            existing.setProfit(agent.getTotalProfit());
            existing.setUpdateTime(LocalDateTime.now());
            periodStatsMapper.updateById(existing);
        }
    }

    private void calculateRankings(Integer periodType, String periodKey) {
        List<AiAgentPeriodStatsDO> statsList = periodStatsMapper.selectListByPeriodOrderByProfit(
                periodType, periodKey, null);
        
        int rank = 1;
        for (AiAgentPeriodStatsDO stats : statsList) {
            stats.setRanking(rank++);
            periodStatsMapper.updateById(stats);
        }
    }

    private String getWeekKey(LocalDate date) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int year = date.getYear();
        int week = date.get(weekFields.weekOfWeekBasedYear());
        return String.format("%d-W%02d", year, week);
    }

}
