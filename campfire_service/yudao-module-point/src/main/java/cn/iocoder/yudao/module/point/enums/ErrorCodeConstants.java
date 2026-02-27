package cn.iocoder.yudao.module.point.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Point 模块错误码枚举
 *
 * point 模块，使用 1-021-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 积分账户 1-021-001-000 ==========
    ErrorCode POINT_ACCOUNT_NOT_EXISTS = new ErrorCode(1_021_001_000, "积分账户不存在");
    ErrorCode POINT_BALANCE_NOT_ENOUGH = new ErrorCode(1_021_001_001, "积分余额不足");

    // ========== 积分流水 1-021-002-000 ==========
    ErrorCode POINT_TRANSACTION_DUPLICATE = new ErrorCode(1_021_002_000, "积分操作重复");

}
