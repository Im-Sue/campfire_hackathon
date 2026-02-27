package cn.iocoder.yudao.module.market.service.dashboard;

import cn.iocoder.yudao.module.market.controller.admin.dashboard.vo.DashboardOverviewRespVO;
import cn.iocoder.yudao.module.market.controller.admin.dashboard.vo.DashboardOverviewRespVO.*;
import cn.iocoder.yudao.module.market.controller.admin.dashboard.vo.DashboardPendingRespVO;
import cn.iocoder.yudao.module.market.controller.admin.dashboard.vo.DashboardPendingRespVO.*;
import cn.iocoder.yudao.module.market.controller.admin.dashboard.vo.DashboardTrendRespVO;
import cn.iocoder.yudao.module.market.controller.admin.dashboard.vo.DashboardTrendRespVO.TrendItem;
import cn.iocoder.yudao.module.market.dal.mysql.event.PmEventMapper;
import cn.iocoder.yudao.module.market.dal.mysql.market.PmMarketMapper;
import cn.iocoder.yudao.module.market.dal.mysql.order.PmOrderMapper;
import cn.iocoder.yudao.module.market.dal.mysql.reward.PmRewardMapper;
import cn.iocoder.yudao.module.market.dal.mysql.settlement.PmSettlementMapper;
import cn.iocoder.yudao.module.market.ws.PolymarketWsManager;
import cn.iocoder.yudao.module.point.dal.mysql.PointAccountMapper;
import cn.iocoder.yudao.module.point.dal.mysql.PointTransactionMapper;
import cn.iocoder.yudao.module.social.dal.mysql.SocialCommentMapper;
import cn.iocoder.yudao.module.social.dal.mysql.SocialLikeMapper;
import cn.iocoder.yudao.module.social.dal.mysql.SocialPostMapper;
import cn.iocoder.yudao.module.wallet.dal.mysql.WalletUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据概览 Service 实现类
 */
