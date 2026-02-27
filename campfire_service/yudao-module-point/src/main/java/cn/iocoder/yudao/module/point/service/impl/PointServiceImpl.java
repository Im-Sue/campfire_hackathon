package cn.iocoder.yudao.module.point.service.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.point.controller.admin.vo.PointTransactionPageReqVO;
import cn.iocoder.yudao.module.point.dal.dataobject.PointAccountDO;
import cn.iocoder.yudao.module.point.dal.dataobject.PointTransactionDO;
import cn.iocoder.yudao.module.point.dal.mysql.PointAccountMapper;
import cn.iocoder.yudao.module.point.dal.mysql.PointTransactionMapper;
import cn.iocoder.yudao.module.point.enums.PointBizTypeEnum;
import cn.iocoder.yudao.module.point.enums.PointTransactionTypeEnum;
import cn.iocoder.yudao.module.point.service.PointService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.point.enums.ErrorCodeConstants.*;

/**
 * 积分 Service 实现类
 */
@Service
@Validated
@Slf4j
public class PointServiceImpl implements PointService {

    @Resource
    private PointAccountMapper pointAccountMapper;

    @Resource
    private PointTransactionMapper pointTransactionMapper;

    // ========== 积分账户相关 ==========

    @Override
    public PointAccountDO getAccount(Long userId) {
        return pointAccountMapper.selectByUserId(userId);
    }

    @Override
    public PointAccountDO getOrCreateAccount(Long userId, String walletAddress) {
        PointAccountDO account = pointAccountMapper.selectByUserId(userId);
        if (account != null) {
            // 如果账户存在但 walletAddress 为空，更新它
            if (walletAddress != null && account.getWalletAddress() == null) {
                account.setWalletAddress(walletAddress);
                pointAccountMapper.updateById(account);
            }
            return account;
        }
        // 创建新账户
        account = PointAccountDO.builder()
                .userId(userId)
                .walletAddress(walletAddress)
                .availablePoints(0L)
                .totalEarned(0L)
                .totalSpent(0L)
                .version(0)
                .build();
        pointAccountMapper.insert(account);
        return account;
    }

    @Override
    public PointAccountDO getOrCreateAccount(Long userId) {
        return getOrCreateAccount(userId, null);
    }

    @Override
    public Long getAvailablePoints(Long userId) {
        PointAccountDO account = getAccount(userId);
        return account != null ? account.getAvailablePoints() : 0L;
    }

