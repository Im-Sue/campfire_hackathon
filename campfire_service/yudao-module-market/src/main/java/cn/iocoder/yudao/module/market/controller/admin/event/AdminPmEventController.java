package cn.iocoder.yudao.module.market.controller.admin.event;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.market.dal.dataobject.event.PmEventDO;
import cn.iocoder.yudao.module.market.service.event.PmEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 预测市场事件")
@RestController
@RequestMapping("/market/event")
@Validated
public class AdminPmEventController {

    @Resource
    private PmEventService pmEventService;

    @GetMapping("/page")
    @Operation(summary = "分页获取事件")
    @PreAuthorize("@ss.hasPermission('market:event:query')")
    public CommonResult<PageResult<PmEventDO>> getEventPage(
            @RequestParam(required = false) @Parameter(description = "状态") Integer status,
            @RequestParam(required = false) @Parameter(description = "分类") String category,
            PageParam pageParam) {
        return success(pmEventService.getEventPage(status, category, pageParam));
    }

    @GetMapping("/get")
    @Operation(summary = "获取事件详情")
    @Parameter(name = "id", description = "事件编号", required = true)
    @PreAuthorize("@ss.hasPermission('market:event:query')")
    public CommonResult<PmEventDO> getEvent(@RequestParam("id") Long id) {
        return success(pmEventService.getEvent(id));
    }

    @PostMapping("/publish")
    @Operation(summary = "上架事件")
    @Parameter(name = "id", description = "事件编号", required = true)
    @PreAuthorize("@ss.hasPermission('market:event:update')")
    public CommonResult<Boolean> publishEvent(@RequestParam("id") Long id) {
        pmEventService.publishEvent(id);
        return success(true);
    }

    @PostMapping("/unpublish")
    @Operation(summary = "下架事件")
    @Parameter(name = "id", description = "事件编号", required = true)
    @PreAuthorize("@ss.hasPermission('market:event:update')")
    public CommonResult<Boolean> unpublishEvent(@RequestParam("id") Long id) {
        pmEventService.unpublishEvent(id);
        return success(true);
    }

}
