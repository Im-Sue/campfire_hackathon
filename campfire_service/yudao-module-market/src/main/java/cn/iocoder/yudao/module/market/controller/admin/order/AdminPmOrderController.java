package cn.iocoder.yudao.module.market.controller.admin.order;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.market.controller.admin.order.vo.OrderPageReqVO;
import cn.iocoder.yudao.module.market.controller.admin.order.vo.OrderRespVO;
import cn.iocoder.yudao.module.market.convert.order.PmOrderConvert;
import cn.iocoder.yudao.module.market.dal.dataobject.market.PmMarketDO;
import cn.iocoder.yudao.module.market.dal.dataobject.order.PmOrderDO;
import cn.iocoder.yudao.module.market.service.market.PmMarketService;
import cn.iocoder.yudao.module.market.service.order.PmOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 预测市场订单")
@RestController
@RequestMapping("/market/order")
@Validated
public class AdminPmOrderController {

    @Resource
    private PmOrderService pmOrderService;

    @Resource
    private PmMarketService pmMarketService;

    @GetMapping("/page")
    @Operation(summary = "分页获取订单列表")
    @PreAuthorize("@ss.hasPermission('market:order:query')")
    public CommonResult<PageResult<OrderRespVO>> getOrderPage(@Valid OrderPageReqVO pageReqVO) {
        PageResult<PmOrderDO> pageResult = pmOrderService.getOrderPage(pageReqVO);
        PageResult<OrderRespVO> result = PmOrderConvert.INSTANCE.convertAdminPage(pageResult);

        // 填充市场信息
        fillMarketInfo(result.getList());

        return success(result);
    }

    @GetMapping("/get")
    @Operation(summary = "获取订单详情")
    @Parameter(name = "id", description = "订单编号", required = true)
    @PreAuthorize("@ss.hasPermission('market:order:query')")
    public CommonResult<OrderRespVO> getOrder(@RequestParam("id") Long id) {
        PmOrderDO order = pmOrderService.getOrder(id);
        if (order == null) {
            return success(null);
        }
        OrderRespVO respVO = PmOrderConvert.INSTANCE.convertAdmin(order);

        // 填充市场信息
        PmMarketDO market = pmMarketService.getMarket(order.getMarketId());
        if (market != null) {
            respVO.setMarketQuestion(market.getQuestion());
        }

        return success(respVO);
    }

    /**
     * 填充市场信息
     */
    private void fillMarketInfo(List<OrderRespVO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        // 收集市场 ID
        Set<Long> marketIds = list.stream()
                .map(OrderRespVO::getMarketId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (marketIds.isEmpty()) {
            return;
        }

        // 批量查询市场
        Map<Long, PmMarketDO> marketMap = new HashMap<>();
        for (Long marketId : marketIds) {
            PmMarketDO market = pmMarketService.getMarket(marketId);
            if (market != null) {
                marketMap.put(marketId, market);
            }
        }

        // 填充信息
        for (OrderRespVO vo : list) {
            PmMarketDO market = marketMap.get(vo.getMarketId());
            if (market != null) {
                vo.setMarketQuestion(market.getQuestion());
            }
        }
    }

}
