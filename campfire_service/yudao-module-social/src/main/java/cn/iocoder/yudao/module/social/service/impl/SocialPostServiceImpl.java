package cn.iocoder.yudao.module.social.service.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.social.controller.admin.vo.PostAuditReqVO;
import cn.iocoder.yudao.module.social.controller.admin.vo.PostPageReqVO;
import cn.iocoder.yudao.module.social.controller.app.vo.AppPostCreateReqVO;
import cn.iocoder.yudao.module.social.controller.app.vo.AppPostPageReqVO;
import cn.iocoder.yudao.module.social.dal.dataobject.SocialPostDO;
import cn.iocoder.yudao.module.social.dal.mysql.SocialPostMapper;
import cn.iocoder.yudao.module.social.enums.ActivityTypeEnum;
import cn.iocoder.yudao.module.social.enums.LikeTargetTypeEnum;
import cn.iocoder.yudao.module.social.enums.PostStatusEnum;
import cn.iocoder.yudao.module.social.service.SocialActivityService;
import cn.iocoder.yudao.module.social.service.SocialLikeService;
import cn.iocoder.yudao.module.social.service.SocialPostService;
import cn.iocoder.yudao.module.social.service.SocialTopicService;
import cn.iocoder.yudao.module.social.service.helper.TaskTriggerHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.social.enums.ErrorCodeConstants.*;

/**
 * 社交帖子 Service 实现类
 */
@Service
@Validated
@Slf4j
public class SocialPostServiceImpl implements SocialPostService {

    @Resource
    private SocialPostMapper socialPostMapper;

    @Resource
    private SocialLikeService likeService;

    @Resource
    private SocialTopicService socialTopicService;

    @Resource
    private SocialActivityService socialActivityService;

    @Resource
    private TaskTriggerHelper taskTriggerHelper;

    @Override
    public SocialPostDO createPost(Long userId, AppPostCreateReqVO reqVO) {
        // 校验内容
        if (reqVO.getContent() == null || reqVO.getContent().trim().isEmpty()) {
            throw exception(POST_CONTENT_EMPTY);
        }
        // 校验图片数量
        if (reqVO.getImages() != null && reqVO.getImages().size() > 9) {
            throw exception(POST_IMAGES_EXCEED_LIMIT);
        }

        // 创建帖子
        SocialPostDO post = SocialPostDO.builder()
                .userId(userId)
                .content(reqVO.getContent())
                .images(reqVO.getImages())
                .likeCount(0)
                .commentCount(0)
                .viewCount(0)
                .status(PostStatusEnum.NORMAL.getStatus()) // 默认正常状态，不需要审核
                .build();
        socialPostMapper.insert(post);

        // 异步解析并创建话题
        createTopicsAsync(reqVO.getContent());

        // 异步发送任务触发消息（发帖任务）
        taskTriggerHelper.sendPostTask(userId, post.getId());

        return post;
    }

    @Async
    public void createTopicsAsync(String content) {
        try {
            socialTopicService.parseAndCreateTopics(content);
        } catch (Exception e) {
            log.error("[createTopicsAsync] 创建话题失败", e);
        }
    }

    @Override
    public SocialPostDO getPost(Long id) {
        SocialPostDO post = socialPostMapper.selectById(id);
        if (post != null) {
            // 异步更新浏览数和话题热度
            incrementViewCountAsync(id, post.getContent());
        }
        return post;
    }

    @Async
    public void incrementViewCountAsync(Long postId, String content) {
        try {
            // 1. 帖子浏览数+1
            socialPostMapper.incrementViewCount(postId);
            // 2. 帖子热度+1
            socialPostMapper.updateHeatScore(postId, 1);
            // 3. 话题热度+1
            socialTopicService.incrementHeatScoreAsync(content, 1);
        } catch (Exception e) {
            log.error("[incrementViewCountAsync] 更新浏览数失败", e);
        }
    }

    @Override
    public SocialPostDO validatePostExists(Long id) {
        SocialPostDO post = socialPostMapper.selectById(id);
        if (post == null) {
            throw exception(POST_NOT_EXISTS);
        }
        if (PostStatusEnum.DELETED.getStatus().equals(post.getStatus())) {
            throw exception(POST_DELETED);
        }
        return post;
    }

    @Override
    public PageResult<SocialPostDO> getPostPage(AppPostPageReqVO reqVO) {
        return socialPostMapper.selectPage(reqVO);
    }

