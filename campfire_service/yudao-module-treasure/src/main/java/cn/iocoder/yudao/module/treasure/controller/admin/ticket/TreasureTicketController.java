package cn.iocoder.yudao.module.treasure.controller.admin.ticket;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.treasure.controller.admin.ticket.vo.TreasureTicketPageReqVO;
import cn.iocoder.yudao.module.treasure.dal.dataobject.TreasureTicketDO;
import cn.iocoder.yudao.module.treasure.service.ticket.TreasureTicketService;
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
 * 管理后台 - 票号管理 Controller
 *
 * @author Sue
 */
@Tag(name = "管理后台 - 票号管理")
@RestController
@RequestMapping("/treasure/ticket")
@Validated
@Slf4j
public class TreasureTicketController {

    @Resource
    private TreasureTicketService ticketService;

    @GetMapping("/page")
    @Operation(summary = "获得票号分页")
    @PreAuthorize("@ss.hasPermission('treasure:ticket:query')")
    public CommonResult<PageResult<TreasureTicketDO>> getTicketPage(@Valid TreasureTicketPageReqVO pageReqVO) {
        PageResult<TreasureTicketDO> pageResult = ticketService.getTicketPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/get")
    @Operation(summary = "获得票号详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('treasure:ticket:query')")
    public CommonResult<TreasureTicketDO> getTicket(@RequestParam("id") Long id) {
        TreasureTicketDO ticket = ticketService.getTicket(id);
        return success(ticket);
    }
}
