package cn.iocoder.yudao.module.market.service.settlement;

import cn.iocoder.yudao.module.market.dal.dataobject.market.PmMarketDO;
import cn.iocoder.yudao.module.market.dal.dataobject.position.PmPositionDO;
import cn.iocoder.yudao.module.market.dal.dataobject.reward.PmRewardDO;
import cn.iocoder.yudao.module.market.dal.dataobject.settlement.PmSettlementDO;
import cn.iocoder.yudao.module.market.dal.mysql.settlement.PmSettlementMapper;
import cn.iocoder.yudao.module.market.enums.RewardStatusEnum;
import cn.iocoder.yudao.module.market.enums.SettlementStatusEnum;
import cn.iocoder.yudao.module.market.event.MarketSettledEvent;
import cn.iocoder.yudao.module.market.service.market.PmMarketService;
import cn.iocoder.yudao.module.market.service.position.PmPositionService;
import cn.iocoder.yudao.module.market.service.reward.PmRewardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.market.enums.ErrorCodeConstants.*;

/**
 * 市场结算 Service 实现类
 */
@Service
@Validated
@Slf4j
public class PmSettlementServiceImpl implements PmSettlementService {

    /**
     * 固定奖励：每份 100 积分
     */
    private static final Long REWARD_PER_SHARE = 100L;

    @Resource
    private PmSettlementMapper pmSettlementMapper;

    @Resource
    private PmMarketService pmMarketService;

    @Resource
    private PmPositionService pmPositionService;

    @Resource
    private PmRewardService pmRewardService;

    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public Long createSettlement(Long marketId, String polymarketId, String winnerOutcome, String source) {
        // 检查是否已存在结算记录
        PmSettlementDO existing = pmSettlementMapper.selectByMarketId(marketId);
        if (existing != null) {
            log.warn("[createSettlement][市场 {} 已存在结算记录]", marketId);
            return existing.getId();
        }

        PmSettlementDO settlement = PmSettlementDO.builder()
                .marketId(marketId)
                .polymarketId(polymarketId)
                .winnerOutcome(winnerOutcome)
                .source(source)
                .status(SettlementStatusEnum.PENDING.getStatus())
                .totalPositions(0)
                .winningPositions(0)
                .losingPositions(0)
                .totalReward(0L)
                .build();

        pmSettlementMapper.insert(settlement);
        log.info("[createSettlement][创建结算记录 settlementId={}, marketId={}, winnerOutcome={}]",
                settlement.getId(), marketId, winnerOutcome);

        // 更新市场状态为待结算
        pmMarketService.setPendingSettlement(marketId, winnerOutcome);

        return settlement.getId();
    }

    @Override
    public PmSettlementDO getSettlement(Long id) {
        return pmSettlementMapper.selectById(id);
    }

    @Override
    public PmSettlementDO getSettlementByMarketId(Long marketId) {
        return pmSettlementMapper.selectByMarketId(marketId);
    }

    @Override
    public List<PmSettlementDO> getPendingSettlements() {
        return pmSettlementMapper.selectPendingSettlements();
    }

