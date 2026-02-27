package cn.iocoder.yudao.module.task.dal.mysql;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.task.controller.admin.vo.TaskRecordPageReqVO;
import cn.iocoder.yudao.module.task.dal.dataobject.TaskRecordDO;
import cn.iocoder.yudao.module.task.enums.RewardStatusEnum;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

/**
 * 任务记录 Mapper
 */
@Mapper
public interface TaskRecordMapper extends BaseMapperX<TaskRecordDO> {

    /**
     * 查询用户某天某任务的记录
     */
    default TaskRecordDO selectByUserTaskDate(Long userId, String taskType, LocalDate date) {
        return selectOne(new LambdaQueryWrapperX<TaskRecordDO>()
                .eq(TaskRecordDO::getUserId, userId)
                .eq(TaskRecordDO::getTaskType, taskType)
                .eq(TaskRecordDO::getCompleteDate, date));
    }

    /**
     * 查询用户的任务记录列表
     */
    default List<TaskRecordDO> selectListByUserId(Long userId) {
        return selectList(TaskRecordDO::getUserId, userId);
    }

    /**
     * 查询用户今天的任务记录列表
     */
    default List<TaskRecordDO> selectTodayListByUserId(Long userId, LocalDate today) {
        return selectList(new LambdaQueryWrapperX<TaskRecordDO>()
                .eq(TaskRecordDO::getUserId, userId)
                .ge(TaskRecordDO::getCompleteDate, today));
    }

    /**
     * 查询用户待领取奖励的记录
     */
    default List<TaskRecordDO> selectPendingRewardsByUserId(Long userId) {
        return selectList(new LambdaQueryWrapperX<TaskRecordDO>()
                .eq(TaskRecordDO::getUserId, userId)
                .eq(TaskRecordDO::getRewardStatus, RewardStatusEnum.PENDING.getValue())
                .gt(TaskRecordDO::getRewardPoints, 0));
    }

    /**
     * 查询用户指定任务类型的待领取奖励记录
     */
    default List<TaskRecordDO> selectPendingRewardsByUserIdAndTaskType(Long userId, String taskType) {
        return selectList(new LambdaQueryWrapperX<TaskRecordDO>()
                .eq(TaskRecordDO::getUserId, userId)
                .eq(TaskRecordDO::getTaskType, taskType)
                .eq(TaskRecordDO::getRewardStatus, RewardStatusEnum.PENDING.getValue())
                .gt(TaskRecordDO::getRewardPoints, 0));
    }

    /**
     * 批量查询用户指定任务类型列表和日期的记录
     * 用于动态分类查询：一次性任务查 1970-01-01，每日任务查当天日期
     */
    default List<TaskRecordDO> selectByUserIdAndTaskTypesAndDate(Long userId, List<String> taskTypes, LocalDate date) {
        if (taskTypes == null || taskTypes.isEmpty()) {
            return List.of();
        }
        return selectList(new LambdaQueryWrapperX<TaskRecordDO>()
                .eq(TaskRecordDO::getUserId, userId)
                .in(TaskRecordDO::getTaskType, taskTypes)
                .eq(TaskRecordDO::getCompleteDate, date));
    }

    /**
     * 分页查询
     */
    default PageResult<TaskRecordDO> selectPage(TaskRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<TaskRecordDO>()
                .eqIfPresent(TaskRecordDO::getUserId, reqVO.getUserId())
                .eqIfPresent(TaskRecordDO::getTaskType, reqVO.getTaskType())
                .eqIfPresent(TaskRecordDO::getRewardStatus, reqVO.getRewardStatus())
                .betweenIfPresent(TaskRecordDO::getCompleteDate, reqVO.getCompleteDateStart(), reqVO.getCompleteDateEnd())
                .orderByDesc(TaskRecordDO::getId));
    }

}
