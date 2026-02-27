package cn.iocoder.yudao.module.treasure.controller.admin.winner;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.treasure.controller.admin.winner.vo.TreasureWinnerPageReqVO;
import cn.iocoder.yudao.module.treasure.dal.dataobject.TreasureWinnerDO;
import cn.iocoder.yudao.module.treasure.service.winner.TreasureWinnerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 中奖记录管理 Controller
 *
 * @author Sue
 */
@Tag(name = "管理后台 - 中奖记录管理")
@RestController
@RequestMapping("/treasure/winner")
@Validated
@Slf4j
public class TreasureWinnerController {

    @Resource
    private TreasureWinnerService winnerService;

    @GetMapping("/page")
    @Operation(summary = "获得中奖记录分页")
    @PreAuthorize("@ss.hasPermission('treasure:winner:query')")
    public CommonResult<PageResult<TreasureWinnerDO>> getWinnerPage(@Valid TreasureWinnerPageReqVO pageReqVO) {
        PageResult<TreasureWinnerDO> pageResult = winnerService.getWinnerPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/get")
    @Operation(summary = "获得中奖记录详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('treasure:winner:query')")
    public CommonResult<TreasureWinnerDO> getWinner(@RequestParam("id") Long id) {
        TreasureWinnerDO winner = winnerService.getWinner(id);
        return success(winner);
    }

    @GetMapping("/pool-page")
    @Operation(summary = "获得指定奖池的中奖记录分页")
    @Parameter(name = "poolId", description = "奖池 ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('treasure:winner:query')")
    public CommonResult<PageResult<TreasureWinnerDO>> getPoolWinnerPage(
            @RequestParam("poolId") Long poolId,
            @Valid TreasureWinnerPageReqVO pageReqVO) {
        PageResult<TreasureWinnerDO> pageResult = winnerService.getPoolWinnerPage(poolId, pageReqVO);
        return success(pageResult);
    }
}
