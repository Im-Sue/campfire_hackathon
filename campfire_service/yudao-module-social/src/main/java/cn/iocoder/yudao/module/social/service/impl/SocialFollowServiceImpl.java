package cn.iocoder.yudao.module.social.service.impl;

import cn.iocoder.yudao.module.social.dal.dataobject.SocialFollowDO;
import cn.iocoder.yudao.module.social.dal.mysql.SocialFollowMapper;
import cn.iocoder.yudao.module.social.enums.ActivityTypeEnum;
import cn.iocoder.yudao.module.social.service.SocialActivityService;
import cn.iocoder.yudao.module.social.service.SocialFollowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.social.enums.ErrorCodeConstants.*;

/**
 * 社交关注 Service 实现类
 */
@Service
@Validated
@Slf4j
public class SocialFollowServiceImpl implements SocialFollowService {

    @Resource
    private SocialFollowMapper socialFollowMapper;

    @Resource
    private SocialActivityService socialActivityService;

    @Override
    public void follow(Long userId, Long followUserId) {
        // 1. 校验：不能关注自己
        if (userId.equals(followUserId)) {
            throw exception(FOLLOW_SELF_NOT_ALLOWED);
        }

        // 2. 幂等检查：如果已关注，直接返回成功
        SocialFollowDO exist = socialFollowMapper.selectByUserAndFollowUser(userId, followUserId);
        if (exist != null) {
            log.debug("用户 {} 已关注用户 {}，无需重复关注", userId, followUserId);
            return;
        }

        // 3. 未关注，插入关注记录
        SocialFollowDO follow = SocialFollowDO.builder()
                .userId(userId)
                .followUserId(followUserId)
                .build();
        try {
            socialFollowMapper.insert(follow);
            log.debug("用户 {} 关注用户 {}", userId, followUserId);

            // 4. 异步写入互动记录
            socialActivityService.createActivityAsync(
                    ActivityTypeEnum.FOLLOW.getType(),
                    userId,
                    followUserId,
                    null);
        } catch (DuplicateKeyException e) {
            // 并发情况下可能出现重复插入，此时忽略异常（视为关注成功）
            log.warn("用户 {} 关注用户 {} 时发生并发冲突，已忽略", userId, followUserId);
        }
    }

    @Override
    public void unfollow(Long userId, Long followUserId) {
        // 1. 校验：已关注
        SocialFollowDO exist = socialFollowMapper.selectByUserAndFollowUser(userId, followUserId);
        if (exist == null) {
            throw exception(FOLLOW_NOT_EXISTS);
        }

        // 2. 物理删除关注记录（避免唯一键冲突）
        socialFollowMapper.physicalDeleteById(exist.getId());
    }

    @Override
    public boolean hasFollowed(Long userId, Long followUserId) {
        return socialFollowMapper.selectByUserAndFollowUser(userId, followUserId) != null;
    }

    @Override
    public List<SocialFollowDO> getFollowingList(Long userId) {
        return socialFollowMapper.selectFollowingList(userId);
    }

    @Override
    public List<SocialFollowDO> getFollowersList(Long userId) {
        return socialFollowMapper.selectFollowersList(userId);
    }

    @Override
    public Long[] getFollowCount(Long userId) {
        Long following = socialFollowMapper.selectFollowingCount(userId);
        Long followers = socialFollowMapper.selectFollowersCount(userId);
        return new Long[] { following, followers };
    }

}
