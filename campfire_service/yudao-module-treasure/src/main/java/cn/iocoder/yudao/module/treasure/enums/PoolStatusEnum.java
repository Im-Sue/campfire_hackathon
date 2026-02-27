package cn.iocoder.yudao.module.treasure.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 奖池状态枚举
 *
 * @author Sue
 */
@Getter
@AllArgsConstructor
public enum PoolStatusEnum implements ArrayValuable<Integer> {

    ACTIVE(0, "进行中"),
    LOCKED(1, "已锁定"),
    DRAWING(2, "开奖中"),
    SETTLED(3, "已结算");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(PoolStatusEnum::getStatus).toArray(Integer[]::new);

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
    public static PoolStatusEnum valueOf(Integer status) {
        return Arrays.stream(values())
                .filter(e -> e.getStatus().equals(status))
                .findFirst()
                .orElse(null);
    }
}
