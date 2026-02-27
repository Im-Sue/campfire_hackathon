package cn.iocoder.yudao.module.market.controller.app.order;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.market.controller.app.order.vo.AppOrderCreateReqVO;
import cn.iocoder.yudao.module.market.controller.app.order.vo.AppOrderRespVO;
import cn.iocoder.yudao.module.market.convert.order.PmOrderConvert;
import cn.iocoder.yudao.module.market.dal.dataobject.market.PmMarketDO;
import cn.iocoder.yudao.module.market.dal.dataobject.order.PmOrderDO;
import cn.iocoder.yudao.module.market.service.market.PmMarketService;
import cn.iocoder.yudao.module.market.service.order.PmOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 预测市场订单")
@RestController
@RequestMapping("/app-market/order")
@Validated
public class AppPmOrderController {

    @Resource
    private PmOrderService pmOrderService;

    @Resource
    private PmMarketService pmMarketService;

    @PostMapping("/create")
    @Operation(summary = "创建订单")
    public CommonResult<Long> createOrder(@Valid @RequestBody AppOrderCreateReqVO createReqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(pmOrderService.createOrder(userId, createReqVO));
    }

    @PostMapping("/cancel")
    @Operation(summary = "取消订单")
    @Parameter(name = "id", description = "订单编号", required = true)
    public CommonResult<Boolean> cancelOrder(@RequestParam("id") Long id) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        pmOrderService.cancelOrder(userId, id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取订单详情")
    @Parameter(name = "id", description = "订单编号", required = true)
    public CommonResult<AppOrderRespVO> getOrder(@RequestParam("id") Long id) {
        PmOrderDO order = pmOrderService.getOrder(id);
        return success(PmOrderConvert.INSTANCE.convertToApp(order));
    }

    @GetMapping("/page")
    @Operation(summary = "分页获取我的订单")
    public CommonResult<PageResult<AppOrderRespVO>> getOrderPage(PageParam pageParam) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        PageResult<PmOrderDO> pageResult = pmOrderService.getOrderPageByUserId(userId, pageParam);

        // 批量查询市场信息
        Set<Long> marketIds = pageResult.getList().stream()
                .map(PmOrderDO::getMarketId)
                .collect(Collectors.toSet());
        Map<Long, PmMarketDO> marketMap = pmMarketService.getMarketMap(marketIds);

        return success(PmOrderConvert.INSTANCE.convertToAppPageWithMarket(pageResult, marketMap));
    }

}
