package cn.iocoder.yudao.module.social.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.social.dal.dataobject.SocialFollowDO;

import java.util.List;

/**
 * 社交关注 Service 接口
 */
public interface SocialFollowService {

    /**
     * 关注用户
     *
     * @param userId       当前用户 ID
     * @param followUserId 被关注用户 ID
     */
    void follow(Long userId, Long followUserId);

    /**
     * 取消关注
     *
     * @param userId       当前用户 ID
     * @param followUserId 被关注用户 ID
     */
    void unfollow(Long userId, Long followUserId);

    /**
     * 检查是否已关注
     *
     * @param userId       当前用户 ID
     * @param followUserId 被关注用户 ID
     * @return 是否已关注
     */
    boolean hasFollowed(Long userId, Long followUserId);

    /**
     * 获取关注列表
     *
     * @param userId 用户 ID
     * @return 关注列表
     */
    List<SocialFollowDO> getFollowingList(Long userId);

    /**
     * 获取粉丝列表
     *
     * @param userId 用户 ID
     * @return 粉丝列表
     */
    List<SocialFollowDO> getFollowersList(Long userId);

    /**
     * 获取关注/粉丝数量
     *
     * @param userId 用户 ID
     * @return [关注数, 粉丝数]
     */
    Long[] getFollowCount(Long userId);

}