    @Override
    public cn.iocoder.yudao.framework.common.pojo.PageResult<PmSettlementDO> getSettlementPage(
            cn.iocoder.yudao.module.market.controller.admin.settlement.vo.SettlementPageReqVO pageReqVO) {
        return pmSettlementMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmSettlement(Long id, Long adminId) {
        PmSettlementDO settlement = pmSettlementMapper.selectById(id);
        if (settlement == null) {
            throw exception(SETTLEMENT_NOT_EXISTS);
        }
        if (!SettlementStatusEnum.PENDING.getStatus().equals(settlement.getStatus())) {
            throw exception(SETTLEMENT_ALREADY_CONFIRMED);
        }

        settlement.setStatus(SettlementStatusEnum.CONFIRMED.getStatus());
        settlement.setConfirmedBy(adminId);
        settlement.setConfirmedAt(LocalDateTime.now());
        pmSettlementMapper.updateById(settlement);

        log.info("[confirmSettlement][结算确认 settlementId={}, adminId={}]", id, adminId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeSettlement(Long id) {
        PmSettlementDO settlement = pmSettlementMapper.selectById(id);
        if (settlement == null) {
            throw exception(SETTLEMENT_NOT_EXISTS);
        }
        // 幂等保护：已完成的结算直接返回
        if (SettlementStatusEnum.COMPLETED.getStatus().equals(settlement.getStatus())) {
            log.info("[executeSettlement][结算 {} 已完成，跳过]", id);
            return;
        }
        if (!SettlementStatusEnum.CONFIRMED.getStatus().equals(settlement.getStatus())) {
            log.warn("[executeSettlement][结算 {} 未确认，无法执行]", id);
            return;
        }

        // 获取市场的所有持仓
        List<PmPositionDO> positions = pmPositionService.getPositionsByMarketId(settlement.getMarketId());

        int totalPositions = 0;
        int winningPositions = 0;
        int losingPositions = 0;
        long totalReward = 0L;

        List<PmRewardDO> rewards = new ArrayList<>();

        for (PmPositionDO position : positions) {
            if (position.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            totalPositions++;
            boolean isWinner = settlement.getWinnerOutcome().equalsIgnoreCase(position.getOutcome());

            PmRewardDO reward = PmRewardDO.builder()
                    .userId(position.getUserId())
                    .walletAddress(position.getWalletAddress())
                    .marketId(settlement.getMarketId())
                    .settlementId(id)
                    .positionId(position.getId())
                    .outcome(position.getOutcome())
                    .quantity(position.getQuantity())
                    .build();

            if (isWinner) {
                // 获胜：每份 100 积分（使用 FLOOR 舍入）
                winningPositions++;
                long rewardAmount = position.getQuantity()
                        .multiply(BigDecimal.valueOf(REWARD_PER_SHARE))
                        .setScale(0, RoundingMode.FLOOR)
                        .longValue();
                reward.setRewardAmount(rewardAmount);
                reward.setStatus(RewardStatusEnum.PENDING.getStatus());
                totalReward += rewardAmount;
            } else {
                // 失败：0 积分
                losingPositions++;
                reward.setRewardAmount(0L);
                reward.setStatus(RewardStatusEnum.FAILED.getStatus());
            }

            rewards.add(reward);

            // 标记持仓已结算（保留持仓数据）
            pmPositionService.markAsSettled(position.getId());
        }

        // 批量创建奖励记录
        if (!rewards.isEmpty()) {
            pmRewardService.createRewardBatch(rewards);
        }

        // 更新结算记录
        settlement.setStatus(SettlementStatusEnum.COMPLETED.getStatus());
        settlement.setCompletedAt(LocalDateTime.now());
        settlement.setTotalPositions(totalPositions);
        settlement.setWinningPositions(winningPositions);
        settlement.setLosingPositions(losingPositions);
        settlement.setTotalReward(totalReward);
        pmSettlementMapper.updateById(settlement);

        // 更新市场状态为已结算
        pmMarketService.setSettled(settlement.getMarketId());

        // 发布市场结算事件,触发Agent结算流程
        PmMarketDO market = pmMarketService.getMarket(settlement.getMarketId());
        if (market != null && market.getEventId() != null) {
            applicationEventPublisher.publishEvent(
                new MarketSettledEvent(
                    this,
                    settlement.getMarketId(),
                    market.getEventId(),
                    settlement.getWinnerOutcome(),
                    id
                )
            );
            log.info("[executeSettlement][发布市场结算事件 marketId={}, eventId={}]",
                    settlement.getMarketId(), market.getEventId());
        }

        log.info("[executeSettlement][结算完成 settlementId={}, totalPositions={}, winningPositions={}, totalReward={}]",
                id, totalPositions, winningPositions, totalReward);
    }

    @Override
    public List<PmSettlementDO> getConfirmedSettlements() {
        return pmSettlementMapper.selectByStatus(SettlementStatusEnum.CONFIRMED.getStatus());
    }

}

