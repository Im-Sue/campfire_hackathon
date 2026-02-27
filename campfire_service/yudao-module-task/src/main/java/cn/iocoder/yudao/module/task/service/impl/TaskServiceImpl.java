package cn.iocoder.yudao.module.task.service.impl;

import cn.iocoder.yudao.module.point.api.PointApi;
import cn.iocoder.yudao.module.task.controller.app.vo.AppInviteInfoRespVO;
import cn.iocoder.yudao.module.task.controller.app.vo.AppTaskCompleteRespVO;
import cn.iocoder.yudao.module.task.controller.app.vo.AppTaskItemRespVO;
import cn.iocoder.yudao.module.task.dal.dataobject.TaskConfigDO;
import cn.iocoder.yudao.module.task.dal.dataobject.TaskRecordDO;
import cn.iocoder.yudao.module.task.dal.mysql.TaskConfigMapper;
import cn.iocoder.yudao.module.task.dal.mysql.TaskRecordMapper;
import cn.iocoder.yudao.module.task.enums.ResetCycleEnum;
import cn.iocoder.yudao.module.task.enums.RewardStatusEnum;
import cn.iocoder.yudao.module.task.enums.TaskTypeEnum;
import cn.iocoder.yudao.module.task.enums.TriggerModeEnum;
import cn.iocoder.yudao.framework.mq.biz.message.TaskTriggerMessage;
import cn.iocoder.yudao.module.task.service.TaskService;
import cn.iocoder.yudao.module.wallet.api.WalletUserApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.task.enums.ErrorCodeConstants.*;

/**
 * 任务 Service 实现
 */
@Service
@Slf4j
public class TaskServiceImpl implements TaskService {

    /** 一次性任务使用的固定日期 */
    private static final LocalDate ONCE_DATE = LocalDate.of(1970, 1, 1);

    /** 时区 UTC+8 */
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");

    @Resource
    private TaskConfigMapper taskConfigMapper;

    @Resource
    private TaskRecordMapper taskRecordMapper;

    @Resource
    private PointApi pointApi;

    @Resource
    private WalletUserApi walletUserApi;

