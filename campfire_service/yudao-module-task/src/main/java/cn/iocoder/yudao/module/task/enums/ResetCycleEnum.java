package cn.iocoder.yudao.module.task.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 重置周期枚举
 */
@Getter
@AllArgsConstructor
public enum ResetCycleEnum {

    /**
     * 一次性任务 - 完成后永久有效
     * 例如：注册、完善资料、加入TG/DC
     */
    ONCE(1, "一次性"),

    /**
     * 每日重置 - UTC+8 00:00 重置
     * 例如：签到、评论、点赞、发帖
     */
    DAILY(2, "每日重置"),

    /**
     * 无限次 - 无上限，可重复完成
     * 例如：邀请好友
     */
    UNLIMITED(3, "无限次");

    private final Integer value;
    private final String name;

}

