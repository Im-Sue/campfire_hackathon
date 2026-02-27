package cn.iocoder.yudao.module.task.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.task.enums.ResetCycleEnum;
import cn.iocoder.yudao.module.task.enums.TriggerModeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 任务配置 DO
 */
@TableName("task_config")
@KeySequence("task_config_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskConfigDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 任务类型枚举值
     */
    private String taskType;

    /**
     * 任务名称
     */
    private String name;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 触发方式
     *
     * 枚举 {@link TriggerModeEnum}
     */
    private Integer triggerMode;

    /**
     * 重置周期
     *
     * 枚举 {@link ResetCycleEnum}
     */
    private Integer resetCycle;

    /**
     * 奖励积分
     */
    private Long rewardPoints;

    /**
     * 每日完成上限（0=无限）
     */
    private Integer dailyLimit;

    /**
     * 跳转URL
     */
    private String redirectUrl;

    /**
     * 任务图标
     */
    private String iconUrl;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 任务名称(英文)
     */
    private String nameEn;

    /**
     * 任务描述(英文)
     */
    private String descriptionEn;

    /**
     * 任务图片URL
     */
    private String imageUrl;

}
