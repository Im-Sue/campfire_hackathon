package cn.iocoder.yudao.module.market.controller.app.position;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.market.controller.app.position.vo.AppPositionRespVO;
import cn.iocoder.yudao.module.market.convert.position.PmPositionConvert;
import cn.iocoder.yudao.module.market.dal.dataobject.market.PmMarketDO;
import cn.iocoder.yudao.module.market.dal.dataobject.position.PmPositionDO;
import cn.iocoder.yudao.module.market.dal.dataobject.reward.PmRewardDO;
import cn.iocoder.yudao.module.market.dal.dataobject.settlement.PmSettlementDO;
import cn.iocoder.yudao.module.market.service.market.PmMarketService;
import cn.iocoder.yudao.module.market.service.position.PmPositionService;
import cn.iocoder.yudao.module.market.service.reward.PmRewardService;
import cn.iocoder.yudao.module.market.service.settlement.PmSettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 预测市场持仓")
@RestController
@RequestMapping("/app-market/position")
@Validated
public class AppPmPositionController {

    @Resource
    private PmPositionService pmPositionService;

    @Resource
    private PmMarketService pmMarketService;

    @Resource
    private PmSettlementService pmSettlementService;

    @Resource
    private PmRewardService pmRewardService;

    @GetMapping("/list")
    @Operation(summary = "获取我的持仓列表")
    public CommonResult<List<AppPositionRespVO>> getPositionList() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        List<PmPositionDO> positions = pmPositionService.getPositionsByUserId(userId);

        // 批量查询市场信息
        Set<Long> marketIds = positions.stream()
                .map(PmPositionDO::getMarketId)
                .collect(Collectors.toSet());
        Map<Long, PmMarketDO> marketMap = pmMarketService.getMarketMap(marketIds);

        // 批量获取市场价格
        Map<Long, Map<String, BigDecimal>> pricesMap = new HashMap<>();
        for (Long marketId : marketIds) {
            pricesMap.put(marketId, pmMarketService.getMarketPrices(marketId));
        }

        // 转换并填充动态价格字段
        List<AppPositionRespVO> result = PmPositionConvert.INSTANCE.convertToAppListWithMarket(positions, marketMap);
        for (int i = 0; i < result.size(); i++) {
            AppPositionRespVO vo = result.get(i);
            PmPositionDO position = positions.get(i);

            // avgPrice 转为积分形式（×100）
            if (position.getAvgPrice() != null) {
                vo.setAvgPrice(position.getAvgPrice()
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(0, java.math.RoundingMode.DOWN)
                        .longValue());
            }

            Map<String, BigDecimal> prices = pricesMap.get(position.getMarketId());
            if (prices != null) {
                BigDecimal currentPriceRaw = prices.get(position.getOutcome());
                // currentPrice 转为积分形式（×100）
                if (currentPriceRaw != null) {
                    vo.setCurrentPrice(currentPriceRaw
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(0, java.math.RoundingMode.DOWN)
                            .longValue());
                }

                if (currentPriceRaw != null && vo.getQuantity() != null) {
                    // currentValue = currentPrice * quantity * 100 (转为积分)
                    Long currentValue = currentPriceRaw.multiply(vo.getQuantity())
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(0, java.math.RoundingMode.DOWN)
                            .longValue();
                    vo.setCurrentValue(currentValue);

                    // unrealizedPnl = currentValue - totalCost
                    if (vo.getTotalCost() != null) {
                        vo.setUnrealizedPnl(currentValue - vo.getTotalCost());
                    }
                }
            }
        }

        // 填充结算和奖励信息
        fillSettlementInfo(result, positions, marketMap);

