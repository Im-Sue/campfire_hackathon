package cn.iocoder.yudao.module.social.service.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.social.controller.admin.vo.CommentPageReqVO;
import cn.iocoder.yudao.module.social.controller.app.vo.AppCommentCreateReqVO;
import cn.iocoder.yudao.module.social.controller.app.vo.AppCommentDeleteRespVO;
import cn.iocoder.yudao.module.social.controller.app.vo.AppCommentPageReqVO;
import cn.iocoder.yudao.module.social.controller.app.vo.AppReplyPageReqVO;
import cn.iocoder.yudao.module.social.dal.dataobject.SocialCommentDO;
import cn.iocoder.yudao.module.social.dal.dataobject.SocialPostDO;
import cn.iocoder.yudao.module.social.dal.mysql.SocialCommentMapper;
import cn.iocoder.yudao.module.social.enums.ActivityTypeEnum;
import cn.iocoder.yudao.module.social.enums.LikeTargetTypeEnum;
import cn.iocoder.yudao.module.social.enums.PostStatusEnum;
import cn.iocoder.yudao.module.social.service.SocialActivityService;
import cn.iocoder.yudao.module.social.service.SocialCommentService;
import cn.iocoder.yudao.module.social.service.SocialLikeService;
import cn.iocoder.yudao.module.social.service.SocialPostService;
import cn.iocoder.yudao.module.social.service.SocialTopicService;
import cn.iocoder.yudao.module.social.service.helper.TaskTriggerHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.social.enums.ErrorCodeConstants.*;

/**
 * 社交评论 Service 实现类
 */
@Service
@Validated
@Slf4j
public class SocialCommentServiceImpl implements SocialCommentService {

    @Resource
    private SocialCommentMapper commentMapper;

    @Resource
    private SocialLikeService likeService;

    @Resource
    @Lazy // 避免循环依赖
    private SocialPostService postService;

    @Resource
    private SocialActivityService socialActivityService;

    @Resource
    private SocialTopicService socialTopicService;

    @Resource
    private TaskTriggerHelper taskTriggerHelper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SocialCommentDO createComment(Long userId, AppCommentCreateReqVO reqVO) {
        // 校验内容
        if (reqVO.getContent() == null || reqVO.getContent().trim().isEmpty()) {
            throw exception(COMMENT_CONTENT_EMPTY);
        }
        if (reqVO.getContent().length() > 500) {
            throw exception(COMMENT_CONTENT_EXCEED_LIMIT);
        }

        // 校验帖子存在
        SocialPostDO post = postService.validatePostExists(reqVO.getPostId());

        // 处理回复逻辑
        Long actualParentId = 0L;
        Long replyCommentId = null;
        Long replyUserId = null;

        if (reqVO.getParentId() != null && reqVO.getParentId() > 0) {
            SocialCommentDO targetComment = validateCommentExists(reqVO.getParentId());
            replyUserId = targetComment.getUserId();
            replyCommentId = reqVO.getParentId();

            // parentId 始终指向一级评论
            if (targetComment.getParentId() == 0L) {
                // 回复的是一级评论
                actualParentId = targetComment.getId();
            } else {
                // 回复的是子评论，parentId 用被回复评论的 parentId（即根评论）
                actualParentId = targetComment.getParentId();
            }
        }

        // 创建评论
        SocialCommentDO comment = SocialCommentDO.builder()
                .postId(reqVO.getPostId())
                .userId(userId)
                .parentId(actualParentId)
                .replyCommentId(replyCommentId)
                .replyUserId(replyUserId)
                .content(reqVO.getContent())
                .likeCount(0)
                .status(PostStatusEnum.NORMAL.getStatus())
                .build();
        commentMapper.insert(comment);

        // 更新帖子评论数
        postService.updateCommentCount(reqVO.getPostId(), 1);

        // 异步：写互动记录 + 更新话题热度
        afterCreateCommentAsync(userId, post.getUserId(), comment.getId(), reqVO.getPostId(), post.getContent(),
                replyUserId);

        // 异步发送任务触发消息（评论任务，targetUserId用于排除自己评论自己）
        Long targetUserId = (replyUserId != null) ? replyUserId : post.getUserId();
        taskTriggerHelper.sendCommentTask(userId, comment.getId(), targetUserId);

        return comment;
    }

    @Async
    public void afterCreateCommentAsync(Long actorUserId, Long postAuthorId, Long commentId, Long postId,
            String postContent,
            Long replyUserId) {
        try {
            // 1. 写互动记录（targetId 存评论 ID，用于查询时获取评论内容）
            Integer type = (replyUserId != null) ? ActivityTypeEnum.REPLY.getType()
                    : ActivityTypeEnum.COMMENT.getType();
            Long targetUserId = (replyUserId != null) ? replyUserId : postAuthorId;
            socialActivityService.createActivityAsync(type, actorUserId, targetUserId, commentId);

            // 2. 话题热度+3
            socialTopicService.incrementHeatScoreAsync(postContent, 3);
        } catch (Exception e) {
            log.error("[afterCreateCommentAsync] 评论后异步处理失败", e);
        }
    }

