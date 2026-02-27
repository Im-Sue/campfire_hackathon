package cn.iocoder.yudao.module.social.service.impl;

import cn.iocoder.yudao.module.social.dal.dataobject.SocialLikeDO;
import cn.iocoder.yudao.module.social.dal.mysql.SocialLikeMapper;
import cn.iocoder.yudao.module.social.service.SocialLikeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

/**
 * 社交点赞 Service 实现类
 */
@Service
@Validated
@Slf4j
public class SocialLikeServiceImpl implements SocialLikeService {

    @Resource
    private SocialLikeMapper likeMapper;

    @Override
    public boolean like(Long userId, Integer targetType, Long targetId) {
        // 检查是否已点赞，如果已点赞则取消点赞（toggle 切换逻辑）
        SocialLikeDO existingLike = likeMapper.selectByUserAndTarget(userId, targetType, targetId);
        if (existingLike != null) {
            // 已点赞，执行取消点赞（使用物理删除，避免唯一键冲突）
            likeMapper.physicalDeleteById(existingLike.getId());
            log.debug("用户 {} 取消点赞目标 (type={}, id={})", userId, targetType, targetId);
            return false; // 取消点赞
        }

        // 未点赞，创建点赞记录
        SocialLikeDO like = SocialLikeDO.builder()
                .userId(userId)
                .targetType(targetType)
                .targetId(targetId)
                .build();
        try {
            likeMapper.insert(like);
            log.debug("用户 {} 点赞目标 (type={}, id={})", userId, targetType, targetId);
            return true; // 点赞成功
        } catch (DuplicateKeyException e) {
            // 并发情况下可能出现重复插入，此时忽略异常（视为点赞成功）
            log.warn("用户 {} 点赞目标 (type={}, id={}) 时发生并发冲突，已忽略", userId, targetType, targetId);
            return true; // 并发冲突视为点赞
        }
    }

    @Override
    public boolean unlike(Long userId, Integer targetType, Long targetId) {
        // 直接物理删除点赞记录，返回是否真正删除了记录
        int deleted = likeMapper.physicalDeleteByUserAndTarget(userId, targetType, targetId);
        return deleted > 0;
    }

    @Override
    public boolean hasLiked(Long userId, Integer targetType, Long targetId) {
        return likeMapper.existsByUserAndTarget(userId, targetType, targetId);
    }

}
