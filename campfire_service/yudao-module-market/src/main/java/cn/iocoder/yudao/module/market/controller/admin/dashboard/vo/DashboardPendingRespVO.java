package cn.iocoder.yudao.module.market.controller.admin.dashboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 数据概览 - 待处理事项 Response VO
 */
@Schema(description = "管理后台 - 数据概览待处理事项 Response VO")
@Data
public class DashboardPendingRespVO {

    @Schema(description = "待审核帖子")
    private PendingItem pendingPosts;

    @Schema(description = "待确认结算")
    private PendingItem pendingSettlements;

    @Schema(description = "超期未领取奖励")
    private OverdueRewardItem overdueRewards;

    @Schema(description = "WebSocket 状态")
    private WsStatusItem wsStatus;

    @Schema(description = "待处理项")
    @Data
    public static class PendingItem {
        @Schema(description = "数量", example = "5")
        private Integer count;

        @Schema(description = "跳转 URL", example = "/social/post?status=1")
        private String url;
    }

    @Schema(description = "超期奖励项")
    @Data
    public static class OverdueRewardItem {
        @Schema(description = "数量", example = "23")
        private Integer count;

        @Schema(description = "超期天数", example = "7")
        private Integer overdueDays;

        @Schema(description = "跳转 URL", example = "/market/reward?status=0")
        private String url;
    }

    @Schema(description = "WebSocket 状态项")
    @Data
    public static class WsStatusItem {
        @Schema(description = "是否已连接", example = "true")
        private Boolean connected;

        @Schema(description = "已订阅市场数", example = "12")
        private Integer subscribedMarkets;

        @Schema(description = "跳转 URL", example = "/market/ws")
        private String url;
    }
}
