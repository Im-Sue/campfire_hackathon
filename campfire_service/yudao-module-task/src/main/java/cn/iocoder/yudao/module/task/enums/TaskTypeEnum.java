package cn.iocoder.yudao.module.task.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务类型枚举
 */
@Getter
@AllArgsConstructor
public enum TaskTypeEnum {

    // ===== 一次性任务 =====
    REGISTER(1, "链接钱包", TriggerModeEnum.AUTO, ResetCycleEnum.ONCE),
    UPDATE_PROFILE(2, "更改用户资料", TriggerModeEnum.CLICK_REDIRECT, ResetCycleEnum.ONCE),

    // ===== 外部跳转任务 =====
    JOIN_TG(3, "加入TG", TriggerModeEnum.CLICK_COMPLETE, ResetCycleEnum.ONCE),
    JOIN_DC(4, "加入DC", TriggerModeEnum.CLICK_COMPLETE, ResetCycleEnum.ONCE),
    FOLLOW_TWITTER(5, "关注推特", TriggerModeEnum.CLICK_COMPLETE, ResetCycleEnum.ONCE),
    RETWEET(6, "转发推特", TriggerModeEnum.CLICK_COMPLETE, ResetCycleEnum.DAILY),

    // ===== 社交互动任务 =====
    COMMENT(7, "评论活跃", TriggerModeEnum.AUTO, ResetCycleEnum.DAILY),
    LIKE(8, "点赞互动", TriggerModeEnum.AUTO, ResetCycleEnum.DAILY),
    POST(9, "发布文章", TriggerModeEnum.AUTO, ResetCycleEnum.DAILY),
    SIGN_IN(10, "每日签到", TriggerModeEnum.CLICK_COMPLETE, ResetCycleEnum.DAILY),
    SHARE(11, "分享传播", TriggerModeEnum.CLICK_COMPLETE, ResetCycleEnum.DAILY),

    // ===== 预测任务 =====
    PLACE_ORDER(12, "参与下注", TriggerModeEnum.AUTO, ResetCycleEnum.DAILY),
    FIRST_WIN(13, "每日首胜", TriggerModeEnum.AUTO, ResetCycleEnum.DAILY),

    // ===== 社交增长 =====
    INVITE_FRIEND(14, "邀请好友", TriggerModeEnum.AUTO, ResetCycleEnum.DAILY);

    /**
     * 类型值
     */
    private final Integer value;

    /**
     * 类型名称
     */
    private final String name;

    /**
     * 触发方式
     */
    private final TriggerModeEnum triggerMode;

    /**
     * 重置周期
     */
    private final ResetCycleEnum resetCycle;

    /**
     * 根据名称查找枚举
     */
    public static TaskTypeEnum getByName(String name) {
        for (TaskTypeEnum type : values()) {
            if (type.name().equals(name)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 是否是自动触发任务
     */
    public boolean isAuto() {
        return this.triggerMode == TriggerModeEnum.AUTO;
    }

    /**
     * 是否是点击完成任务
     */
    public boolean isClickComplete() {
        return this.triggerMode == TriggerModeEnum.CLICK_COMPLETE
                || this.triggerMode == TriggerModeEnum.CLICK_REDIRECT;
    }

    /**
     * 是否是每日重置任务
     */
    public boolean isDaily() {
        return this.resetCycle == ResetCycleEnum.DAILY;
    }

    /**
     * 是否是一次性任务
     */
    public boolean isOnce() {
        return this.resetCycle == ResetCycleEnum.ONCE;
    }

}
