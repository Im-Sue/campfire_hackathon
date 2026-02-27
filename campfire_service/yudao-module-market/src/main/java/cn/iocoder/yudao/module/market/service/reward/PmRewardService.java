package cn.iocoder.yudao.module.market.service.reward;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.market.controller.admin.reward.vo.RewardPageReqVO;
import cn.iocoder.yudao.module.market.controller.admin.reward.vo.RewardStatisticsVO;
import cn.iocoder.yudao.module.market.dal.dataobject.reward.PmRewardDO;

import java.util.List;

/**
 * 用户奖励 Service 接口
 */
public interface PmRewardService {

    /**
     * 创建奖励记录
     *
     * @param reward 奖励记录
     * @return 奖励记录编号
     */
    Long createReward(PmRewardDO reward);

    /**
     * 批量创建奖励记录
     *
     * @param rewards 奖励记录列表
     */
    void createRewardBatch(List<PmRewardDO> rewards);

    /**
     * 获取奖励记录
     *
     * @param id 奖励记录编号
     * @return 奖励记录
     */
    PmRewardDO getReward(Long id);

    /**
     * 根据持仓 ID 获取奖励记录
     *
     * @param positionId 持仓 ID
     * @return 奖励记录
     */
    PmRewardDO getRewardByPositionId(Long positionId);

    /**
     * 获取用户的奖励列表
     *
     * @param userId 用户 ID
     * @return 奖励列表
     */
    List<PmRewardDO> getRewardsByUserId(Long userId);

    /**
     * 分页获取用户奖励
     *
     * @param userId    用户 ID
     * @param pageParam 分页参数
     * @return 分页结果
     */
    PageResult<PmRewardDO> getRewardPageByUserId(Long userId, PageParam pageParam);

    /**
     * 获取用户待领取的奖励
     *
     * @param userId 用户 ID
     * @return 待领取奖励列表
     */
    List<PmRewardDO> getPendingRewardsByUserId(Long userId);

    /**
     * 领取奖励
     *
     * @param userId   用户 ID
     * @param rewardId 奖励记录编号
     */
    void claimReward(Long userId, Long rewardId);

    /**
     * 批量领取奖励
     *
     * @param userId    用户 ID
     * @param rewardIds 奖励记录编号列表
     */
    void claimRewardBatch(Long userId, List<Long> rewardIds);

    /**
     * 获取用户待领取奖励总额
     *
     * @param userId 用户 ID
     * @return 待领取奖励总额
     */
    Long getPendingRewardAmount(Long userId);

    // ========== 管理端 ==========

    /**
     * 管理端分页获取奖励列表
     *
     * @param pageReqVO 分页请求
     * @return 分页结果
     */
    PageResult<PmRewardDO> getRewardPage(RewardPageReqVO pageReqVO);

    /**
     * 获取奖励统计
     *
     * @return 统计信息
     */
    RewardStatisticsVO getStatistics();

}
