package cn.iocoder.yudao.module.task.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.task.dal.dataobject.TaskConfigDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 任务配置 Mapper
 */
@Mapper
public interface TaskConfigMapper extends BaseMapperX<TaskConfigDO> {

    /**
     * 根据任务类型查询配置
     */
    default TaskConfigDO selectByTaskType(String taskType) {
        return selectOne(TaskConfigDO::getTaskType, taskType);
    }

    /**
     * 查询所有启用的任务配置
     */
    default List<TaskConfigDO> selectEnabledList() {
        return selectList(TaskConfigDO::getEnabled, true);
    }

    /**
     * 查询所有任务配置（按排序）
     */
    default List<TaskConfigDO> selectAllOrderBySort() {
        return selectList(null); // TODO: add order by sort
    }

}
