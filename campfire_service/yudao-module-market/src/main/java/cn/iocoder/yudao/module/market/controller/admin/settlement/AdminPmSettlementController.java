package cn.iocoder.yudao.module.market.controller.admin.settlement;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.market.controller.admin.settlement.vo.SettlementPageReqVO;
import cn.iocoder.yudao.module.market.controller.admin.settlement.vo.SettlementRespVO;
import cn.iocoder.yudao.module.market.convert.settlement.PmSettlementConvert;
import cn.iocoder.yudao.module.market.dal.dataobject.event.PmEventDO;
import cn.iocoder.yudao.module.market.dal.dataobject.market.PmMarketDO;
import cn.iocoder.yudao.module.market.dal.dataobject.settlement.PmSettlementDO;
import cn.iocoder.yudao.module.market.dal.dataobject.position.PmPositionDO;
import cn.iocoder.yudao.module.market.service.event.PmEventService;
import cn.iocoder.yudao.module.market.service.market.PmMarketService;
import cn.iocoder.yudao.module.market.service.position.PmPositionService;
import cn.iocoder.yudao.module.market.service.settlement.PmSettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 预测市场结算")
@RestController
@RequestMapping("/market/settlement")
@Validated
public class AdminPmSettlementController {

    @Resource
    private PmSettlementService pmSettlementService;

    @Resource
    private PmMarketService pmMarketService;

    @Resource
    private PmEventService pmEventService;

    @Resource
    private PmPositionService pmPositionService;

    @GetMapping("/pending")
    @Operation(summary = "获取待确认结算列表")
    @PreAuthorize("@ss.hasPermission('market:settlement:query')")
    public CommonResult<List<SettlementRespVO>> getPendingSettlements() {
        List<PmSettlementDO> settlements = pmSettlementService.getPendingSettlements();
        List<SettlementRespVO> result = PmSettlementConvert.INSTANCE.convertList(settlements);
        fillMarketAndEventInfo(result);
        return success(result);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询结算记录")
    @PreAuthorize("@ss.hasPermission('market:settlement:query')")
    public CommonResult<PageResult<SettlementRespVO>> getSettlementPage(@Valid SettlementPageReqVO pageReqVO) {
        PageResult<PmSettlementDO> pageResult = pmSettlementService.getSettlementPage(pageReqVO);
        PageResult<SettlementRespVO> result = PmSettlementConvert.INSTANCE.convertPage(pageResult);
        fillMarketAndEventInfo(result.getList());
        return success(result);
    }

    @GetMapping("/get")
    @Operation(summary = "获取结算详情")
    @Parameter(name = "id", description = "结算编号", required = true)
    @PreAuthorize("@ss.hasPermission('market:settlement:query')")
    public CommonResult<SettlementRespVO> getSettlement(@RequestParam("id") Long id) {
        PmSettlementDO settlement = pmSettlementService.getSettlement(id);
        if (settlement == null) {
            return success(null);
        }
        SettlementRespVO respVO = PmSettlementConvert.INSTANCE.convert(settlement);

        // 填充市场和事件信息
        PmMarketDO market = pmMarketService.getMarket(settlement.getMarketId());
        if (market != null) {
            respVO.setEventId(market.getEventId());
            respVO.setMarketQuestion(market.getQuestion());

            PmEventDO event = pmEventService.getEvent(market.getEventId());
            if (event != null) {
                respVO.setEventTitle(event.getTitle());
            }
        }

        // 对于待确认和已确认状态，实时计算持仓统计预览
        if (settlement.getStatus() != null && settlement.getStatus() < 2) {
            fillPositionStatistics(respVO, settlement.getMarketId(), settlement.getWinnerOutcome());
        }

        return success(respVO);
    }

    @PostMapping("/confirm")
    @Operation(summary = "确认结算")
    @Parameter(name = "id", description = "结算编号", required = true)
    @PreAuthorize("@ss.hasPermission('market:settlement:update')")
    public CommonResult<Boolean> confirmSettlement(@RequestParam("id") Long id) {
        Long adminId = SecurityFrameworkUtils.getLoginUserId();
        pmSettlementService.confirmSettlement(id, adminId);
        return success(true);
    }

    @PostMapping("/execute")
    @Operation(summary = "执行结算（生成奖励）")
    @Parameter(name = "id", description = "结算编号", required = true)
    @PreAuthorize("@ss.hasPermission('market:settlement:update')")
    public CommonResult<Boolean> executeSettlement(@RequestParam("id") Long id) {
        pmSettlementService.executeSettlement(id);
        return success(true);
    }

    /**
     * 填充市场和事件信息
     */
    private void fillMarketAndEventInfo(List<SettlementRespVO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        // 收集市场 ID
        Set<Long> marketIds = list.stream()
                .map(SettlementRespVO::getMarketId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (marketIds.isEmpty()) {
            return;
        }

        // 批量查询市场
        Map<Long, PmMarketDO> marketMap = new HashMap<>();
        Set<Long> eventIds = new HashSet<>();
        for (Long marketId : marketIds) {
            PmMarketDO market = pmMarketService.getMarket(marketId);
            if (market != null) {
                marketMap.put(marketId, market);
                if (market.getEventId() != null) {
                    eventIds.add(market.getEventId());
                }
            }
        }

        // 批量查询事件
        Map<Long, PmEventDO> eventMap = new HashMap<>();
        for (Long eventId : eventIds) {
            PmEventDO event = pmEventService.getEvent(eventId);
            if (event != null) {
                eventMap.put(eventId, event);
            }
        }

        // 填充信息
        for (SettlementRespVO vo : list) {
            PmMarketDO market = marketMap.get(vo.getMarketId());
            if (market != null) {
                vo.setEventId(market.getEventId());
                vo.setMarketQuestion(market.getQuestion());

                PmEventDO event = eventMap.get(market.getEventId());
                if (event != null) {
                    vo.setEventTitle(event.getTitle());
                }
            }
        }
    }

    /**
     * 填充持仓统计预览（用于待确认/已确认状态的结算）
     * 实时查询 pm_position 表计算统计数据
     */
    private void fillPositionStatistics(SettlementRespVO respVO, Long marketId, String winnerOutcome) {
        List<PmPositionDO> positions = pmPositionService.getPositionsByMarketId(marketId);

        int totalPositions = 0;
        int winningPositions = 0;
        int losingPositions = 0;
        long estimatedReward = 0L;

        for (PmPositionDO position : positions) {
            if (position.getQuantity() == null || position.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            totalPositions++;
            boolean isWinner = winnerOutcome != null && winnerOutcome.equalsIgnoreCase(position.getOutcome());

            if (isWinner) {
                winningPositions++;
                // 预估奖励：每份 100 积分
                long reward = position.getQuantity()
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(0, RoundingMode.FLOOR)
                        .longValue();
                estimatedReward += reward;
            } else {
                losingPositions++;
            }
        }

        respVO.setTotalPositions(totalPositions);
        respVO.setWinningPositions(winningPositions);
        respVO.setLosingPositions(losingPositions);
        respVO.setTotalReward(estimatedReward);
    }

}
