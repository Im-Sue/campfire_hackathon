package cn.iocoder.yudao.module.treasure.controller.app.winner;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.treasure.controller.admin.winner.vo.TreasureWinnerPageReqVO;
import cn.iocoder.yudao.module.treasure.dal.dataobject.TreasureWinnerDO;
import cn.iocoder.yudao.module.treasure.service.winner.TreasureWinnerService;
import cn.iocoder.yudao.module.wallet.dal.dataobject.WalletUserDO;
import cn.iocoder.yudao.module.wallet.service.WalletUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * 用户端 - 中奖记录管理 Controller
 *
 * @author Sue
 */
@Tag(name = "用户端 - 中奖记录管理")
@RestController
@RequestMapping("/treasure/winner")
@Validated
@Slf4j
public class AppTreasureWinnerController {

    @Resource
    private TreasureWinnerService winnerService;

    @Resource
    private WalletUserService walletUserService;

    @GetMapping("/my-page")
    @Operation(summary = "获得我的中奖记录分页")
    public CommonResult<PageResult<TreasureWinnerDO>> getMyWinnerPage(@Valid TreasureWinnerPageReqVO pageReqVO) {
        // 从框架获取当前登录用户 ID
        Long userId = getLoginUserId();
        // 通过 WalletUserService 获取钱包地址
        WalletUserDO walletUser = walletUserService.getUser(userId);
        String userAddress = walletUser.getWalletAddress();

        PageResult<TreasureWinnerDO> pageResult = winnerService.getUserWinnerPage(userAddress, pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/my-list")
    @Operation(summary = "获得我的中奖记录列表（兼容端点）")
    public CommonResult<PageResult<TreasureWinnerDO>> getMyWinnerList(@Valid TreasureWinnerPageReqVO pageReqVO) {
        return getMyWinnerPage(pageReqVO);
    }

    @GetMapping("/pool-page")
    @Operation(summary = "获得指定奖池的中奖记录分页")
    @Parameter(name = "poolId", description = "奖池 ID", required = true, example = "1")
    public CommonResult<PageResult<TreasureWinnerDO>> getPoolWinnerPage(
            @RequestParam("poolId") Long poolId,
            @Valid TreasureWinnerPageReqVO pageReqVO) {
        PageResult<TreasureWinnerDO> pageResult = winnerService.getPoolWinnerPage(poolId, pageReqVO);
        return success(pageResult);
    }
}
