package cn.iocoder.yudao.module.task.service;

import cn.iocoder.yudao.module.task.controller.app.vo.AppInviteInfoRespVO;
import cn.iocoder.yudao.module.task.controller.app.vo.AppTaskCompleteRespVO;
import cn.iocoder.yudao.module.task.controller.app.vo.AppTaskItemRespVO;
import cn.iocoder.yudao.framework.mq.biz.message.TaskTriggerMessage;

import java.util.List;

/**
 * 任务 Service 接口
 */
public interface TaskService {

    // ========== C端接口 ==========

    /**
     * 获取任务列表（含用户完成状态）
     *
     * @param userId   用户ID
     * @param category 任务分类：new=新手任务(一次性), daily=日常任务, null=全部
     * @return 任务列表
     */
    List<AppTaskItemRespVO> getTaskList(Long userId, String category);

    /**
     * 完成点击类任务
     *
     * @param userId   用户ID
     * @param taskType 任务类型
     * @return 完成结果
     */
    AppTaskCompleteRespVO completeClickTask(Long userId, String taskType);

    /**
     * 每日签到
     *
     * @param userId 用户ID
     * @return 完成结果
     */
    AppTaskCompleteRespVO signIn(Long userId);

    /**
     * 领取单个任务奖励
     *
     * @param userId   用户ID
     * @param recordId 任务记录ID
     * @return 领取的积分
     */
    Long claimReward(Long userId, Long recordId);

    /**
     * 一键领取所有奖励
     *
     * @param userId 用户ID
     * @return 领取的总积分
     */
    Long claimAllRewards(Long userId);

    /**
     * 获取邀请信息（含邀请列表和可领取积分）
     *
     * @param userId   用户ID
     * @param pageNo   页码
     * @param pageSize 每页条数
     * @return 邀请信息
     */
    AppInviteInfoRespVO getInviteInfo(Long userId, Integer pageNo, Integer pageSize);

    /**
     * 获取用户邀请任务可领取的积分（包含历史未领取）
     *
     * @param userId 用户ID
     * @return 可领取积分数
     */
    Long getInviteClaimablePoints(Long userId);

    // ========== 消息触发 ==========

    /**
     * 处理任务触发消息
     *
     * @param message 消息
     */
    void processTaskTrigger(TaskTriggerMessage message);

}
