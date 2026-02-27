package cn.iocoder.yudao.module.market.controller.admin.market;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.market.dal.dataobject.market.PmMarketDO;
import cn.iocoder.yudao.module.market.service.market.PmMarketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 预测市场信息")
@RestController
@RequestMapping("/market/market")
@Validated
public class AdminPmMarketController {

    @Resource
    private PmMarketService pmMarketService;

    @GetMapping("/list-by-event")
    @Operation(summary = "获取事件下的市场列表")
    @Parameter(name = "eventId", description = "事件编号", required = true)
    @PreAuthorize("@ss.hasPermission('market:market:query')")
    public CommonResult<List<PmMarketDO>> getMarketsByEventId(@RequestParam("eventId") Long eventId) {
        return success(pmMarketService.getMarketsByEventId(eventId));
    }

    @GetMapping("/get")
    @Operation(summary = "获取市场详情")
    @Parameter(name = "id", description = "市场编号", required = true)
    @PreAuthorize("@ss.hasPermission('market:market:query')")
    public CommonResult<PmMarketDO> getMarket(@RequestParam("id") Long id) {
        return success(pmMarketService.getMarket(id));
    }

    @PostMapping("/suspend")
    @Operation(summary = "封盘市场")
    @Parameter(name = "id", description = "市场编号", required = true)
    @PreAuthorize("@ss.hasPermission('market:market:update')")
    public CommonResult<Boolean> suspendMarket(@RequestParam("id") Long id) {
        pmMarketService.suspendMarket(id);
        return success(true);
    }

    @PostMapping("/resume")
    @Operation(summary = "恢复交易")
    @Parameter(name = "id", description = "市场编号", required = true)
    @PreAuthorize("@ss.hasPermission('market:market:update')")
    public CommonResult<Boolean> resumeMarket(@RequestParam("id") Long id) {
        pmMarketService.resumeMarket(id);
        return success(true);
    }

    @GetMapping("/trading")
    @Operation(summary = "获取交易中的市场列表")
    @PreAuthorize("@ss.hasPermission('market:market:query')")
    public CommonResult<List<PmMarketDO>> getTradingMarkets() {
        return success(pmMarketService.getTradingMarkets());
    }

    @GetMapping("/pending-settlement")
    @Operation(summary = "获取待结算的市场列表")
    @PreAuthorize("@ss.hasPermission('market:market:query')")
    public CommonResult<List<PmMarketDO>> getPendingSettlementMarkets() {
        return success(pmMarketService.getPendingSettlementMarkets());
    }

    @GetMapping("/suspended")
    @Operation(summary = "获取已封盘的市场列表")
    @PreAuthorize("@ss.hasPermission('market:market:query')")
    public CommonResult<List<PmMarketDO>> getSuspendedMarkets() {
        return success(pmMarketService.getSuspendedMarkets());
    }

    @GetMapping("/prices")
    @Operation(summary = "获取市场价格")
    @Parameter(name = "id", description = "市场编号", required = true)
    @PreAuthorize("@ss.hasPermission('market:market:query')")
    public CommonResult<java.util.Map<String, java.math.BigDecimal>> getMarketPrices(@RequestParam("id") Long id) {
        return success(pmMarketService.getMarketPrices(id));
    }

}
