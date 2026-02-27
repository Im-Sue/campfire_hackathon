package cn.iocoder.yudao.module.task.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Task 模块错误码常量
 *
 * task 模块使用 1-023-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 任务配置 1-023-001-000 ==========
    ErrorCode TASK_CONFIG_NOT_EXISTS = new ErrorCode(1_023_001_000, "任务配置不存在");
    ErrorCode TASK_NOT_ENABLED = new ErrorCode(1_023_001_001, "任务未启用");
    ErrorCode TASK_TYPE_EXISTS = new ErrorCode(1_023_001_002, "任务类型已存在");

    // ========== 任务记录 1-023-002-000 ==========
    ErrorCode TASK_RECORD_NOT_EXISTS = new ErrorCode(1_023_002_000, "任务记录不存在");
    ErrorCode TASK_ALREADY_COMPLETED = new ErrorCode(1_023_002_001, "任务已完成（达到上限）");
    ErrorCode TASK_REWARD_ALREADY_CLAIMED = new ErrorCode(1_023_002_002, "奖励已领取");
    ErrorCode TASK_NO_REWARD_TO_CLAIM = new ErrorCode(1_023_002_003, "没有可领取的奖励");
    ErrorCode TASK_CLICK_ONLY = new ErrorCode(1_023_002_004, "该任务需要手动完成");
    ErrorCode TASK_AUTO_ONLY = new ErrorCode(1_023_002_005, "该任务由系统自动完成");

}