    @Override
    public List<AppTaskItemRespVO> getTaskList(Long userId, String category) {
        // 1. 获取所有启用的任务配置
        List<TaskConfigDO> configs = taskConfigMapper.selectEnabledList();

        // 1.1 按分类筛选
        if ("new".equals(category)) {
            // 新手任务：一次性任务 (resetCycle = 1)
            configs = configs.stream()
                    .filter(c -> c.getResetCycle() == 1)
                    .collect(Collectors.toList());
        } else if ("daily".equals(category)) {
            // 日常任务：每日重置任务 (resetCycle = 2)
            configs = configs.stream()
                    .filter(c -> c.getResetCycle() == 2)
                    .collect(Collectors.toList());
        }
        // category 为空或其他值则返回全部

        // 2. 获取用户的任务记录（仅登录用户）
        Map<String, TaskRecordDO> recordMap;
        if (userId != null) {
            LocalDate today = LocalDate.now(ZONE_ID);
            
            // 2.1 从配置中动态分类任务类型
            List<String> onceTaskTypes = configs.stream()
                    .filter(c -> c.getResetCycle().equals(ResetCycleEnum.ONCE.getValue()))
                    .map(TaskConfigDO::getTaskType)
                    .collect(Collectors.toList());
            List<String> dailyTaskTypes = configs.stream()
                    .filter(c -> c.getResetCycle().equals(ResetCycleEnum.DAILY.getValue()))
                    .map(TaskConfigDO::getTaskType)
                    .collect(Collectors.toList());
            
            // 2.2 分别查询：一次性任务查 1970-01-01，每日任务查当天
            List<TaskRecordDO> records = new ArrayList<>();
            records.addAll(taskRecordMapper.selectByUserIdAndTaskTypesAndDate(userId, onceTaskTypes, ONCE_DATE));
            records.addAll(taskRecordMapper.selectByUserIdAndTaskTypesAndDate(userId, dailyTaskTypes, today));
            
            // 2.3 转为 Map: taskType -> record
            recordMap = records.stream()
                    .collect(Collectors.toMap(TaskRecordDO::getTaskType, r -> r, (a, b) -> a));
        } else {
            // 未登录用户：返回空的记录映射，所有任务显示默认状态
            recordMap = Collections.emptyMap();
        }

        // 3. 组装返回数据
        List<AppTaskItemRespVO> result = new ArrayList<>();
        for (TaskConfigDO config : configs) {
            AppTaskItemRespVO item = new AppTaskItemRespVO();
            item.setTaskType(config.getTaskType());
            item.setName(config.getName());
            item.setDescription(config.getDescription());
            item.setTriggerMode(config.getTriggerMode());
            item.setResetCycle(config.getResetCycle());
            item.setRewardPoints(config.getRewardPoints());
            item.setDailyLimit(config.getDailyLimit());
            item.setRedirectUrl(config.getRedirectUrl());
            item.setIconUrl(config.getIconUrl());
            item.setNameEn(config.getNameEn());
            item.setDescriptionEn(config.getDescriptionEn());
            item.setImageUrl(config.getImageUrl());

            // 用户状态
            TaskRecordDO record = recordMap.get(config.getTaskType());
            if (record != null) {
                item.setTodayCount(record.getCompleteCount());
                item.setCompleted(config.getDailyLimit() > 0 && record.getCompleteCount() >= config.getDailyLimit());
                item.setCanClaim(record.getRewardStatus().equals(RewardStatusEnum.PENDING.getValue())
                        && record.getRewardPoints() > 0);
                item.setPendingRewards(record.getRewardStatus().equals(RewardStatusEnum.PENDING.getValue())
                        ? record.getRewardPoints()
                        : 0L);
                item.setRecordId(record.getId());
            } else {
                item.setTodayCount(0);
                item.setCompleted(false);
                item.setCanClaim(false);
                item.setPendingRewards(0L);
                item.setRecordId(null);
            }

            result.add(item);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppTaskCompleteRespVO completeClickTask(Long userId, String taskType) {
        // 1. 校验任务类型
        TaskTypeEnum typeEnum = TaskTypeEnum.getByName(taskType);
        if (typeEnum == null) {
            throw exception(TASK_CONFIG_NOT_EXISTS);
        }

        // 2. 校验是否是点击完成类型
        if (!typeEnum.isClickComplete()) {
            throw exception(TASK_AUTO_ONLY);
        }

        // 3. 获取任务配置
        TaskConfigDO config = taskConfigMapper.selectByTaskType(taskType);
        if (config == null || !config.getEnabled()) {
            throw exception(TASK_NOT_ENABLED);
        }

        // 4. 处理任务完成
        return doCompleteTask(userId, config, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppTaskCompleteRespVO signIn(Long userId) {
        return completeClickTask(userId, TaskTypeEnum.SIGN_IN.name());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long claimReward(Long userId, Long recordId) {
        // 1. 校验记录
        TaskRecordDO record = taskRecordMapper.selectById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw exception(TASK_RECORD_NOT_EXISTS);
        }
        if (record.getRewardStatus().equals(RewardStatusEnum.CLAIMED.getValue())) {
            throw exception(TASK_REWARD_ALREADY_CLAIMED);
        }
        if (record.getRewardPoints() <= 0) {
            throw exception(TASK_NO_REWARD_TO_CLAIM);
        }

        // 2. 发放积分
        Long claimedPoints = record.getRewardPoints();
        // 生成唯一 bizId：首次沿用旧格式，第2次及以后使用偏移量
        Integer currentClaimCount = record.getClaimCount() == null ? 0 : record.getClaimCount();
        Long uniqueBizId;
        if (currentClaimCount == 0) {
            uniqueBizId = recordId; // 首次领取：兼容历史数据
        } else {
            // 第2次及以后：10亿偏移 + recordId*10000 + claimCount
            uniqueBizId = 1_000_000_000L + recordId * 10000L + currentClaimCount;
        }
        pointApi.addTaskReward(userId, null, claimedPoints, uniqueBizId,
                "任务奖励: " + record.getTaskType());
        log.info("[claimReward] 用户 {} 领取任务 {} 奖励 {} 积分, bizId={}",
                userId, record.getTaskType(), claimedPoints, uniqueBizId);

        // 3. 更新状态（重置 rewardPoints 为 0，增加领取次数）
        record.setRewardStatus(RewardStatusEnum.CLAIMED.getValue());
        record.setRewardPoints(0L);
        record.setClaimCount(currentClaimCount + 1);
        log.info("[claimReward] 准备更新记录: recordId={}, claimCount: {} -> {}",
                recordId, currentClaimCount, record.getClaimCount());
        taskRecordMapper.updateById(record);
        log.info("[claimReward] 更新完成: recordId={}, 新的claimCount={}", recordId, record.getClaimCount());

        return claimedPoints;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long claimAllRewards(Long userId) {
        // 1. 获取所有待领取的记录
        List<TaskRecordDO> pendingRecords = taskRecordMapper.selectPendingRewardsByUserId(userId);
        if (pendingRecords.isEmpty()) {
            throw exception(TASK_NO_REWARD_TO_CLAIM);
        }

        // 2. 计算总积分
        long totalPoints = pendingRecords.stream()
                .mapToLong(TaskRecordDO::getRewardPoints)
                .sum();

        // 3. 发放积分（一次性发放总积分，使用时间戳作为唯一 bizId）
        Long uniqueBizId = System.currentTimeMillis();
        pointApi.addTaskReward(userId, null, totalPoints, uniqueBizId,
                "一键领取任务奖励 (" + pendingRecords.size() + "个任务)");
        log.info("[claimAllRewards] 用户 {} 一键领取 {} 个任务奖励，共 {} 积分, bizId={}",
                userId, pendingRecords.size(), totalPoints, uniqueBizId);

        // 4. 更新状态和领取次数
        for (TaskRecordDO record : pendingRecords) {
            record.setRewardStatus(RewardStatusEnum.CLAIMED.getValue());
            Integer currentClaimCount = record.getClaimCount() == null ? 0 : record.getClaimCount();
            record.setClaimCount(currentClaimCount + 1);
            taskRecordMapper.updateById(record);
        }

        return totalPoints;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processTaskTrigger(TaskTriggerMessage message) {
        String taskType = message.getTaskType();
        Long userId = message.getUserId();

        log.info("[processTaskTrigger] 收到任务触发消息: taskType={}, userId={}, bizId={}",
                taskType, userId, message.getBizId());

        // 1. 校验任务类型
        TaskTypeEnum typeEnum = TaskTypeEnum.getByName(taskType);
        if (typeEnum == null) {
            log.warn("[processTaskTrigger] 未知任务类型: {}", taskType);
            return;
        }

        // 2. 自我互动检查（社交任务）
        if (isSocialTask(taskType) && isSelfInteraction(message)) {
            log.info("[processTaskTrigger] 忽略自我互动: taskType={}, userId={}", taskType, userId);
            return;
        }

        // 3. 获取任务配置
        TaskConfigDO config = taskConfigMapper.selectByTaskType(taskType);
        if (config == null || !config.getEnabled()) {
            log.warn("[processTaskTrigger] 任务未启用: {}", taskType);
            return;
        }

        // 4. 处理任务完成
        doCompleteTask(userId, config, message.getBizId());
    }

    /**
     * 执行任务完成逻辑
     */
    private AppTaskCompleteRespVO doCompleteTask(Long userId, TaskConfigDO config, Long bizId) {
        String taskType = config.getTaskType();

        // 1. 确定记录日期
        LocalDate recordDate = config.getResetCycle().equals(ResetCycleEnum.ONCE.getValue())
                ? ONCE_DATE
                : LocalDate.now(ZONE_ID);

        // 2. 获取或创建记录
        TaskRecordDO record = taskRecordMapper.selectByUserTaskDate(userId, taskType, recordDate);
        if (record == null) {
            record = TaskRecordDO.builder()
                    .userId(userId)
                    .taskType(taskType)
                    .completeDate(recordDate)
                    .completeCount(0)
                    .rewardStatus(RewardStatusEnum.PENDING.getValue())
                    .rewardPoints(0L)
                    .bizId(bizId)
                    .build();
            taskRecordMapper.insert(record);
        }

        // 3. 检查每日上限
        if (config.getDailyLimit() > 0 && record.getCompleteCount() >= config.getDailyLimit()) {
            log.info("[doCompleteTask] 已达每日上限: taskType={}, userId={}, count={}",
                    taskType, userId, record.getCompleteCount());

            AppTaskCompleteRespVO resp = new AppTaskCompleteRespVO();
            resp.setSuccess(false);
            resp.setRewardPoints(0L);
            resp.setMessage("今日该任务已完成");
            return resp;
        }

        // 4. 更新完成次数和待领取奖励
        record.setCompleteCount(record.getCompleteCount() + 1);
        record.setRewardPoints(record.getRewardPoints() + config.getRewardPoints());
        record.setRewardStatus(RewardStatusEnum.PENDING.getValue());
        if (bizId != null) {
            record.setBizId(bizId);
        }
        taskRecordMapper.updateById(record);

        log.info("[doCompleteTask] 任务完成: taskType={}, userId={}, count={}, pending={}",
                taskType, userId, record.getCompleteCount(), record.getRewardPoints());

        // 5. 返回结果
        AppTaskCompleteRespVO resp = new AppTaskCompleteRespVO();
        resp.setSuccess(true);
        resp.setRewardPoints(config.getRewardPoints());
        resp.setMessage("任务完成，请领取奖励");
        resp.setRecordId(record.getId());
        return resp;
    }

    @Override
    public AppInviteInfoRespVO getInviteInfo(Long userId, Integer pageNo, Integer pageSize) {
        // 1. 获取邀请码信息（通过 WalletUserApi）
        cn.iocoder.yudao.module.wallet.controller.app.vo.AppInviteCodeRespVO codeInfo = walletUserApi
                .getInviteCode(userId);

        // 2. 获取被邀请人列表（分页）
        cn.iocoder.yudao.framework.common.pojo.PageResult<cn.iocoder.yudao.module.wallet.dal.dataobject.WalletUserDO> inviteePage = walletUserApi
                .getInviteeList(userId, pageNo, pageSize);

        // 3. 获取今日可领取积分
        Long claimablePoints = getInviteClaimablePoints(userId);

        // 4. 组装响应
        AppInviteInfoRespVO respVO = new AppInviteInfoRespVO();
        respVO.setInviteCode(codeInfo.getInviteCode());
        respVO.setInviteUrl(codeInfo.getInviteUrl());
        respVO.setInviteCount(codeInfo.getInviteCount());
        respVO.setTodayClaimablePoints(claimablePoints);

        // 转换邀请人列表
        cn.iocoder.yudao.framework.common.pojo.PageResult<AppInviteInfoRespVO.InviteeVO> invitees = new cn.iocoder.yudao.framework.common.pojo.PageResult<>();
        invitees.setTotal(inviteePage.getTotal());
        invitees.setList(inviteePage.getList().stream().map(u -> {
            AppInviteInfoRespVO.InviteeVO vo = new AppInviteInfoRespVO.InviteeVO();
            vo.setUserId(u.getId());
            vo.setNickname(u.getNickname());
            vo.setAvatar(u.getAvatar());
            vo.setRegisterTime(u.getCreateTime());
            return vo;
        }).collect(Collectors.toList()));
        respVO.setInvitees(invitees);

        return respVO;
    }

    @Override
    public Long getInviteClaimablePoints(Long userId) {
        // 只统计邀请好友任务的待领取积分
        List<TaskRecordDO> pendingRecords = taskRecordMapper.selectPendingRewardsByUserIdAndTaskType(
                userId, TaskTypeEnum.INVITE_FRIEND.name());
        return pendingRecords.stream()
                .mapToLong(TaskRecordDO::getRewardPoints)
                .sum();
    }

    /**
     * 判断是否是社交任务
     */
    private boolean isSocialTask(String taskType) {
        return TaskTypeEnum.COMMENT.name().equals(taskType)
                || TaskTypeEnum.LIKE.name().equals(taskType)
                || TaskTypeEnum.POST.name().equals(taskType);
    }

    /**
     * 判断是否是自我互动
     */
    private boolean isSelfInteraction(TaskTriggerMessage message) {
        return message.getTargetUserId() != null
                && message.getTargetUserId().equals(message.getUserId());
    }

}
