package cn.iocoder.yudao.module.point.controller.app;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.point.controller.admin.vo.PointTransactionPageReqVO;
import cn.iocoder.yudao.module.point.controller.app.vo.AppPointAccountRespVO;
import cn.iocoder.yudao.module.point.controller.app.vo.AppPointRankRespVO;
import cn.iocoder.yudao.module.point.controller.app.vo.AppPointTransactionRespVO;
import cn.iocoder.yudao.module.point.convert.PointConvert;
import cn.iocoder.yudao.module.point.dal.dataobject.PointAccountDO;
import cn.iocoder.yudao.module.point.dal.dataobject.PointTransactionDO;
import cn.iocoder.yudao.module.point.dal.dataobject.WalletUserReadOnlyDO;
import cn.iocoder.yudao.module.point.dal.mysql.WalletUserReadOnlyMapper;
import cn.iocoder.yudao.module.point.service.PointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * 用户 APP - 积分
 */
@Tag(name = "用户 APP - 积分")
@RestController
@RequestMapping("/point")
@Validated
public class AppPointController {

    @Resource
    private PointService pointService;

    @Resource
    private WalletUserReadOnlyMapper walletUserReadOnlyMapper;

    @GetMapping("/account")
    @Operation(summary = "获取积分账户")
    public CommonResult<AppPointAccountRespVO> getAccount() {
        Long userId = getLoginUserId();
        PointAccountDO account = pointService.getOrCreateAccount(userId);
        return success(PointConvert.INSTANCE.convertToAppAccountResp(account));
    }

    @GetMapping("/transactions")
    @Operation(summary = "获取积分流水")
    public CommonResult<PageResult<AppPointTransactionRespVO>> getTransactions(@Valid PointTransactionPageReqVO reqVO) {
        Long userId = getLoginUserId();
        PageResult<PointTransactionDO> pageResult = pointService.getTransactionPage(userId, reqVO);

        // 转换并填充类型名称
        List<AppPointTransactionRespVO> list = pageResult.getList().stream()
                .map(PointConvert.INSTANCE::convertToAppWithTypeName)
                .collect(Collectors.toList());

        return success(new PageResult<>(list, pageResult.getTotal()));
    }

    @GetMapping("/ranking")
    @Operation(summary = "获取积分排行榜")
    @Parameter(name = "limit", description = "返回数量（默认10，最大100）", example = "10")
    @PermitAll
    public CommonResult<List<AppPointRankRespVO>> getRanking(Integer limit) {
        List<PointAccountDO> accounts = pointService.getPointRanking(limit);

        // 批量获取用户信息
        Set<Long> userIds = accounts.stream()
                .map(PointAccountDO::getUserId)
                .collect(Collectors.toSet());
        Map<Long, WalletUserReadOnlyDO> userMap = walletUserReadOnlyMapper.selectListByIds(userIds).stream()
                .collect(Collectors.toMap(WalletUserReadOnlyDO::getId, Function.identity()));

        // 转换为排行榜 VO 并添加排名
        List<AppPointRankRespVO> result = new ArrayList<>();
        for (int i = 0; i < accounts.size(); i++) {
            PointAccountDO account = accounts.get(i);
            WalletUserReadOnlyDO user = userMap.get(account.getUserId());

            AppPointRankRespVO vo = new AppPointRankRespVO();
            vo.setRank(i + 1);
            vo.setUserId(account.getUserId());
            vo.setWalletAddress(user != null ? user.getWalletAddress() : account.getWalletAddress());
            vo.setAvatar(user != null ? user.getAvatar() : null);
            vo.setAvailablePoints(account.getAvailablePoints());
            vo.setTotalEarned(account.getTotalEarned());
            result.add(vo);
        }

        return success(result);
    }

}
