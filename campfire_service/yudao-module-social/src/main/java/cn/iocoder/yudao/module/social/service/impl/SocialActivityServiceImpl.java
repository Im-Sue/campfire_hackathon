package cn.iocoder.yudao.module.social.service.impl;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.social.controller.app.vo.AppActivityRespVO;
import cn.iocoder.yudao.module.social.dal.dataobject.SocialActivityDO;
import cn.iocoder.yudao.module.social.dal.dataobject.SocialCommentDO;
import cn.iocoder.yudao.module.social.dal.dataobject.SocialPostDO;
import cn.iocoder.yudao.module.social.dal.mysql.SocialActivityMapper;
import cn.iocoder.yudao.module.social.dal.mysql.SocialCommentMapper;
import cn.iocoder.yudao.module.social.dal.mysql.SocialPostMapper;
import cn.iocoder.yudao.module.social.enums.ActivityTypeEnum;
import cn.iocoder.yudao.module.social.service.SocialActivityService;
import cn.iocoder.yudao.module.wallet.dal.dataobject.WalletUserDO;
import cn.iocoder.yudao.module.wallet.service.WalletUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 社交互动记录 Service 实现类
 */
@Service
@Validated
@Slf4j
public class SocialActivityServiceImpl implements SocialActivityService {

    /** 内容摘要最大长度 */
    private static final int CONTENT_SUMMARY_MAX_LENGTH = 50;

    @Resource
    private SocialActivityMapper socialActivityMapper;

    @Resource
    private SocialPostMapper socialPostMapper;

    @Resource
    private SocialCommentMapper socialCommentMapper;

    @Resource
    private WalletUserService walletUserService;

    @Override
    @Async
    public void createActivityAsync(Integer type, Long actorUserId, Long targetUserId, Long targetId) {
        try {
            SocialActivityDO activity = SocialActivityDO.builder()
                    .type(type)
                    .actorUserId(actorUserId)
                    .targetUserId(targetUserId)
                    .targetId(targetId)
                    .build();
            socialActivityMapper.insert(activity);
            log.debug("[createActivityAsync] 创建互动记录: type={}, actor={}, target={}",
                    type, actorUserId, targetUserId);
        } catch (Exception e) {
            log.error("[createActivityAsync] 创建互动记录失败", e);
        }
    }

    @Override
    public PageResult<AppActivityRespVO> getActivityList(Long userId, PageParam pageParam) {
        // 1. 分页查询互动记录
        PageResult<SocialActivityDO> pageResult = socialActivityMapper.selectPageByUserId(userId, pageParam);
        if (pageResult.getList().isEmpty()) {
            return new PageResult<>(Collections.emptyList(), pageResult.getTotal());
        }

        // 2. 收集需要查询的 ID
        Set<Long> userIds = new HashSet<>();
        Set<Long> postIds = new HashSet<>();
        Set<Long> commentIds = new HashSet<>();

        for (SocialActivityDO activity : pageResult.getList()) {
            userIds.add(activity.getActorUserId());
            if (activity.getTargetUserId() != null) {
                userIds.add(activity.getTargetUserId());
            }

            Integer type = activity.getType();
            Long targetId = activity.getTargetId();
            if (targetId != null) {
                if (Objects.equals(type, ActivityTypeEnum.LIKE_POST.getType())) {
                    // 点赞帖子
                    postIds.add(targetId);
                } else if (Objects.equals(type, ActivityTypeEnum.LIKE_COMMENT.getType())
                        || Objects.equals(type, ActivityTypeEnum.COMMENT.getType())
                        || Objects.equals(type, ActivityTypeEnum.REPLY.getType())) {
                    // 点赞评论/评论/回复
                    commentIds.add(targetId);
                }
            }
        }

        // 3. 批量查询关联数据
        Map<Long, WalletUserDO> userMap = getUserMap(userIds);
        Map<Long, SocialPostDO> postMap = getPostMap(postIds);
        Map<Long, SocialCommentDO> commentMap = getCommentMap(commentIds);

        // 4. 从评论中收集帖子 ID（用于跳转详情）
        for (SocialCommentDO comment : commentMap.values()) {
            if (comment.getPostId() != null) {
                postIds.add(comment.getPostId());
            }
        }
        // 再次查询帖子（可能有新的帖子 ID）
        if (!postIds.isEmpty()) {
            postMap = getPostMap(postIds);
        }

        // 5. 组装 VO
        List<AppActivityRespVO> voList = new ArrayList<>();
        for (SocialActivityDO activity : pageResult.getList()) {
            AppActivityRespVO vo = convertToVO(activity, userMap, postMap, commentMap);
            voList.add(vo);
        }

        return new PageResult<>(voList, pageResult.getTotal());
    }

    // ========== 私有方法 ==========

