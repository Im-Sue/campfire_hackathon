package cn.iocoder.yudao.module.treasure.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Treasure 错误码枚举
 * <p>
 * treasure 模块，使用 1-030-000-000 段
 *
 * @author Sue
 */
public interface ErrorCodeConstants {

    // ========== 奖池相关 1-030-001-000 ==========
    ErrorCode POOL_NOT_EXISTS = new ErrorCode(1_030_001_000, "奖池不存在");
    ErrorCode POOL_NOT_ACTIVE = new ErrorCode(1_030_001_001, "奖池未激活");
    ErrorCode POOL_ALREADY_SETTLED = new ErrorCode(1_030_001_002, "奖池已结算");
    ErrorCode POOL_NOT_ENDED = new ErrorCode(1_030_001_003, "奖池未结束");
    ErrorCode POOL_SOLD_OUT = new ErrorCode(1_030_001_004, "奖池已售罄");

    // ========== 票号相关 1-030-002-000 ==========
    ErrorCode TICKET_NOT_EXISTS = new ErrorCode(1_030_002_000, "票号不存在");
    ErrorCode TICKET_ALREADY_PURCHASED = new ErrorCode(1_030_002_001, "用户已购买票号");
    ErrorCode TICKET_NOT_WINNER = new ErrorCode(1_030_002_002, "该票号未中奖");
    ErrorCode TICKET_ALREADY_CLAIMED = new ErrorCode(1_030_002_003, "奖金已领取");

    // ========== 合约相关 1-030-003-000 ==========
    ErrorCode CONTRACT_CALL_FAILED = new ErrorCode(1_030_003_000, "合约调用失败");
    ErrorCode CONTRACT_NOT_CONFIGURED = new ErrorCode(1_030_003_001, "合约未配置");
    ErrorCode INSUFFICIENT_BALANCE = new ErrorCode(1_030_003_002, "余额不足");
    ErrorCode TRANSACTION_FAILED = new ErrorCode(1_030_003_003, "交易失败");

    // ========== 事件同步相关 1-030-004-000 ==========
    ErrorCode EVENT_SYNC_FAILED = new ErrorCode(1_030_004_000, "事件同步失败");
    ErrorCode EVENT_ALREADY_PROCESSED = new ErrorCode(1_030_004_001, "事件已处理");
    ErrorCode EVENT_DATA_INVALID = new ErrorCode(1_030_004_002, "事件数据无效");
}