@Service
@Validated
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private static final int OVERDUE_DAYS = 7; // 超期天数阈值

    @Resource
    private WalletUserMapper walletUserMapper;
    @Resource
    private PointAccountMapper pointAccountMapper;
    @Resource
    private PointTransactionMapper pointTransactionMapper;
    @Resource
    private PmOrderMapper pmOrderMapper;
    @Resource
    private PmEventMapper pmEventMapper;
    @Resource
    private PmMarketMapper pmMarketMapper;
    @Resource
    private PmSettlementMapper pmSettlementMapper;
    @Resource
    private PmRewardMapper pmRewardMapper;
    @Resource
    private SocialPostMapper socialPostMapper;
    @Resource
    private SocialCommentMapper socialCommentMapper;
    @Resource
    private SocialLikeMapper socialLikeMapper;
    @Resource
    private PolymarketWsManager polymarketWsManager;

    @Override
    public DashboardOverviewRespVO getOverview() {
        DashboardOverviewRespVO vo = new DashboardOverviewRespVO();
        vo.setUsers(buildUserStats());
        vo.setPoints(buildPointStats());
        vo.setOrders(buildOrderStats());
        vo.setMarkets(buildMarketStats());
        vo.setRewards(buildRewardStats());
        vo.setSocial(buildSocialStats());
        return vo;
    }

    @Override
    public DashboardTrendRespVO getUserTrend(String dimension) {
        TimeRange range = getTimeRange(dimension);
        String groupFormat = getGroupFormat(dimension);

        // 使用 BaseMapperX 的 selectCount 和分组查询
        List<TrendItem> items = new ArrayList<>();

        // 根据维度生成时间点
        List<String> timePoints = generateTimePoints(dimension, range);

        // 查询每个时间点的数据
        Long cumulativeTotal = walletUserMapper.selectCount(); // 截止到开始时间的累计
        for (String timePoint : timePoints) {
            LocalDateTime[] pointRange = getTimePointRange(dimension, timePoint, range);
            Long newUsers = countUsersByTimeRange(pointRange[0], pointRange[1]);

            TrendItem item = new TrendItem();
            item.setTime(timePoint);
            item.setValue1(newUsers); // 新增用户
            item.setValue2(cumulativeTotal); // 累计用户
            items.add(item);

            cumulativeTotal += newUsers;
        }

        return new DashboardTrendRespVO(dimension, items);
    }

    @Override
    public DashboardTrendRespVO getOrderTrend(String dimension) {
        TimeRange range = getTimeRange(dimension);
        List<String> timePoints = generateTimePoints(dimension, range);
        List<TrendItem> items = new ArrayList<>();

        for (String timePoint : timePoints) {
            LocalDateTime[] pointRange = getTimePointRange(dimension, timePoint, range);

            // 统计买单和卖单
            Long buyOrders = countOrdersByTimeRangeAndSide(pointRange[0], pointRange[1], 1); // side=1 买入
            Long sellOrders = countOrdersByTimeRangeAndSide(pointRange[0], pointRange[1], 2); // side=2 卖出

            TrendItem item = new TrendItem();
            item.setTime(timePoint);
            item.setValue1(buyOrders);
            item.setValue2(sellOrders);
            item.setValue3(buyOrders + sellOrders); // 总订单
            items.add(item);
        }

        return new DashboardTrendRespVO(dimension, items);
    }

    @Override
    public DashboardTrendRespVO getPointTrend(String dimension) {
        TimeRange range = getTimeRange(dimension);
        List<String> timePoints = generateTimePoints(dimension, range);
        List<TrendItem> items = new ArrayList<>();

        for (String timePoint : timePoints) {
            LocalDateTime[] pointRange = getTimePointRange(dimension, timePoint, range);

            // 统计收入(amount > 0)和支出(amount < 0)
            Long income = sumPointsByTimeRangePositive(pointRange[0], pointRange[1]);
            Long expense = sumPointsByTimeRangeNegative(pointRange[0], pointRange[1]);

            TrendItem item = new TrendItem();
            item.setTime(timePoint);
            item.setValue1(income);
            item.setValue2(expense);
            item.setValue3(income - expense); // 净流入
            items.add(item);
        }

        return new DashboardTrendRespVO(dimension, items);
    }

    @Override
    public DashboardTrendRespVO getSocialTrend(String dimension) {
        TimeRange range = getTimeRange(dimension);
        List<String> timePoints = generateTimePoints(dimension, range);
        List<TrendItem> items = new ArrayList<>();

        for (String timePoint : timePoints) {
            LocalDateTime[] pointRange = getTimePointRange(dimension, timePoint, range);

            Long posts = countPostsByTimeRange(pointRange[0], pointRange[1]);
            Long comments = countCommentsByTimeRange(pointRange[0], pointRange[1]);
            Long likes = countLikesByTimeRange(pointRange[0], pointRange[1]);

            TrendItem item = new TrendItem();
            item.setTime(timePoint);
            item.setValue1(posts);
            item.setValue2(comments);
            item.setValue3(likes);
            items.add(item);
        }

        return new DashboardTrendRespVO(dimension, items);
    }

    @Override
    public DashboardPendingRespVO getPending() {
        DashboardPendingRespVO vo = new DashboardPendingRespVO();

        // 待审核帖子
        PendingItem pendingPosts = new PendingItem();
        pendingPosts.setCount(countPendingPosts());
        pendingPosts.setUrl("/social/post?status=1");
        vo.setPendingPosts(pendingPosts);

        // 待确认结算
        PendingItem pendingSettlements = new PendingItem();
        pendingSettlements.setCount(countPendingSettlements());
        pendingSettlements.setUrl("/market/settlement?status=0");
        vo.setPendingSettlements(pendingSettlements);

        // 超期未领取奖励
        OverdueRewardItem overdueRewards = new OverdueRewardItem();
        overdueRewards.setCount(countOverdueRewards());
        overdueRewards.setOverdueDays(OVERDUE_DAYS);
        overdueRewards.setUrl("/market/reward?status=0");
        vo.setOverdueRewards(overdueRewards);

        // WebSocket 状态
        WsStatusItem wsStatus = new WsStatusItem();
        wsStatus.setConnected(polymarketWsManager.isConnected());
        wsStatus.setSubscribedMarkets(polymarketWsManager.getSubscribedTokenIds().size());
        wsStatus.setUrl("/market/ws");
        vo.setWsStatus(wsStatus);

        return vo;
    }

    // ========== 用户统计 ==========

    private UserStats buildUserStats() {
        UserStats stats = new UserStats();

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime yesterdayStart = todayStart.minusDays(1);

        stats.setTotal(walletUserMapper.selectCount());
        stats.setToday(countUsersByTimeRange(todayStart, LocalDateTime.now()));
        stats.setYesterday(countUsersByTimeRange(yesterdayStart, todayStart));
        stats.setChangeRate(calculateChangeRate(stats.getToday(), stats.getYesterday()));

        return stats;
    }

    private Long countUsersByTimeRange(LocalDateTime start, LocalDateTime end) {
        return walletUserMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<cn.iocoder.yudao.module.wallet.dal.dataobject.WalletUserDO>()
                        .ge(cn.iocoder.yudao.module.wallet.dal.dataobject.WalletUserDO::getCreateTime, start)
                        .lt(cn.iocoder.yudao.module.wallet.dal.dataobject.WalletUserDO::getCreateTime, end));
    }

    // ========== 积分统计 ==========

    private PointStats buildPointStats() {
        PointStats stats = new PointStats();

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime yesterdayStart = todayStart.minusDays(1);

        // 总余额
        stats.setTotalBalance(sumAllPointBalance());

        // 今日收入/支出
        stats.setTodayEarned(sumPointsByTimeRangePositive(todayStart, LocalDateTime.now()));
        stats.setTodaySpent(sumPointsByTimeRangeNegative(todayStart, LocalDateTime.now()));

        // 昨日收入
        stats.setYesterdayEarned(sumPointsByTimeRangePositive(yesterdayStart, todayStart));
        stats.setChangeRate(calculateChangeRate(stats.getTodayEarned(), stats.getYesterdayEarned()));

        return stats;
    }

    private Long sumAllPointBalance() {
        List<cn.iocoder.yudao.module.point.dal.dataobject.PointAccountDO> accounts = pointAccountMapper.selectList();
        return accounts.stream()
                .mapToLong(a -> a.getAvailablePoints() != null ? a.getAvailablePoints() : 0L)
                .sum();
    }

    private Long sumPointsByTimeRangePositive(LocalDateTime start, LocalDateTime end) {
        List<cn.iocoder.yudao.module.point.dal.dataobject.PointTransactionDO> transactions = pointTransactionMapper
                .selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<cn.iocoder.yudao.module.point.dal.dataobject.PointTransactionDO>()
                                .ge(cn.iocoder.yudao.module.point.dal.dataobject.PointTransactionDO::getCreateTime,
                                        start)
                                .lt(cn.iocoder.yudao.module.point.dal.dataobject.PointTransactionDO::getCreateTime, end)
                                .gt(cn.iocoder.yudao.module.point.dal.dataobject.PointTransactionDO::getAmount, 0));
        return transactions.stream()
                .mapToLong(t -> t.getAmount() != null ? t.getAmount() : 0L)
                .sum();
    }

    private Long sumPointsByTimeRangeNegative(LocalDateTime start, LocalDateTime end) {
        List<cn.iocoder.yudao.module.point.dal.dataobject.PointTransactionDO> transactions = pointTransactionMapper
                .selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<cn.iocoder.yudao.module.point.dal.dataobject.PointTransactionDO>()
                                .ge(cn.iocoder.yudao.module.point.dal.dataobject.PointTransactionDO::getCreateTime,
                                        start)
                                .lt(cn.iocoder.yudao.module.point.dal.dataobject.PointTransactionDO::getCreateTime, end)
                                .lt(cn.iocoder.yudao.module.point.dal.dataobject.PointTransactionDO::getAmount, 0));
        return transactions.stream()
                .mapToLong(t -> t.getAmount() != null ? Math.abs(t.getAmount()) : 0L)
                .sum();
    }

    // ========== 订单统计 ==========

    private OrderStats buildOrderStats() {
        OrderStats stats = new OrderStats();

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime yesterdayStart = todayStart.minusDays(1);

        stats.setTotal(pmOrderMapper.selectCount());
        stats.setToday(countOrdersByTimeRange(todayStart, LocalDateTime.now()));
        stats.setYesterday(countOrdersByTimeRange(yesterdayStart, todayStart));
        stats.setChangeRate(calculateChangeRate(stats.getToday(), stats.getYesterday()));

        // 成交金额
        stats.setTotalAmount(sumFilledAmount(null, null));
        stats.setTodayAmount(sumFilledAmount(todayStart, LocalDateTime.now()));

        return stats;
    }

    private Long countOrdersByTimeRange(LocalDateTime start, LocalDateTime end) {
        return pmOrderMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<cn.iocoder.yudao.module.market.dal.dataobject.order.PmOrderDO>()
                        .ge(cn.iocoder.yudao.module.market.dal.dataobject.order.PmOrderDO::getCreateTime, start)
                        .lt(cn.iocoder.yudao.module.market.dal.dataobject.order.PmOrderDO::getCreateTime, end));
    }

    private Long countOrdersByTimeRangeAndSide(LocalDateTime start, LocalDateTime end, Integer side) {
        return pmOrderMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<cn.iocoder.yudao.module.market.dal.dataobject.order.PmOrderDO>()
                        .ge(cn.iocoder.yudao.module.market.dal.dataobject.order.PmOrderDO::getCreateTime, start)
                        .lt(cn.iocoder.yudao.module.market.dal.dataobject.order.PmOrderDO::getCreateTime, end)
                        .eq(cn.iocoder.yudao.module.market.dal.dataobject.order.PmOrderDO::getSide, side));
    }

    private Long sumFilledAmount(LocalDateTime start, LocalDateTime end) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<cn.iocoder.yudao.module.market.dal.dataobject.order.PmOrderDO> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(cn.iocoder.yudao.module.market.dal.dataobject.order.PmOrderDO::getStatus, 1); // FILLED
        if (start != null) {
            wrapper.ge(cn.iocoder.yudao.module.market.dal.dataobject.order.PmOrderDO::getCreateTime, start);
        }
        if (end != null) {
            wrapper.lt(cn.iocoder.yudao.module.market.dal.dataobject.order.PmOrderDO::getCreateTime, end);
        }
        List<cn.iocoder.yudao.module.market.dal.dataobject.order.PmOrderDO> orders = pmOrderMapper.selectList(wrapper);
        return orders.stream()
                .mapToLong(o -> o.getFilledAmount() != null ? o.getFilledAmount() : 0L)
                .sum();
    }

    // ========== 市场统计 ==========

    private MarketStats buildMarketStats() {
        MarketStats stats = new MarketStats();

        // 活跃事件 (status = 1)
        stats.setActiveEvents(Math.toIntExact(pmEventMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<cn.iocoder.yudao.module.market.dal.dataobject.event.PmEventDO>()
                        .eq(cn.iocoder.yudao.module.market.dal.dataobject.event.PmEventDO::getStatus, 1))));

        // 活跃市场 (status = 1 交易中)
        stats.setActiveMarkets(Math.toIntExact(pmMarketMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<cn.iocoder.yudao.module.market.dal.dataobject.market.PmMarketDO>()
                        .eq(cn.iocoder.yudao.module.market.dal.dataobject.market.PmMarketDO::getStatus, 1))));

        // 待结算 (status = 0)
        stats.setPendingSettlements(Math.toIntExact(pmSettlementMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<cn.iocoder.yudao.module.market.dal.dataobject.settlement.PmSettlementDO>()
                        .eq(cn.iocoder.yudao.module.market.dal.dataobject.settlement.PmSettlementDO::getStatus, 0))));

        return stats;
    }

    // ========== 奖励统计 ==========

    private RewardStats buildRewardStats() {
        RewardStats stats = new RewardStats();

        // 待领取
        stats.setPendingCount(pmRewardMapper.countByStatus(0));
        stats.setPendingAmount(pmRewardMapper.sumAmountByStatus(0));
        stats.setOverdueDays(OVERDUE_DAYS);
        stats.setOverdueCount(countOverdueRewards());

        return stats;
    }

    private Integer countOverdueRewards() {
        LocalDateTime overdueTime = LocalDateTime.now().minusDays(OVERDUE_DAYS);
        return Math.toIntExact(pmRewardMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<cn.iocoder.yudao.module.market.dal.dataobject.reward.PmRewardDO>()
                        .eq(cn.iocoder.yudao.module.market.dal.dataobject.reward.PmRewardDO::getStatus, 0)
                        .lt(cn.iocoder.yudao.module.market.dal.dataobject.reward.PmRewardDO::getCreateTime,
                                overdueTime)));
    }

    // ========== 社交统计 ==========

    private SocialStats buildSocialStats() {
        SocialStats stats = new SocialStats();

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        stats.setTotalPosts(socialPostMapper.selectCount());
        stats.setTodayPosts(countPostsByTimeRange(todayStart, LocalDateTime.now()));
        stats.setTotalComments(socialCommentMapper.selectCount());
        stats.setTodayComments(countCommentsByTimeRange(todayStart, LocalDateTime.now()));

        return stats;
    }

    private Long countPostsByTimeRange(LocalDateTime start, LocalDateTime end) {
        return socialPostMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<cn.iocoder.yudao.module.social.dal.dataobject.SocialPostDO>()
                        .ge(cn.iocoder.yudao.module.social.dal.dataobject.SocialPostDO::getCreateTime, start)
                        .lt(cn.iocoder.yudao.module.social.dal.dataobject.SocialPostDO::getCreateTime, end)
                        .eq(cn.iocoder.yudao.module.social.dal.dataobject.SocialPostDO::getStatus, 0)); // 正常状态
    }

    private Long countCommentsByTimeRange(LocalDateTime start, LocalDateTime end) {
        return socialCommentMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<cn.iocoder.yudao.module.social.dal.dataobject.SocialCommentDO>()
                        .ge(cn.iocoder.yudao.module.social.dal.dataobject.SocialCommentDO::getCreateTime, start)
                        .lt(cn.iocoder.yudao.module.social.dal.dataobject.SocialCommentDO::getCreateTime, end));
    }

    private Long countLikesByTimeRange(LocalDateTime start, LocalDateTime end) {
        return socialLikeMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<cn.iocoder.yudao.module.social.dal.dataobject.SocialLikeDO>()
                        .ge(cn.iocoder.yudao.module.social.dal.dataobject.SocialLikeDO::getCreateTime, start)
                        .lt(cn.iocoder.yudao.module.social.dal.dataobject.SocialLikeDO::getCreateTime, end));
    }

    // ========== 待处理事项 ==========

    private Integer countPendingPosts() {
        return Math.toIntExact(socialPostMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<cn.iocoder.yudao.module.social.dal.dataobject.SocialPostDO>()
                        .eq(cn.iocoder.yudao.module.social.dal.dataobject.SocialPostDO::getStatus, 1))); // 待审核
    }

    private Integer countPendingSettlements() {
        return Math.toIntExact(pmSettlementMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<cn.iocoder.yudao.module.market.dal.dataobject.settlement.PmSettlementDO>()
                        .eq(cn.iocoder.yudao.module.market.dal.dataobject.settlement.PmSettlementDO::getStatus, 0)));
    }

    // ========== 工具方法 ==========

    private BigDecimal calculateChangeRate(Long current, Long previous) {
        if (previous == null || previous == 0) {
            return current != null && current > 0 ? new BigDecimal("100.00") : BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(current - previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(previous), 2, RoundingMode.HALF_UP);
    }

    // ========== 时间维度处理 ==========

    private static class TimeRange {
        LocalDateTime start;
        LocalDateTime end;

        TimeRange(LocalDateTime start, LocalDateTime end) {
            this.start = start;
            this.end = end;
        }
    }

    private TimeRange getTimeRange(String dimension) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start;

        switch (dimension) {
            case "day":
                start = now.minusHours(23).withMinute(0).withSecond(0).withNano(0);
                break;
            case "week":
                start = LocalDate.now().minusDays(6).atStartOfDay();
                break;
            case "month":
                start = LocalDate.now().minusDays(29).atStartOfDay();
                break;
            case "year":
                start = LocalDate.now().minusMonths(11).withDayOfMonth(1).atStartOfDay();
                break;
            default:
                start = LocalDate.now().minusDays(29).atStartOfDay();
        }

        return new TimeRange(start, now);
    }

    private String getGroupFormat(String dimension) {
        switch (dimension) {
            case "day":
                return "%H:00";
            case "week":
            case "month":
                return "%m-%d";
            case "year":
                return "%Y-%m";
            default:
                return "%m-%d";
        }
    }

    private List<String> generateTimePoints(String dimension, TimeRange range) {
        List<String> points = new ArrayList<>();
        DateTimeFormatter formatter;

        switch (dimension) {
            case "day":
                formatter = DateTimeFormatter.ofPattern("HH:00");
                LocalDateTime hourStart = range.start;
                for (int i = 0; i < 24; i++) {
                    points.add(hourStart.plusHours(i).format(formatter));
                }
                break;
            case "week":
                formatter = DateTimeFormatter.ofPattern("MM-dd");
                for (int i = 0; i < 7; i++) {
                    points.add(range.start.plusDays(i).format(formatter));
                }
                break;
            case "month":
                formatter = DateTimeFormatter.ofPattern("MM-dd");
                for (int i = 0; i < 30; i++) {
                    points.add(range.start.plusDays(i).format(formatter));
                }
                break;
            case "year":
                formatter = DateTimeFormatter.ofPattern("yyyy-MM");
                for (int i = 0; i < 12; i++) {
                    points.add(range.start.plusMonths(i).format(formatter));
                }
                break;
            default:
                formatter = DateTimeFormatter.ofPattern("MM-dd");
                for (int i = 0; i < 30; i++) {
                    points.add(range.start.plusDays(i).format(formatter));
                }
        }

        return points;
    }

    private LocalDateTime[] getTimePointRange(String dimension, String timePoint, TimeRange baseRange) {
        LocalDateTime start, end;

        switch (dimension) {
            case "day":
                // timePoint 格式: "HH:00"
                int hour = Integer.parseInt(timePoint.substring(0, 2));
                start = baseRange.start.withHour(hour).withMinute(0).withSecond(0).withNano(0);
                if (hour < baseRange.start.getHour()) {
                    start = start.plusDays(1);
                }
                end = start.plusHours(1);
                break;
            case "week":
            case "month":
                // timePoint 格式: "MM-dd"
                String[] parts = timePoint.split("-");
                int month = Integer.parseInt(parts[0]);
                int day = Integer.parseInt(parts[1]);
                int year = LocalDate.now().getYear();
                // 处理跨年情况
                if (month > LocalDate.now().getMonthValue()) {
                    year--;
                }
                start = LocalDate.of(year, month, day).atStartOfDay();
                end = start.plusDays(1);
                break;
            case "year":
                // timePoint 格式: "yyyy-MM"
                String[] yearMonth = timePoint.split("-");
                start = LocalDate.of(Integer.parseInt(yearMonth[0]), Integer.parseInt(yearMonth[1]), 1).atStartOfDay();
                end = start.plusMonths(1);
                break;
            default:
                start = baseRange.start;
                end = baseRange.end;
        }

        return new LocalDateTime[] { start, end };
    }
}