    // ========== 积分操作相关 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPoints(Long userId, String walletAddress, Long amount, Integer type, String bizType, Long bizId,
            String remark, Map<String, Object> extension) {
        if (amount <= 0) {
            throw new IllegalArgumentException("增加积分金额必须大于0");
        }

        // 1. 确保账户存在
        PointAccountDO account = getOrCreateAccount(userId, walletAddress);
        Long beforeBalance = account.getAvailablePoints();

        // 2. 原子增加积分
        int rows = pointAccountMapper.atomicAdd(userId, amount);
        if (rows == 0) {
            throw exception(POINT_ACCOUNT_NOT_EXISTS);
        }

        // 3. 计算变动后余额
        Long afterBalance = beforeBalance + amount;

        // 4. 写入流水
        createTransaction(userId, walletAddress, type, amount, beforeBalance, afterBalance,
                bizType, bizId, remark, extension);

        log.info("[addPoints] userId={}, walletAddress={}, amount={}, type={}, bizType={}, bizId={}, afterBalance={}",
                userId, walletAddress, amount, type, bizType, bizId, afterBalance);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductPoints(Long userId, String walletAddress, Long amount, Integer type, String bizType, Long bizId,
            String remark, Map<String, Object> extension) {
        if (amount <= 0) {
            throw new IllegalArgumentException("扣减积分金额必须大于0");
        }

        // 1. 获取当前余额
        PointAccountDO account = getAccount(userId);
        if (account == null) {
            throw exception(POINT_ACCOUNT_NOT_EXISTS);
        }
        Long beforeBalance = account.getAvailablePoints();
        // 如果 walletAddress 为空，使用账户中的
        if (walletAddress == null) {
            walletAddress = account.getWalletAddress();
        }

        // 2. 原子扣减积分（包含余额校验）
        int rows = pointAccountMapper.atomicDeduct(userId, amount);
        if (rows == 0) {
            throw exception(POINT_BALANCE_NOT_ENOUGH);
        }

        // 3. 计算变动后余额
        Long afterBalance = beforeBalance - amount;

        // 4. 写入流水（金额记为负数）
        createTransaction(userId, walletAddress, type, -amount, beforeBalance, afterBalance,
                bizType, bizId, remark, extension);

        log.info(
                "[deductPoints] userId={}, walletAddress={}, amount={}, type={}, bizType={}, bizId={}, afterBalance={}",
                userId, walletAddress, amount, type, bizType, bizId, afterBalance);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean tryDeductPoints(Long userId, String walletAddress, Long amount, Integer type, String bizType,
            Long bizId,
            String remark, Map<String, Object> extension) {
        try {
            deductPoints(userId, walletAddress, amount, type, bizType, bizId, remark, extension);
            return true;
        } catch (Exception e) {
            log.warn("[tryDeductPoints] Failed to deduct points: userId={}, amount={}, error={}",
                    userId, amount, e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustPoints(Long userId, String walletAddress, Long amount, String remark) {
        if (amount == 0) {
            throw new IllegalArgumentException("调整金额不能为0");
        }

        // 确保账户存在
        PointAccountDO account = getOrCreateAccount(userId, walletAddress);
        Long beforeBalance = account.getAvailablePoints();
        // 如果 walletAddress 为空，使用账户中的
        if (walletAddress == null) {
            walletAddress = account.getWalletAddress();
        }

        if (amount > 0) {
            // 增加积分
            pointAccountMapper.atomicAdd(userId, amount);
        } else {
            // 扣减积分
            int rows = pointAccountMapper.atomicDeduct(userId, -amount);
            if (rows == 0) {
                throw exception(POINT_BALANCE_NOT_ENOUGH);
            }
        }

        Long afterBalance = beforeBalance + amount;

        // 写入流水
        createTransaction(userId, walletAddress, PointTransactionTypeEnum.ADMIN_ADJUST.getType(), amount,
                beforeBalance, afterBalance, PointBizTypeEnum.ADMIN.getCode(),
                null, remark, null);

        log.info("[adjustPoints] userId={}, walletAddress={}, amount={}, remark={}, afterBalance={}",
                userId, walletAddress, amount, remark, afterBalance);
    }

    // ========== 积分流水相关 ==========

    @Override
    public PageResult<PointTransactionDO> getTransactionPage(Long userId, PointTransactionPageReqVO reqVO) {
        return pointTransactionMapper.selectPageByUserId(userId, reqVO);
    }

    @Override
    public PageResult<PointTransactionDO> getTransactionPage(PointTransactionPageReqVO reqVO) {
        return pointTransactionMapper.selectPage(reqVO);
    }

    // ========== 积分排行榜相关 ==========

    @Override
    public java.util.List<PointAccountDO> getPointRanking(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10; // 默认返回 Top 10
        } else if (limit > 100) {
            limit = 100; // 最大返回 100 条
        }
        return pointAccountMapper.selectTopByAvailablePoints(limit);
    }

    // ========== 私有方法 ==========

    /**
     * 创建积分流水记录
     */
    private void createTransaction(Long userId, String walletAddress, Integer type, Long amount, Long beforeBalance,
            Long afterBalance, String bizType, Long bizId,
            String remark, Map<String, Object> extension) {
        PointTransactionDO transaction = PointTransactionDO.builder()
                .userId(userId)
                .walletAddress(walletAddress)
                .type(type)
                .amount(amount)
                .beforeBalance(beforeBalance)
                .afterBalance(afterBalance)
                .bizType(bizType)
                .bizId(bizId)
                .remark(remark)
                .extension(extension)
                .build();

        try {
            pointTransactionMapper.insert(transaction);
        } catch (DuplicateKeyException e) {
            // 幂等性：如果流水已存在，说明是重复操作，忽略
            log.warn("[createTransaction] Duplicate transaction: bizType={}, bizId={}, type={}",
                    bizType, bizId, type);
            throw exception(POINT_TRANSACTION_DUPLICATE);
        }
    }

}
