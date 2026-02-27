package cn.iocoder.yudao.module.market.controller.admin.api;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.market.controller.admin.api.vo.ApiLogDeleteReqVO;
import cn.iocoder.yudao.module.market.controller.admin.api.vo.ApiLogPageReqVO;
import cn.iocoder.yudao.module.market.controller.admin.api.vo.ApiLogRespVO;
import cn.iocoder.yudao.module.market.controller.admin.api.vo.ApiLogStatsVO;
import cn.iocoder.yudao.module.market.convert.api.ApiLogConvert;
import cn.iocoder.yudao.module.market.dal.dataobject.api.PmApiLogDO;
import cn.iocoder.yudao.module.market.service.api.PmApiLogService;
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

@Tag(name = "管理后台 - Polymarket API 日志")
@RestController
@RequestMapping("/market/api-log")
@Validated
public class AdminApiLogController {

    @Resource
    private PmApiLogService apiLogService;

    @GetMapping("/page")
    @Operation(summary = "分页查询 API 日志")
    @PreAuthorize("@ss.hasPermission('market:api-log:query')")
    public CommonResult<PageResult<ApiLogRespVO>> getApiLogPage(@Valid ApiLogPageReqVO pageReqVO) {
        PageResult<PmApiLogDO> pageResult = apiLogService.getApiLogPage(pageReqVO);
        return success(new PageResult<>(
                ApiLogConvert.INSTANCE.convertList(pageResult.getList()),
                pageResult.getTotal()));
    }

    @GetMapping("/latest")
    @Operation(summary = "获取最新 API 日志")
    @Parameter(name = "limit", description = "数量限制，默认 50，最大 500")
    @PreAuthorize("@ss.hasPermission('market:api-log:query')")
    public CommonResult<List<ApiLogRespVO>> getLatestLogs(@RequestParam(defaultValue = "50") Integer limit) {
        List<PmApiLogDO> logs = apiLogService.getLatestLogs(limit);
        return success(ApiLogConvert.INSTANCE.convertList(logs));
    }

    @GetMapping("/stats")
    @Operation(summary = "获取 API 日志统计信息")
    @PreAuthorize("@ss.hasPermission('market:api-log:query')")
    public CommonResult<ApiLogStatsVO> getStats() {
        return success(apiLogService.getStats());
    }

    @DeleteMapping("/delete")
    @Operation(summary = "按时间范围删除 API 日志")
    @PreAuthorize("@ss.hasPermission('market:api-log:delete')")
    public CommonResult<Integer> deleteApiLogs(@Valid ApiLogDeleteReqVO reqVO) {
        int deleted = apiLogService.deleteByTimeRange(reqVO.getStartTime(), reqVO.getEndTime());
        return success(deleted);
    }

}
