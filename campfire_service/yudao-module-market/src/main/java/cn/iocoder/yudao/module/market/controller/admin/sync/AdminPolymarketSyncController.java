package cn.iocoder.yudao.module.market.controller.admin.sync;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.market.api.dto.PolymarketEventDTO;
import cn.iocoder.yudao.module.market.controller.admin.sync.vo.PolymarketImportReqVO;
import cn.iocoder.yudao.module.market.service.sync.PolymarketSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - Polymarket 同步
 */
@Tag(name = "管理后台 - Polymarket 同步")
@RestController
@RequestMapping("/market/polymarket")
@Validated
public class AdminPolymarketSyncController {

    @Resource
    private PolymarketSyncService polymarketSyncService;

    @GetMapping("/list")
    @Operation(summary = "浏览 Polymarket 可选事件")
    @PreAuthorize("@ss.hasPermission('market:polymarket:query')")
    public CommonResult<List<PolymarketEventDTO>> browseEvents(
            @RequestParam(required = false, defaultValue = "politics") @Parameter(description = "分类: politics, sports, crypto") String category,
            @RequestParam(required = false, defaultValue = "1") @Parameter(description = "页码") Integer pageNo,
            @RequestParam(required = false, defaultValue = "50") @Parameter(description = "每页条数") Integer pageSize) {
        List<PolymarketEventDTO> events = polymarketSyncService.browseEvents(category, pageNo, pageSize);
        return success(events);
    }

    @PostMapping("/import")
    @Operation(summary = "导入 Polymarket 事件")
    @PreAuthorize("@ss.hasPermission('market:polymarket:import')")
    public CommonResult<Long> importEvent(@RequestBody @Valid PolymarketImportReqVO reqVO) {
        Long eventId = polymarketSyncService.importEvent(reqVO.getPolymarketEventId());
        return success(eventId);
    }

    @GetMapping("/check")
    @Operation(summary = "检查事件是否已导入")
    @Parameter(name = "polymarketEventId", description = "Polymarket Event ID", required = true)
    @PreAuthorize("@ss.hasPermission('market:polymarket:query')")
    public CommonResult<Boolean> checkImported(@RequestParam String polymarketEventId) {
        boolean imported = polymarketSyncService.isEventImported(polymarketEventId);
        return success(imported);
    }

    @GetMapping("/search")
    @Operation(summary = "搜索 Polymarket 事件")
    @PreAuthorize("@ss.hasPermission('market:polymarket:query')")
    public CommonResult<List<PolymarketEventDTO>> searchEvents(
            @RequestParam @Parameter(description = "搜索关键字") String keyword,
            @RequestParam(required = false, defaultValue = "20") @Parameter(description = "返回数量") Integer limit) {
        List<PolymarketEventDTO> events = polymarketSyncService.searchEvents(keyword, limit);
        return success(events);
    }

}
