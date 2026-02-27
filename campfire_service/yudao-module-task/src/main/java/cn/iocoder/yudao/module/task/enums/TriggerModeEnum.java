package cn.iocoder.yudao.module.task.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 触发方式枚举
 */
@Getter
@AllArgsConstructor
public enum TriggerModeEnum {

    /**
     * 自动触发 - 业务操作后自动完成
     * 例如：发帖、评论、点赞等
     */
    AUTO(1, "自动触发"),

    /**
     * 点击完成 - 用户点击"去完成"直接标记完成
     * 例如：签到、加入TG/DC、关注推特
     * 如果有 redirect_url，同时打开该链接
     */
    CLICK_COMPLETE(2, "点击完成"),

    /**
     * 点击跳转后回调 - 跳转到指定页面完成操作后由业务接口触发
     * 例如：更新个人资料
     */
    CLICK_REDIRECT(3, "点击跳转后回调");

    private final Integer value;
    private final String name;

}

