package cn.iocoder.yudao.module.market.service.dashboard;

import cn.iocoder.yudao.module.market.controller.admin.dashboard.vo.DashboardOverviewRespVO;
import cn.iocoder.yudao.module.market.controller.admin.dashboard.vo.DashboardPendingRespVO;
import cn.iocoder.yudao.module.market.controller.admin.dashboard.vo.DashboardTrendRespVO;

/**
 * 数据概览 Service 接口
 */
public interface DashboardService {

    /**
     * 获取概览统计数据
     *
     * @return 概览统计数据
     */
    DashboardOverviewRespVO getOverview();

    /**
     * 获取用户增长趋势
     *
     * @param dimension 时间维度: day/week/month/year
     * @return 用户趋势数据
     */
    DashboardTrendRespVO getUserTrend(String dimension);

    /**
     * 获取订单量趋势
     *
     * @param dimension 时间维度: day/week/month/year
     * @return 订单趋势数据
     */
    DashboardTrendRespVO getOrderTrend(String dimension);

    /**
     * 获取积分流水趋势
     *
     * @param dimension 时间维度: day/week/month/year
     * @return 积分趋势数据
     */
    DashboardTrendRespVO getPointTrend(String dimension);

    /**
     * 获取社交活跃度趋势
     *
     * @param dimension 时间维度: day/week/month/year
     * @return 社交趋势数据
     */
    DashboardTrendRespVO getSocialTrend(String dimension);

    /**
     * 获取待处理事项
     *
     * @return 待处理事项数据
     */
    DashboardPendingRespVO getPending();
}
