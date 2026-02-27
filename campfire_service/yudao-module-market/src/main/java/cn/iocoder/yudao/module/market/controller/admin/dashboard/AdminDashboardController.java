package cn.iocoder.yudao.module.market.controller.admin.dashboard;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.market.controller.admin.dashboard.vo.DashboardOverviewRespVO;
import cn.iocoder.yudao.module.market.controller.admin.dashboard.vo.DashboardPendingRespVO;
import cn.iocoder.yudao.module.market.controller.admin.dashboard.vo.DashboardTrendRespVO;
import cn.iocoder.yudao.module.market.service.dashboard.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 数据概览
 */
@Tag(name = "管理后台 - 数据概览")
@RestController
@RequestMapping("/market/dashboard")
@Validated
public class AdminDashboardController {

    @Resource
    private DashboardService dashboardService;

    @GetMapping("/overview")
    @Operation(summary = "获取概览统计数据")

    public CommonResult<DashboardOverviewRespVO> getOverview() {
        return success(dashboardService.getOverview());
    }

    @GetMapping("/trend/users")
    @Operation(summary = "获取用户增长趋势")
    @Parameter(name = "dimension", description = "时间维度: day/week/month/year", required = true, example = "month")

    public CommonResult<DashboardTrendRespVO> getUserTrend(
            @RequestParam("dimension") String dimension) {
        return success(dashboardService.getUserTrend(dimension));
    }

    @GetMapping("/trend/orders")
    @Operation(summary = "获取订单量趋势")
    @Parameter(name = "dimension", description = "时间维度: day/week/month/year", required = true, example = "month")

    public CommonResult<DashboardTrendRespVO> getOrderTrend(
            @RequestParam("dimension") String dimension) {
        return success(dashboardService.getOrderTrend(dimension));
    }

    @GetMapping("/trend/points")
    @Operation(summary = "获取积分流水趋势")
    @Parameter(name = "dimension", description = "时间维度: day/week/month/year", required = true, example = "month")

    public CommonResult<DashboardTrendRespVO> getPointTrend(
            @RequestParam("dimension") String dimension) {
        return success(dashboardService.getPointTrend(dimension));
    }

    @GetMapping("/trend/social")
    @Operation(summary = "获取社交活跃度趋势")
    @Parameter(name = "dimension", description = "时间维度: day/week/month/year", required = true, example = "month")

    public CommonResult<DashboardTrendRespVO> getSocialTrend(
            @RequestParam("dimension") String dimension) {
        return success(dashboardService.getSocialTrend(dimension));
    }

    @GetMapping("/pending")
    @Operation(summary = "获取待处理事项")

    public CommonResult<DashboardPendingRespVO> getPending() {
        return success(dashboardService.getPending());
    }
}
