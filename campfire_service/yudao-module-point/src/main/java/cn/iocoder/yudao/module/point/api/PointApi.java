package cn.iocoder.yudao.module.point.api;

import cn.iocoder.yudao.module.point.enums.PointBizTypeEnum;
import cn.iocoder.yudao.module.point.enums.PointTransactionTypeEnum;
import cn.iocoder.yudao.module.point.service.PointService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Map;

/**
 * 积分 API 实现类
 *
 * 提供给其他模块调用的统一入口
 * 注意：调用方需要传入 walletAddress 进行冗余存储
 */
@Service
public class PointApi {

    @Resource
    private PointService pointService;

    /**
     * 增加积分（任务奖励）
     */
    public void addTaskReward(Long userId, String walletAddress, Long amount, Long taskRecordId, String remark) {
        pointService.addPoints(userId, walletAddress, amount,
                PointTransactionTypeEnum.TASK_REWARD.getType(),
                PointBizTypeEnum.TASK.getCode(),
                taskRecordId, remark, null);
    }

    /**
     * 扣减积分（下单消费）
     */
    public void deductOrderConsume(Long userId, String walletAddress, Long amount, Long orderId, String remark) {
        pointService.deductPoints(userId, walletAddress, amount,
                PointTransactionTypeEnum.ORDER_CONSUME.getType(),
                PointBizTypeEnum.ORDER.getCode(),
                orderId, remark, null);
    }

    /**
     * 尝试扣减积分（下单消费，不抛异常）
     */
    public boolean tryDeductOrderConsume(Long userId, String walletAddress, Long amount, Long orderId, String remark) {
        return pointService.tryDeductPoints(userId, walletAddress, amount,
                PointTransactionTypeEnum.ORDER_CONSUME.getType(),
                PointBizTypeEnum.ORDER.getCode(),
                orderId, remark, null);
    }

    /**
     * 增加积分（卖单收入）
     */
    public void addSellIncome(Long userId, String walletAddress, Long amount, Long orderId, String remark,
            Map<String, Object> extension) {
        pointService.addPoints(userId, walletAddress, amount,
                PointTransactionTypeEnum.SELL_INCOME.getType(),
                PointBizTypeEnum.ORDER.getCode(),
                orderId, remark, extension);
    }

    /**
     * 增加积分（订单结算领取）
     */
    public void addSettlementClaim(Long userId, String walletAddress, Long amount, Long rewardRecordId, String remark) {
        pointService.addPoints(userId, walletAddress, amount,
                PointTransactionTypeEnum.SETTLEMENT_CLAIM.getType(),
                PointBizTypeEnum.REWARD.getCode(),
                rewardRecordId, remark, null);
    }

    /**
     * 查询可用余额
     */
    public Long getAvailablePoints(Long userId) {
        return pointService.getAvailablePoints(userId);
    }

    /**
     * 批量查询用户积分账户
     *
     * @param userIds 用户ID列表
     * @return Map: userId -> PointAccountDO
     */
    public Map<Long, cn.iocoder.yudao.module.point.dal.dataobject.PointAccountDO> getAccountsByUserIds(
            java.util.List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return java.util.Collections.emptyMap();
        }
        return userIds.stream()
                .map(userId -> pointService.getAccount(userId))
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toMap(
                        cn.iocoder.yudao.module.point.dal.dataobject.PointAccountDO::getUserId,
                        account -> account));
    }

}
