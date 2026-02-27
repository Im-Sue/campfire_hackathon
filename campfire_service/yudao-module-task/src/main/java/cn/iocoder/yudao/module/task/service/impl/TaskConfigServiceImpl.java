package cn.iocoder.yudao.module.task.service.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.task.controller.admin.vo.TaskRecordPageReqVO;
import cn.iocoder.yudao.module.task.controller.admin.vo.TaskRecordRespVO;
import cn.iocoder.yudao.module.task.dal.dataobject.TaskConfigDO;
import cn.iocoder.yudao.module.task.dal.dataobject.TaskRecordDO;
import cn.iocoder.yudao.module.task.dal.mysql.TaskConfigMapper;
import cn.iocoder.yudao.module.task.dal.mysql.TaskRecordMapper;
import cn.iocoder.yudao.module.task.service.TaskConfigService;
import cn.iocoder.yudao.module.wallet.api.WalletUserApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.task.enums.ErrorCodeConstants.TASK_CONFIG_NOT_EXISTS;

/**
 * 任务配置 Service 实现
 */
@Service
@Slf4j
public class TaskConfigServiceImpl implements TaskConfigService {

    @Resource
    private TaskConfigMapper taskConfigMapper;

    @Resource
    private TaskRecordMapper taskRecordMapper;

    @Resource
    private WalletUserApi walletUserApi;

    @Override
    public List<TaskConfigDO> getAllConfigs() {
        return taskConfigMapper.selectList();
    }

    @Override
    public List<TaskConfigDO> getEnabledConfigs() {
        return taskConfigMapper.selectEnabledList();
    }

    @Override
    public TaskConfigDO getConfigByTaskType(String taskType) {
        return taskConfigMapper.selectByTaskType(taskType);
    }

    @Override
    public TaskConfigDO getConfigById(Long id) {
        return taskConfigMapper.selectById(id);
    }

    @Override
    public void updateConfig(TaskConfigDO config) {
        TaskConfigDO existConfig = taskConfigMapper.selectById(config.getId());
        if (existConfig == null) {
            throw exception(TASK_CONFIG_NOT_EXISTS);
        }
        taskConfigMapper.updateById(config);
    }

    @Override
    public PageResult<TaskRecordRespVO> getTaskRecordPage(TaskRecordPageReqVO reqVO) {
        // 1. 查询分页数据
        PageResult<TaskRecordDO> pageResult = taskRecordMapper.selectPage(reqVO);

        if (pageResult.getList().isEmpty()) {
            return new PageResult<>(new ArrayList<>(), pageResult.getTotal());
        }

        // 2. 批量获取用户地址（通过 API 层调用）
        Set<Long> userIds = pageResult.getList().stream()
                .map(TaskRecordDO::getUserId)
                .collect(Collectors.toSet());

        Map<Long, String> userAddressMap = walletUserApi.getUserAddressMap(userIds);

        // 3. 转换为 RespVO
        List<TaskRecordRespVO> voList = pageResult.getList().stream().map(record -> {
            TaskRecordRespVO vo = new TaskRecordRespVO();
            vo.setId(record.getId());
            vo.setUserId(record.getUserId());
            vo.setUserAddress(userAddressMap.getOrDefault(record.getUserId(), ""));
            vo.setTaskType(record.getTaskType());
            vo.setCompleteDate(record.getCompleteDate());
            vo.setCompleteCount(record.getCompleteCount());
            vo.setRewardPoints(record.getRewardPoints());
            vo.setRewardStatus(record.getRewardStatus());
            vo.setCreateTime(record.getCreateTime());
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(voList, pageResult.getTotal());
    }

}
