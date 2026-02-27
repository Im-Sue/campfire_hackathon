package cn.iocoder.yudao.module.treasure.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 事件同步状态枚举
 *
 * @author Sue
 */
@Getter
@AllArgsConstructor
public enum SyncStatusEnum implements ArrayValuable<Integer> {

    PENDING(0, "待处理"),
    PROCESSING(1, "处理中"),
    COMPLETED(2, "已完成"),
    FAILED(3, "失败");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(SyncStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 状态值
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    /**
     * 根据状态值获取枚举
     */
    public static SyncStatusEnum valueOf(Integer status) {
        return Arrays.stream(values())
                .filter(e -> e.getStatus().equals(status))
                .findFirst()
                .orElse(null);
    }
}
