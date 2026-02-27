package cn.iocoder.yudao.module.market.controller.admin.order;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.market.service.order.OrderRepairService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 订单数据修复")
@RestController
@RequestMapping("/admin-api/market/order/repair")
public class OrderRepairController {

    @Resource
    private OrderRepairService orderRepairService;

    @PostMapping("/all")
    @Operation(summary = "修复所有不一致的订单")
    @PreAuthorize("@ss.hasPermission('market:order:repair')")
    public CommonResult<Integer> repairAll() {
        int count = orderRepairService.repairAllInconsistentOrders();
        return success(count);
    }

    @GetMapping("/check")
    @Operation(summary = "检查特定订单")
    @Parameter(name = "orderNo", description = "订单号", required = true)
    @PreAuthorize("@ss.hasPermission('market:order:query')")
    public CommonResult<String> checkOrder(@RequestParam("orderNo") String orderNo) {
        String result = orderRepairService.checkOrder(orderNo);
        return success(result);
    }
}
