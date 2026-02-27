package cn.iocoder.yudao.module.task.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.task.enums.RewardStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDate;

/**
 * 任务完成记录 DO
 */
@TableName("task_record")
@KeySequence("task_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRecordDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 任务类型
     */
    private String taskType;

    /**
     * 完成日期
     * 一次性任务使用固定日期 1970-01-01
     */
    private LocalDate completeDate;

    /**
     * 当日完成次数
     */
    private Integer completeCount;

    /**
     * 奖励状态
     *
     * 枚举 {@link RewardStatusEnum}
     */
    private Integer rewardStatus;

    /**
     * 奖励积分（快照）
     */
    private Long rewardPoints;

    /**
     * 关联业务ID
     */
    private Long bizId;

    /**
     * 已领取次数（用于生成唯一的积分流水 bizId）
     */
    private Integer claimCount;

}
