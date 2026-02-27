package cn.iocoder.yudao.module.point.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.point.controller.admin.vo.PointAccountRespVO;
import cn.iocoder.yudao.module.point.controller.admin.vo.PointAdjustReqVO;
import cn.iocoder.yudao.module.point.controller.admin.vo.PointTransactionPageReqVO;
import cn.iocoder.yudao.module.point.controller.admin.vo.PointTransactionRespVO;
import cn.iocoder.yudao.module.point.convert.PointConvert;
import cn.iocoder.yudao.module.point.dal.dataobject.PointAccountDO;
import cn.iocoder.yudao.module.point.dal.dataobject.PointTransactionDO;
import cn.iocoder.yudao.module.point.service.PointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 积分
 */
@Tag(name = "管理后台 - 积分")
@RestController
@RequestMapping("/point")
@Validated
public class AdminPointController {

    @Resource
    private PointService pointService;

    @GetMapping("/user/{userId}")
    @Operation(summary = "查看用户积分")
    @Parameter(name = "userId", description = "用户ID", required = true)
    @PreAuthorize("@ss.hasPermission('point:account:query')")
    public CommonResult<PointAccountRespVO> getUserAccount(@PathVariable("userId") Long userId) {
        PointAccountDO account = pointService.getOrCreateAccount(userId);
        return success(PointConvert.INSTANCE.convertToAccountResp(account));
    }

    @GetMapping("/user/{userId}/transactions")
    @Operation(summary = "查看用户积分流水")
    @Parameter(name = "userId", description = "用户ID", required = true)
    @PreAuthorize("@ss.hasPermission('point:transaction:query')")
    public CommonResult<PageResult<PointTransactionRespVO>> getUserTransactions(
            @PathVariable("userId") Long userId,
            @Valid PointTransactionPageReqVO reqVO) {
        reqVO.setUserId(userId);
        PageResult<PointTransactionDO> pageResult = pointService.getTransactionPage(reqVO);

        // 转换并填充类型名称
        List<PointTransactionRespVO> list = pageResult.getList().stream()
                .map(PointConvert.INSTANCE::convertWithTypeName)
                .collect(Collectors.toList());

        return success(new PageResult<>(list, pageResult.getTotal()));
    }

    @PostMapping("/adjust")
    @Operation(summary = "手动调整积分")
    @PreAuthorize("@ss.hasPermission('point:account:adjust')")
    public CommonResult<Boolean> adjustPoints(@Valid @RequestBody PointAdjustReqVO reqVO) {
        pointService.adjustPoints(reqVO.getUserId(), reqVO.getWalletAddress(), reqVO.getAmount(), reqVO.getRemark());
        return success(true);
    }

    @GetMapping("/transactions/page")
    @Operation(summary = "分页查询积分流水（管理端）")
    @PreAuthorize("@ss.hasPermission('point:transaction:query')")
    public CommonResult<PageResult<PointTransactionRespVO>> getTransactionPage(@Valid PointTransactionPageReqVO reqVO) {
        // walletAddress 直接传给 Service/Mapper 查询，无需跨模块
        PageResult<PointTransactionDO> pageResult = pointService.getTransactionPage(reqVO);

        // 转换并填充类型名称
        List<PointTransactionRespVO> list = pageResult.getList().stream()
                .map(PointConvert.INSTANCE::convertWithTypeName)
                .collect(Collectors.toList());

        return success(new PageResult<>(list, pageResult.getTotal()));
    }

}
