package cn.iocoder.yudao.module.market.controller.app.reward;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.market.controller.app.reward.vo.AppRewardRespVO;
import cn.iocoder.yudao.module.market.convert.reward.PmRewardConvert;
import cn.iocoder.yudao.module.market.dal.dataobject.reward.PmRewardDO;
import cn.iocoder.yudao.module.market.service.reward.PmRewardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 预测市场奖励")
@RestController
@RequestMapping("/app-market/reward")
@Validated
public class AppPmRewardController {

    @Resource
    private PmRewardService pmRewardService;

    @GetMapping("/list")
    @Operation(summary = "获取我的奖励列表")

    public CommonResult<List<AppRewardRespVO>> getRewardList() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        List<PmRewardDO> rewards = pmRewardService.getRewardsByUserId(userId);
        return success(PmRewardConvert.INSTANCE.convertToAppList(rewards));
    }

    @GetMapping("/page")
    @Operation(summary = "分页获取我的奖励")

    public CommonResult<PageResult<AppRewardRespVO>> getRewardPage(PageParam pageParam) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        PageResult<PmRewardDO> pageResult = pmRewardService.getRewardPageByUserId(userId, pageParam);
        return success(PmRewardConvert.INSTANCE.convertToAppPage(pageResult));
    }

    @GetMapping("/pending")
    @Operation(summary = "获取待领取奖励")

    public CommonResult<List<AppRewardRespVO>> getPendingRewards() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        List<PmRewardDO> rewards = pmRewardService.getPendingRewardsByUserId(userId);
        return success(PmRewardConvert.INSTANCE.convertToAppList(rewards));
    }

    @GetMapping("/pending-amount")
    @Operation(summary = "获取待领取奖励总额")

    public CommonResult<Long> getPendingRewardAmount() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(pmRewardService.getPendingRewardAmount(userId));
    }

    @PostMapping("/claim")
    @Operation(summary = "领取奖励")
    @Parameter(name = "id", description = "奖励编号", required = true)

    public CommonResult<Boolean> claimReward(@RequestParam("id") Long id) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        pmRewardService.claimReward(userId, id);
        return success(true);
    }

    @PostMapping("/claim-batch")
    @Operation(summary = "批量领取奖励")

    public CommonResult<Boolean> claimRewardBatch(@RequestBody List<Long> ids) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        pmRewardService.claimRewardBatch(userId, ids);
        return success(true);
    }

}
