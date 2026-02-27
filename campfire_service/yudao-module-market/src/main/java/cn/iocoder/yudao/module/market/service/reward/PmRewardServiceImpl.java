package cn.iocoder.yudao.module.market.service.reward;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.market.dal.dataobject.reward.PmRewardDO;
import cn.iocoder.yudao.module.market.dal.mysql.reward.PmRewardMapper;
import cn.iocoder.yudao.module.market.enums.RewardStatusEnum;
import cn.iocoder.yudao.module.point.enums.PointBizTypeEnum;
import cn.iocoder.yudao.module.point.enums.PointTransactionTypeEnum;
import cn.iocoder.yudao.module.point.service.PointService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.market.enums.ErrorCodeConstants.*;

/**
 * 用户奖励 Service 实现类
 */
@Service
@Validated
@Slf4j
public class PmRewardServiceImpl implements PmRewardService {

    @Resource
    private PmRewardMapper pmRewardMapper;

    @Resource
    private PointService pointService;

    @Override
    public Long createReward(PmRewardDO reward) {
        pmRewardMapper.insert(reward);
        return reward.getId();
    }

    @Override
    public void createRewardBatch(List<PmRewardDO> rewards) {
        pmRewardMapper.insertBatch(rewards);
    }

    @Override
    public PmRewardDO getReward(Long id) {
        return pmRewardMapper.selectById(id);
    }

    @Override
    public PmRewardDO getRewardByPositionId(Long positionId) {
        return pmRewardMapper.selectByPositionId(positionId);
    }

    @Override
    public List<PmRewardDO> getRewardsByUserId(Long userId) {
        return pmRewardMapper.selectByUserId(userId);
    }

    @Override
    public PageResult<PmRewardDO> getRewardPageByUserId(Long userId, PageParam pageParam) {
        return pmRewardMapper.selectPageByUserId(userId, pageParam);
    }

    @Override
    public List<PmRewardDO> getPendingRewardsByUserId(Long userId) {
        return pmRewardMapper.selectPendingByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void claimReward(Long userId, Long rewardId) {
        PmRewardDO reward = pmRewardMapper.selectById(rewardId);
        if (reward == null) {
            throw exception(REWARD_NOT_EXISTS);
        }
        if (!reward.getUserId().equals(userId)) {
            throw exception(REWARD_NOT_EXISTS);
        }
        if (!RewardStatusEnum.PENDING.getStatus().equals(reward.getStatus())) {
            if (RewardStatusEnum.CLAIMED.getStatus().equals(reward.getStatus())) {
                throw exception(REWARD_ALREADY_CLAIMED);
            }
            throw exception(REWARD_CANNOT_CLAIM);
        }

        // 调用积分服务增加积分
        pointService.addPoints(userId, reward.getWalletAddress(), reward.getRewardAmount(),
                PointTransactionTypeEnum.MARKET_REWARD.getType(),
                PointBizTypeEnum.MARKET.getCode(), reward.getId(),
                "预测市场奖励", null);

        // 更新奖励状态
        reward.setStatus(RewardStatusEnum.CLAIMED.getStatus());
        reward.setClaimedAt(LocalDateTime.now());
        pmRewardMapper.updateById(reward);

        log.info("[claimReward][领取奖励 rewardId={}, userId={}, amount={}]",
                rewardId, userId, reward.getRewardAmount());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void claimRewardBatch(Long userId, List<Long> rewardIds) {
        for (Long rewardId : rewardIds) {
            try {
                claimReward(userId, rewardId);
            } catch (Exception e) {
                log.warn("[claimRewardBatch][领取奖励失败 rewardId={}, error={}]", rewardId, e.getMessage());
            }
        }
    }

    @Override
    public Long getPendingRewardAmount(Long userId) {
        List<PmRewardDO> pendingRewards = pmRewardMapper.selectPendingByUserId(userId);
        return pendingRewards.stream()
                .mapToLong(PmRewardDO::getRewardAmount)
                .sum();
    }

    // ========== 管理端 ==========

    @Override
    public PageResult<PmRewardDO> getRewardPage(
            cn.iocoder.yudao.module.market.controller.admin.reward.vo.RewardPageReqVO pageReqVO) {
        return pmRewardMapper.selectPage(pageReqVO);
    }

    @Override
    public cn.iocoder.yudao.module.market.controller.admin.reward.vo.RewardStatisticsVO getStatistics() {
        cn.iocoder.yudao.module.market.controller.admin.reward.vo.RewardStatisticsVO vo = new cn.iocoder.yudao.module.market.controller.admin.reward.vo.RewardStatisticsVO();

        // 查询待领取统计
        Long pendingAmount = pmRewardMapper.sumAmountByStatus(RewardStatusEnum.PENDING.getStatus());
        Integer pendingCount = pmRewardMapper.countByStatus(RewardStatusEnum.PENDING.getStatus());

        // 查询已领取统计
        Long claimedAmount = pmRewardMapper.sumAmountByStatus(RewardStatusEnum.CLAIMED.getStatus());
        Integer claimedCount = pmRewardMapper.countByStatus(RewardStatusEnum.CLAIMED.getStatus());

        vo.setPendingAmount(pendingAmount != null ? pendingAmount : 0L);
        vo.setPendingCount(pendingCount != null ? pendingCount : 0);
        vo.setClaimedAmount(claimedAmount != null ? claimedAmount : 0L);
        vo.setClaimedCount(claimedCount != null ? claimedCount : 0);

        return vo;
    }

}
