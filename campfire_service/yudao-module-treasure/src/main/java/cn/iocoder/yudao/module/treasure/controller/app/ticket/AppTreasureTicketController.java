package cn.iocoder.yudao.module.treasure.controller.app.ticket;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.treasure.controller.admin.ticket.vo.TreasureTicketPageReqVO;
import cn.iocoder.yudao.module.treasure.dal.dataobject.TreasureTicketDO;
import cn.iocoder.yudao.module.treasure.service.ticket.TreasureTicketService;
import cn.iocoder.yudao.module.wallet.dal.dataobject.WalletUserDO;
import cn.iocoder.yudao.module.wallet.service.WalletUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * 用户端 - 票号管理 Controller
 *
 * @author Sue
 */
@Tag(name = "用户端 - 票号管理")
@RestController
@RequestMapping("/treasure/ticket")
@Validated
@Slf4j
public class AppTreasureTicketController {

    @Resource
    private TreasureTicketService ticketService;

    @Resource
    private WalletUserService walletUserService;

    @GetMapping("/my-page")
    @Operation(summary = "获得我的票号分页")
    public CommonResult<PageResult<TreasureTicketDO>> getMyTicketPage(@Valid TreasureTicketPageReqVO pageReqVO) {
        // 从框架获取当前登录用户 ID
        Long userId = getLoginUserId();
        // 通过 WalletUserService 获取钱包地址
        WalletUserDO walletUser = walletUserService.getUser(userId);
        String userAddress = walletUser.getWalletAddress();

        PageResult<TreasureTicketDO> pageResult = ticketService.getUserTicketPage(userAddress, pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/my-list")
    @Operation(summary = "获得我的参与记录（兼容端点）")
    public CommonResult<PageResult<TreasureTicketDO>> getMyTicketList(@Valid TreasureTicketPageReqVO pageReqVO) {
        return getMyTicketPage(pageReqVO);
    }

    @GetMapping("/my-wins")
    @Operation(summary = "获得我的中奖记录（兼容端点）")
    public CommonResult<PageResult<TreasureTicketDO>> getMyWins(@Valid TreasureTicketPageReqVO pageReqVO) {
        pageReqVO.setIsWinner(true);
        return getMyTicketPage(pageReqVO);
    }
}
