package cn.iocoder.yudao.module.market.controller.admin.reward;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.market.controller.admin.reward.vo.RewardPageReqVO;
import cn.iocoder.yudao.module.market.controller.admin.reward.vo.RewardRespVO;
import cn.iocoder.yudao.module.market.controller.admin.reward.vo.RewardStatisticsVO;
import cn.iocoder.yudao.module.market.convert.reward.PmRewardConvert;
import cn.iocoder.yudao.module.market.dal.dataobject.market.PmMarketDO;
import cn.iocoder.yudao.module.market.dal.dataobject.reward.PmRewardDO;
import cn.iocoder.yudao.module.market.service.market.PmMarketService;
import cn.iocoder.yudao.module.market.service.reward.PmRewardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 预测市场奖励")
@RestController
@RequestMapping("/market/reward")
@Validated
public class AdminPmRewardController {

    @Resource
    private PmRewardService pmRewardService;

    @Resource
    private PmMarketService pmMarketService;

    @GetMapping("/page")
    @Operation(summary = "分页获取奖励列表")
    @PreAuthorize("@ss.hasPermission('market:reward:query')")
    public CommonResult<PageResult<RewardRespVO>> getRewardPage(@Valid RewardPageReqVO pageReqVO) {
        PageResult<PmRewardDO> pageResult = pmRewardService.getRewardPage(pageReqVO);
        PageResult<RewardRespVO> result = PmRewardConvert.INSTANCE.convertAdminPage(pageResult);

        // 填充市场信息
        fillMarketInfo(result.getList());

        return success(result);
    }

    @GetMapping("/get")
    @Operation(summary = "获取奖励详情")
    @Parameter(name = "id", description = "奖励编号", required = true)
    @PreAuthorize("@ss.hasPermission('market:reward:query')")
    public CommonResult<RewardRespVO> getReward(@RequestParam("id") Long id) {
        PmRewardDO reward = pmRewardService.getReward(id);
        if (reward == null) {
            return success(null);
        }
        RewardRespVO respVO = PmRewardConvert.INSTANCE.convertAdmin(reward);

        // 填充市场信息
        PmMarketDO market = pmMarketService.getMarket(reward.getMarketId());
        if (market != null) {
            respVO.setMarketQuestion(market.getQuestion());
        }

        return success(respVO);
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取奖励统计")
    @PreAuthorize("@ss.hasPermission('market:reward:query')")
    public CommonResult<RewardStatisticsVO> getStatistics() {
        return success(pmRewardService.getStatistics());
    }

    /**
     * 填充市场信息
     */
    private void fillMarketInfo(List<RewardRespVO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        // 收集市场 ID
        Set<Long> marketIds = list.stream()
                .map(RewardRespVO::getMarketId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (marketIds.isEmpty()) {
            return;
        }

        // 批量查询市场
        Map<Long, PmMarketDO> marketMap = new HashMap<>();
        for (Long marketId : marketIds) {
            PmMarketDO market = pmMarketService.getMarket(marketId);
            if (market != null) {
                marketMap.put(marketId, market);
            }
        }

        // 填充信息
        for (RewardRespVO vo : list) {
            PmMarketDO market = marketMap.get(vo.getMarketId());
            if (market != null) {
                vo.setMarketQuestion(market.getQuestion());
            }
        }
    }

}