        return success(result);
    }

    @GetMapping("/page")
    @Operation(summary = "分页获取我的持仓")
    public CommonResult<PageResult<AppPositionRespVO>> getPositionPage(PageParam pageParam) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        PageResult<PmPositionDO> pageResult = pmPositionService.getPositionPageByUserId(userId, pageParam);
        List<PmPositionDO> positions = pageResult.getList();

        // 批量查询市场信息
        Set<Long> marketIds = positions.stream()
                .map(PmPositionDO::getMarketId)
                .collect(Collectors.toSet());
        Map<Long, PmMarketDO> marketMap = pmMarketService.getMarketMap(marketIds);

        // 批量获取市场价格
        Map<Long, Map<String, BigDecimal>> pricesMap = new HashMap<>();
        for (Long marketId : marketIds) {
            pricesMap.put(marketId, pmMarketService.getMarketPrices(marketId));
        }

        // 转换并填充动态价格字段
        PageResult<AppPositionRespVO> result = PmPositionConvert.INSTANCE.convertToAppPageWithMarket(pageResult,
                marketMap);
        List<AppPositionRespVO> voList = result.getList();
        for (int i = 0; i < voList.size(); i++) {
            AppPositionRespVO vo = voList.get(i);
            PmPositionDO position = positions.get(i);

            // avgPrice 转为积分形式（×100）
            if (position.getAvgPrice() != null) {
                vo.setAvgPrice(position.getAvgPrice()
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(0, java.math.RoundingMode.DOWN)
                        .longValue());
            }

            Map<String, BigDecimal> prices = pricesMap.get(position.getMarketId());
            if (prices != null) {
                BigDecimal currentPriceRaw = prices.get(position.getOutcome());
                // currentPrice 转为积分形式（×100）
                if (currentPriceRaw != null) {
                    vo.setCurrentPrice(currentPriceRaw
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(0, java.math.RoundingMode.DOWN)
                            .longValue());
                }

                if (currentPriceRaw != null && vo.getQuantity() != null) {
                    // currentValue = currentPrice * quantity * 100 (转为积分)
                    Long currentValue = currentPriceRaw.multiply(vo.getQuantity())
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(0, java.math.RoundingMode.DOWN)
                            .longValue();
                    vo.setCurrentValue(currentValue);

                    // unrealizedPnl = currentValue - totalCost
                    if (vo.getTotalCost() != null) {
                        vo.setUnrealizedPnl(currentValue - vo.getTotalCost());
                    }
                }
            }
        }

        // 填充结算和奖励信息
        fillSettlementInfo(result.getList(), positions, marketMap);

        return success(result);
    }

    /**
     * 填充结算和奖励信息
     */
    private void fillSettlementInfo(List<AppPositionRespVO> voList, List<PmPositionDO> positions,
            Map<Long, PmMarketDO> marketMap) {
        // 批量查询结算信息（按市场ID）
        Set<Long> marketIds = positions.stream()
                .map(PmPositionDO::getMarketId)
                .collect(Collectors.toSet());
        Map<Long, PmSettlementDO> settlementMap = new HashMap<>();
        for (Long marketId : marketIds) {
            PmSettlementDO settlement = pmSettlementService.getSettlementByMarketId(marketId);
            if (settlement != null) {
                settlementMap.put(marketId, settlement);
            }
        }

        // 批量查询奖励信息（按持仓ID）
        Set<Long> positionIds = positions.stream()
                .map(PmPositionDO::getId)
                .collect(Collectors.toSet());
        Map<Long, PmRewardDO> rewardMap = new HashMap<>();
        for (Long positionId : positionIds) {
            PmRewardDO reward = pmRewardService.getRewardByPositionId(positionId);
            if (reward != null) {
                rewardMap.put(positionId, reward);
            }
        }

        // 填充每个持仓的结算和奖励信息
        for (int i = 0; i < voList.size(); i++) {
            AppPositionRespVO vo = voList.get(i);
            PmPositionDO position = positions.get(i);

            // 市场状态
            PmMarketDO market = marketMap.get(position.getMarketId());
            if (market != null) {
                vo.setMarketStatus(market.getStatus());
            }

            // 结算信息
            PmSettlementDO settlement = settlementMap.get(position.getMarketId());
            if (settlement != null) {
                vo.setSettlementStatus(settlement.getStatus());
                vo.setWinnerOutcome(settlement.getWinnerOutcome());
                vo.setIsWinner(settlement.getWinnerOutcome() != null
                        && settlement.getWinnerOutcome().equalsIgnoreCase(position.getOutcome()));
            }

            // 奖励信息
            PmRewardDO reward = rewardMap.get(position.getId());
            if (reward != null) {
                vo.setRewardId(reward.getId());
                vo.setRewardStatus(reward.getStatus());
                vo.setRewardAmount(reward.getRewardAmount());
            }
        }
    }

}
