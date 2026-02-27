package cn.iocoder.yudao.module.treasure.controller.admin.pool;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.treasure.controller.admin.pool.vo.TreasurePoolCreateReqVO;
import cn.iocoder.yudao.module.treasure.controller.admin.pool.vo.TreasurePoolCreateRespVO;
import cn.iocoder.yudao.module.treasure.controller.admin.pool.vo.TreasurePoolPageReqVO;
import cn.iocoder.yudao.module.treasure.dal.dataobject.TreasurePoolDO;
import cn.iocoder.yudao.module.treasure.service.pool.TreasurePoolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.math.BigInteger;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 奖池管理 Controller
 *
 * @author Sue
 */
@Tag(name = "管理后台 - 奖池管理")
@RestController
@RequestMapping("/treasure/pool")
@Validated
@Slf4j
public class TreasurePoolController {

    @Resource
    private TreasurePoolService poolService;

    @GetMapping("/page")
    @Operation(summary = "获得奖池分页")
    @PreAuthorize("@ss.hasPermission('treasure:pool:query')")
    public CommonResult<PageResult<TreasurePoolDO>> getPoolPage(@Valid TreasurePoolPageReqVO pageReqVO) {
        PageResult<TreasurePoolDO> pageResult = poolService.getPoolPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/list")
    @Operation(summary = "获得奖池列表（兼容端点）")
    @PreAuthorize("@ss.hasPermission('treasure:pool:query')")
    public CommonResult<PageResult<TreasurePoolDO>> getPoolList(@Valid TreasurePoolPageReqVO pageReqVO) {
        return getPoolPage(pageReqVO);
    }

    @PostMapping("/create")
    @Operation(summary = "创建奖池")
    @PreAuthorize("@ss.hasPermission('treasure:pool:create')")
    public CommonResult<TreasurePoolCreateRespVO> createPool(@Valid @RequestBody TreasurePoolCreateReqVO reqVO) {
        try {
            TreasurePoolCreateRespVO result = poolService.createPool(
                    reqVO.getPrice(),
                    reqVO.getTotalShares(),
                    reqVO.getDuration(),
                    reqVO.getWinnerCount(),
                    reqVO.getInitialPrize());
            return success(result);
        } catch (Exception e) {
            log.error("创建奖池失败: reqVO={}", reqVO, e);
            return CommonResult.error(500, "创建奖池失败: " + e.getMessage());
        }
    }

    @GetMapping("/get")
    @Operation(summary = "获得奖池详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('treasure:pool:query')")
    public CommonResult<TreasurePoolDO> getPool(@RequestParam("id") Long id) {
        TreasurePoolDO pool = poolService.getPool(id);
        return success(pool);
    }

    @PostMapping("/sync")
    @Operation(summary = "同步链上奖池数据")
    @Parameter(name = "poolId", description = "链上奖池 ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('treasure:pool:sync')")
    public CommonResult<TreasurePoolDO> syncPool(@RequestParam("poolId") Long poolId) {
        try {
            TreasurePoolDO pool = poolService.syncPoolFromChain(BigInteger.valueOf(poolId));
            return success(pool);
        } catch (Exception e) {
            log.error("同步奖池数据失败: poolId={}", poolId, e);
            return CommonResult.error(500, "同步失败: " + e.getMessage());
        }
    }

    @PostMapping("/draw")
    @Operation(summary = "执行开奖")
    @Parameter(name = "poolId", description = "链上奖池 ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('treasure:pool:draw')")
    public CommonResult<String> executeDraw(@RequestParam("poolId") Long poolId) {
        try {
            String txHash = poolService.executeDraw(poolId);
            return success(txHash);
        } catch (Exception e) {
            log.error("执行开奖失败: poolId={}", poolId, e);
            return CommonResult.error(500, "开奖失败: " + e.getMessage());
        }
    }
}