    @Override
    public SocialCommentDO getComment(Long id) {
        return commentMapper.selectById(id);
    }

    @Override
    public SocialCommentDO validateCommentExists(Long id) {
        SocialCommentDO comment = commentMapper.selectById(id);
        if (comment == null) {
            throw exception(COMMENT_NOT_EXISTS);
        }
        if (PostStatusEnum.DELETED.getStatus().equals(comment.getStatus())) {
            throw exception(COMMENT_DELETED);
        }
        return comment;
    }

    @Override
    public PageResult<SocialCommentDO> getCommentPage(AppCommentPageReqVO reqVO) {
        return commentMapper.selectPage(reqVO);
    }

    @Override
    public PageResult<SocialCommentDO> getReplyPage(AppReplyPageReqVO reqVO) {
        return commentMapper.selectReplyPage(reqVO);
    }

    @Override
    public PageResult<SocialCommentDO> getCommentPage(CommentPageReqVO reqVO) {
        return commentMapper.selectPage(reqVO);
    }

    @Override
    public List<SocialCommentDO> getCommentListByPostId(Long postId) {
        return commentMapper.selectListByPostId(postId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppCommentDeleteRespVO deleteComment(Long userId, Long commentId) {
        // 校验评论存在
        SocialCommentDO comment = validateCommentExists(commentId);
        // 校验是否是自己的评论
        if (!comment.getUserId().equals(userId)) {
            throw exception(COMMENT_NOT_YOURS);
        }
        // 标记删除
        commentMapper.updateById(SocialCommentDO.builder()
                .id(commentId)
                .status(PostStatusEnum.DELETED.getStatus())
                .build());
        // 更新帖子评论数
        postService.updateCommentCount(comment.getPostId(), -1);

        // 返回删除后的数量信息
        AppCommentDeleteRespVO respVO = new AppCommentDeleteRespVO();
        // 获取帖子最新评论数
        SocialPostDO post = postService.getPost(comment.getPostId());
        respVO.setCommentCount(post != null ? post.getCommentCount() : 0);
        // 获取帖子的回复总数（不含一级评论）
        Integer postReplyCount = commentMapper.selectPostReplyCount(comment.getPostId());
        respVO.setPostReplyCount(postReplyCount != null ? postReplyCount : 0);
        // 如果是回复，返回父评论的回复数
        if (comment.getParentId() != null && comment.getParentId() > 0) {
            Integer replyCount = commentMapper.selectReplyCountByParentId(comment.getParentId());
            respVO.setReplyCount(replyCount != null ? replyCount : 0);
        }
        return respVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCommentByAdmin(Long commentId) {
        // 校验评论存在
        SocialCommentDO comment = validateCommentExists(commentId);
        // 标记删除
        commentMapper.updateById(SocialCommentDO.builder()
                .id(commentId)
                .status(PostStatusEnum.DELETED.getStatus())
                .build());
        // 更新帖子评论数
        postService.updateCommentCount(comment.getPostId(), -1);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteCommentByAdmin(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        // 批量删除评论（跳过不存在或已删除的）
        for (Long id : ids) {
            SocialCommentDO comment = commentMapper.selectById(id);
            if (comment != null && !PostStatusEnum.DELETED.getStatus().equals(comment.getStatus())) {
                commentMapper.updateById(SocialCommentDO.builder()
                        .id(id)
                        .status(PostStatusEnum.DELETED.getStatus())
                        .build());
                // 更新帖子评论数
                postService.updateCommentCount(comment.getPostId(), -1);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likeComment(Long userId, Long commentId) {
        // 校验评论存在
        SocialCommentDO comment = validateCommentExists(commentId);
        // 点赞（Toggle 模式，返回 true=点赞，false=取消）
        boolean liked = likeService.like(userId, LikeTargetTypeEnum.COMMENT.getType(), commentId);
        // 根据返回值更新点赞数
        commentMapper.updateLikeCount(commentId, liked ? 1 : -1);

        // 只有点赞时才触发异步任务
        if (liked) {
            // 异步：写互动记录
            afterLikeCommentAsync(userId, comment.getUserId(), commentId);
        }
    }

    @Async
    public void afterLikeCommentAsync(Long actorUserId, Long commentAuthorId, Long commentId) {
        try {
            socialActivityService.createActivityAsync(
                    ActivityTypeEnum.LIKE_COMMENT.getType(),
                    actorUserId,
                    commentAuthorId,
                    commentId);
        } catch (Exception e) {
            log.error("[afterLikeCommentAsync] 点赞评论后异步处理失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlikeComment(Long userId, Long commentId) {
        // 校验评论存在
        validateCommentExists(commentId);
        // 取消点赞，只有真正删除了记录才更新点赞数
        boolean deleted = likeService.unlike(userId, LikeTargetTypeEnum.COMMENT.getType(), commentId);
        if (deleted) {
            commentMapper.updateLikeCount(commentId, -1);
        }
    }

    @Override
    public boolean hasLikedComment(Long userId, Long commentId) {
        return likeService.hasLiked(userId, LikeTargetTypeEnum.COMMENT.getType(), commentId);
    }

}
