package cn.iocoder.yudao.module.market.controller.admin.dashboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 数据概览 - 统计数据 Response VO
 */
@Schema(description = "管理后台 - 数据概览统计数据 Response VO")
@Data
public class DashboardOverviewRespVO {

    @Schema(description = "用户统计")
    private UserStats users;

    @Schema(description = "积分统计")
    private PointStats points;

    @Schema(description = "订单统计")
    private OrderStats orders;

    @Schema(description = "市场统计")
    private MarketStats markets;

    @Schema(description = "奖励统计")
    private RewardStats rewards;

    @Schema(description = "社交统计")
    private SocialStats social;

    @Schema(description = "用户统计")
    @Data
    public static class UserStats {
        @Schema(description = "总用户数", example = "12580")
        private Long total;

        @Schema(description = "今日新增", example = "128")
        private Long today;

        @Schema(description = "昨日新增", example = "115")
        private Long yesterday;

        @Schema(description = "环比变化率 %", example = "11.3")
        private BigDecimal changeRate;
    }

    @Schema(description = "积分统计")
    @Data
    public static class PointStats {
        @Schema(description = "总余额", example = "8560000")
        private Long totalBalance;

        @Schema(description = "今日收入", example = "125000")
        private Long todayEarned;

        @Schema(description = "今日支出", example = "89000")
        private Long todaySpent;

        @Schema(description = "昨日收入", example = "110000")
        private Long yesterdayEarned;

        @Schema(description = "环比变化率 %", example = "13.6")
        private BigDecimal changeRate;
    }

    @Schema(description = "订单统计")
    @Data
    public static class OrderStats {
        @Schema(description = "总订单数", example = "45680")
        private Long total;

        @Schema(description = "今日订单", example = "523")
        private Long today;

        @Schema(description = "昨日订单", example = "498")
        private Long yesterday;

        @Schema(description = "环比变化率 %", example = "5.0")
        private BigDecimal changeRate;

        @Schema(description = "总成交金额", example = "12560000")
        private Long totalAmount;

        @Schema(description = "今日成交金额", example = "185000")
        private Long todayAmount;
    }

    @Schema(description = "市场统计")
    @Data
    public static class MarketStats {
        @Schema(description = "活跃事件数", example = "45")
        private Integer activeEvents;

        @Schema(description = "活跃市场数", example = "128")
        private Integer activeMarkets;

        @Schema(description = "待结算数", example = "3")
        private Integer pendingSettlements;
    }

    @Schema(description = "奖励统计")
    @Data
    public static class RewardStats {
        @Schema(description = "待领取笔数", example = "156")
        private Integer pendingCount;

        @Schema(description = "待领取金额", example = "450000")
        private Long pendingAmount;

        @Schema(description = "超期天数阈值", example = "7")
        private Integer overdueDays;

        @Schema(description = "超期未领取笔数", example = "23")
        private Integer overdueCount;
    }

    @Schema(description = "社交统计")
    @Data
    public static class SocialStats {
        @Schema(description = "总帖子数", example = "8560")
        private Long totalPosts;

        @Schema(description = "今日帖子", example = "45")
        private Long todayPosts;

        @Schema(description = "总评论数", example = "25680")
        private Long totalComments;

        @Schema(description = "今日评论", example = "156")
        private Long todayComments;
    }
}