    /**
     * 批量获取用户信息
     */
    private Map<Long, WalletUserDO> getUserMap(Set<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userIds.stream()
                .map(walletUserService::getUser)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(WalletUserDO::getId, Function.identity()));
    }

    /**
     * 批量获取帖子信息
     */
    private Map<Long, SocialPostDO> getPostMap(Set<Long> postIds) {
        if (postIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<SocialPostDO> posts = socialPostMapper.selectByIds(postIds);
        return posts.stream().collect(Collectors.toMap(SocialPostDO::getId, Function.identity()));
    }

    /**
     * 批量获取评论信息
     */
    private Map<Long, SocialCommentDO> getCommentMap(Set<Long> commentIds) {
        if (commentIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<SocialCommentDO> comments = socialCommentMapper.selectByIds(commentIds);
        return comments.stream().collect(Collectors.toMap(SocialCommentDO::getId, Function.identity()));
    }

    /**
     * 将 DO 转换为增强版 VO
     */
    private AppActivityRespVO convertToVO(SocialActivityDO activity,
            Map<Long, WalletUserDO> userMap,
            Map<Long, SocialPostDO> postMap,
            Map<Long, SocialCommentDO> commentMap) {
        AppActivityRespVO vo = new AppActivityRespVO();

        // 基础信息
        vo.setId(activity.getId());
        vo.setType(activity.getType());
        vo.setTypeName(getTypeName(activity.getType()));
        vo.setCreateTime(activity.getCreateTime());

        // 发起者信息
        vo.setActorUserId(activity.getActorUserId());
        WalletUserDO actorUser = userMap.get(activity.getActorUserId());
        if (actorUser != null) {
            vo.setActorUserAddress(formatAddress(actorUser.getWalletAddress()));
            vo.setActorNickname(actorUser.getNickname());
            vo.setActorAvatar(actorUser.getAvatar());
        } else {
            vo.setActorNickname("已注销");
        }

        // 目标用户信息
        vo.setTargetUserId(activity.getTargetUserId());
        if (activity.getTargetUserId() != null) {
            WalletUserDO targetUser = userMap.get(activity.getTargetUserId());
            if (targetUser != null) {
                vo.setTargetUserAddress(formatAddress(targetUser.getWalletAddress()));
                vo.setTargetNickname(targetUser.getNickname());
                vo.setTargetAvatar(targetUser.getAvatar());
            } else {
                vo.setTargetNickname("已注销");
            }
        }

        // 目标内容信息（根据类型填充）
        vo.setTargetId(activity.getTargetId());
        fillContentInfo(vo, activity, postMap, commentMap);

        return vo;
    }

    /**
     * 填充内容信息
     */
    private void fillContentInfo(AppActivityRespVO vo,
            SocialActivityDO activity,
            Map<Long, SocialPostDO> postMap,
            Map<Long, SocialCommentDO> commentMap) {
        Integer type = activity.getType();
        Long targetId = activity.getTargetId();

        if (targetId == null) {
            return;
        }

        if (Objects.equals(type, ActivityTypeEnum.LIKE_POST.getType())) {
            // 点赞帖子
            SocialPostDO post = postMap.get(targetId);
            if (post != null) {
                vo.setPostId(post.getId());
                vo.setContentSummary(truncateContent(post.getContent()));
                vo.setFirstImage(getFirstImage(post.getImages()));
            } else {
                vo.setContentSummary("内容已删除");
            }
        } else if (Objects.equals(type, ActivityTypeEnum.LIKE_COMMENT.getType())
                || Objects.equals(type, ActivityTypeEnum.COMMENT.getType())
                || Objects.equals(type, ActivityTypeEnum.REPLY.getType())) {
            // 点赞评论/评论/回复
            SocialCommentDO comment = commentMap.get(targetId);
            if (comment != null) {
                vo.setPostId(comment.getPostId());
                vo.setContentSummary(truncateContent(comment.getContent()));
            } else {
                vo.setContentSummary("内容已删除");
            }
        }
        // 关注类型 (type=1) 不需要填充内容信息
    }

    /**
     * 获取类型名称
     */
    private String getTypeName(Integer type) {
        for (ActivityTypeEnum e : ActivityTypeEnum.values()) {
            if (Objects.equals(e.getType(), type)) {
                return e.getName();
            }
        }
        return "未知";
    }

    /**
     * 格式化钱包地址 (0x1234...5678)
     */
    private String formatAddress(String address) {
        if (address == null || address.length() < 10) {
            return address;
        }
        return address.substring(0, 6) + "..." + address.substring(address.length() - 4);
    }

    /**
     * 截断内容
     */
    private String truncateContent(String content) {
        if (content == null) {
            return null;
        }
        if (content.length() <= CONTENT_SUMMARY_MAX_LENGTH) {
            return content;
        }
        return content.substring(0, CONTENT_SUMMARY_MAX_LENGTH) + "...";
    }

    /**
     * 获取第一张图片
     */
    private String getFirstImage(List<String> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        return images.get(0);
    }

}
