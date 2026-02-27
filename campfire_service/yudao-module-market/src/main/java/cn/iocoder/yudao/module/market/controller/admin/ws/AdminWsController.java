package cn.iocoder.yudao.module.market.controller.admin.ws;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.market.controller.admin.ws.vo.WsLogDeleteReqVO;
import cn.iocoder.yudao.module.market.controller.admin.ws.vo.WsLogPageReqVO;
import cn.iocoder.yudao.module.market.controller.admin.ws.vo.WsLogRespVO;
import cn.iocoder.yudao.module.market.convert.ws.WsLogConvert;
import cn.iocoder.yudao.module.market.dal.dataobject.ws.PmWsLogDO;
import cn.iocoder.yudao.module.market.service.ws.PmWsLogService;
import cn.iocoder.yudao.module.market.ws.PolymarketWsManager;
import cn.iocoder.yudao.module.market.ws.vo.WsStatusVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - WebSocket 管理")
@RestController
@RequestMapping("/market/ws")
@Validated
public class AdminWsController {

    @Resource
    private PolymarketWsManager polymarketWsManager;

    @Resource
    private PmWsLogService wsLogService;

    // ========== WS 状态管理 ==========

    @GetMapping("/status")
    @Operation(summary = "获取 WebSocket 连接状态")
    @PreAuthorize("@ss.hasPermission('market:ws:query')")
    public CommonResult<WsStatusVO> getWsStatus() {
        return success(polymarketWsManager.getStatusInfo());
    }

    @GetMapping("/subscribed")
    @Operation(summary = "获取已订阅的 Token IDs")
    @PreAuthorize("@ss.hasPermission('market:ws:query')")
    public CommonResult<Set<String>> getSubscribedTokenIds() {
        return success(polymarketWsManager.getSubscribedTokenIds());
    }

    @GetMapping("/unsubscribed")
    @Operation(summary = "获取交易中市场未订阅的 Token IDs")
    @PreAuthorize("@ss.hasPermission('market:ws:query')")
    public CommonResult<Set<String>> getUnsubscribedTokenIds() {
        return success(polymarketWsManager.getUnsubscribedTokenIds());
    }

    @PostMapping("/reconnect")
    @Operation(summary = "手动重连")
    @PreAuthorize("@ss.hasPermission('market:ws:update')")
    public CommonResult<Boolean> reconnect() {
        polymarketWsManager.disconnect();
        polymarketWsManager.connectAndSubscribe();
        return success(true);
    }

    // ========== WS 日志管理 ==========

    @GetMapping("/log/page")
    @Operation(summary = "分页查询 WS 日志")
    @PreAuthorize("@ss.hasPermission('market:ws:query')")
    public CommonResult<PageResult<WsLogRespVO>> getWsLogPage(@Valid WsLogPageReqVO pageReqVO) {
        PageResult<PmWsLogDO> pageResult = wsLogService.getWsLogPage(pageReqVO);
        return success(new PageResult<>(
                WsLogConvert.INSTANCE.convertList(pageResult.getList()),
                pageResult.getTotal()));
    }

    @GetMapping("/log/latest")
    @Operation(summary = "获取最新 WS 日志")
    @Parameter(name = "limit", description = "数量限制，默认 50，最大 500")
    @PreAuthorize("@ss.hasPermission('market:ws:query')")
    public CommonResult<List<WsLogRespVO>> getLatestLogs(@RequestParam(defaultValue = "50") Integer limit) {
        List<PmWsLogDO> logs = wsLogService.getLatestLogs(limit);
        return success(WsLogConvert.INSTANCE.convertList(logs));
    }

    @DeleteMapping("/log/delete")
    @Operation(summary = "按时间范围删除 WS 日志")
    @PreAuthorize("@ss.hasPermission('market:ws:delete')")
    public CommonResult<Integer> deleteWsLogs(@Valid WsLogDeleteReqVO reqVO) {
        int deleted = wsLogService.deleteByTimeRange(reqVO.getStartTime(), reqVO.getEndTime());
        return success(deleted);
    }

}
