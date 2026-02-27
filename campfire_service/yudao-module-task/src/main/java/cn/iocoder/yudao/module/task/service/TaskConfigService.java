package cn.iocoder.yudao.module.task.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.task.controller.admin.vo.TaskRecordPageReqVO;
import cn.iocoder.yudao.module.task.controller.admin.vo.TaskRecordRespVO;
import cn.iocoder.yudao.module.task.dal.dataobject.TaskConfigDO;

import java.util.List;

/**
 * 任务配置 Service 接口
 */
public interface TaskConfigService {

    /**
     * 获取所有任务配置
     *
     * @return 配置列表
     */
    List<TaskConfigDO> getAllConfigs();

    /**
     * 获取所有启用的任务配置
     *
     * @return 配置列表
     */
    List<TaskConfigDO> getEnabledConfigs();

    /**
     * 根据任务类型获取配置
     *
     * @param taskType 任务类型
     * @return 配置
     */
    TaskConfigDO getConfigByTaskType(String taskType);

    /**
     * 根据ID获取配置
     *
     * @param id 配置ID
     * @return 配置
     */
    TaskConfigDO getConfigById(Long id);

    /**
     * 更新任务配置
     *
     * @param config 配置
     */
    void updateConfig(TaskConfigDO config);

    /**
     * 分页查询任务记录
     *
     * @param reqVO 查询条件
     * @return 分页结果
     */
    PageResult<TaskRecordRespVO> getTaskRecordPage(TaskRecordPageReqVO reqVO);

}