    @Override
    public PageResult<SocialPostDO> getPostPage(PostPageReqVO reqVO) {
        return socialPostMapper.selectPage(reqVO);
    }

    @Override
    public void deletePost(Long userId, Long postId) {
        // 校验帖子存在
        SocialPostDO post = validatePostExists(postId);
        // 校验是否是自己的帖子
        if (!post.getUserId().equals(userId)) {
            throw exception(POST_NOT_YOURS);
        }
        // 标记删除
        socialPostMapper.updateById(SocialPostDO.builder()
                .id(postId)
                .status(PostStatusEnum.DELETED.getStatus())
                .build());
    }

    @Override
    public void deletePostByAdmin(Long postId) {
        // 校验帖子存在
        validatePostExists(postId);
        // 标记删除
        socialPostMapper.updateById(SocialPostDO.builder()
                .id(postId)
                .status(PostStatusEnum.DELETED.getStatus())
                .build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeletePostByAdmin(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        // 批量更新状态为已删除（跳过不存在的 ID）
        for (Long id : ids) {
            SocialPostDO post = socialPostMapper.selectById(id);
            if (post != null && !PostStatusEnum.DELETED.getStatus().equals(post.getStatus())) {
                socialPostMapper.updateById(SocialPostDO.builder()
                        .id(id)
                        .status(PostStatusEnum.DELETED.getStatus())
                        .build());
            }
        }
    }

    @Override
    public void auditPost(PostAuditReqVO reqVO) {
        // 校验帖子存在
        validatePostExists(reqVO.getId());
        // 更新状态
        socialPostMapper.updateById(SocialPostDO.builder()
                .id(reqVO.getId())
                .status(reqVO.getPass() ? PostStatusEnum.NORMAL.getStatus() : PostStatusEnum.REJECTED.getStatus())
                .build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likePost(Long userId, Long postId) {
        // 校验帖子存在
        SocialPostDO post = validatePostExists(postId);
        // 点赞（Toggle 模式，返回 true=点赞，false=取消）
        boolean liked = likeService.like(userId, LikeTargetTypeEnum.POST.getType(), postId);
        // 根据返回值更新点赞数
        socialPostMapper.updateLikeCount(postId, liked ? 1 : -1);
        // 点赞热度+3（取消点赞不扣分）
        if (liked) {
            socialPostMapper.updateHeatScore(postId, 3);
        }

        // 只有点赞时才触发异步任务
        if (liked) {
            // 异步：写互动记录 + 更新话题热度
            afterLikePostAsync(userId, post.getUserId(), postId, post.getContent());

            // 异步发送任务触发消息（点赞任务，targetUserId用于排除自己点赞自己）
            taskTriggerHelper.sendLikeTask(userId, postId, post.getUserId());
        }
    }

    @Async
    public void afterLikePostAsync(Long actorUserId, Long targetUserId, Long postId, String content) {
        try {
            // 1. 写互动记录
            socialActivityService.createActivityAsync(
                    ActivityTypeEnum.LIKE_POST.getType(),
                    actorUserId,
                    targetUserId,
                    postId);
            // 2. 话题热度+2
            socialTopicService.incrementHeatScoreAsync(content, 2);
        } catch (Exception e) {
            log.error("[afterLikePostAsync] 点赞后异步处理失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlikePost(Long userId, Long postId) {
        // 校验帖子存在
        validatePostExists(postId);
        // 取消点赞，只有真正删除了记录才更新点赞数
        boolean deleted = likeService.unlike(userId, LikeTargetTypeEnum.POST.getType(), postId);
        if (deleted) {
            socialPostMapper.updateLikeCount(postId, -1);
        }
    }

    @Override
    public boolean hasLikedPost(Long userId, Long postId) {
        return likeService.hasLiked(userId, LikeTargetTypeEnum.POST.getType(), postId);
    }

    @Override
    public void updateCommentCount(Long postId, int delta) {
        socialPostMapper.updateCommentCount(postId, delta);
        // 评论增加时热度+5（删除评论不扣分）
        if (delta > 0) {
            socialPostMapper.updateHeatScore(postId, 5);
        }
    }

    @Override
    public PageResult<SocialPostDO> getHotPostPage(AppPostPageReqVO reqVO) {
        return socialPostMapper.selectHotPage(reqVO);
    }

    @Override
    public Integer getPostCountByUserId(Long userId) {
        return socialPostMapper.selectCountByUserId(userId);
    }

}
