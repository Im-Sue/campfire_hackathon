package cn.iocoder.yudao.module.market.service.comment;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.market.controller.admin.comment.vo.EventCommentPageReqVO;
import cn.iocoder.yudao.module.market.controller.app.comment.vo.AppEventCommentCreateReqVO;
import cn.iocoder.yudao.module.market.controller.app.comment.vo.AppEventCommentDeleteRespVO;
import cn.iocoder.yudao.module.market.controller.app.comment.vo.AppEventCommentPageReqVO;
import cn.iocoder.yudao.module.market.controller.app.comment.vo.AppEventCommentReplyPageReqVO;
import cn.iocoder.yudao.module.market.dal.dataobject.comment.PmEventCommentDO;
import cn.iocoder.yudao.module.market.dal.dataobject.comment.PmEventCommentLikeDO;
import cn.iocoder.yudao.module.market.dal.mysql.comment.PmEventCommentLikeMapper;
import cn.iocoder.yudao.module.market.dal.mysql.comment.PmEventCommentMapper;
import cn.iocoder.yudao.module.market.enums.CommentStatusEnum;
import cn.iocoder.yudao.module.market.service.event.PmEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.market.enums.ErrorCodeConstants.*;

/**
 * 事件评论 Service 实现类
 */
@Service
@Validated
@Slf4j
public class EventCommentServiceImpl implements EventCommentService {

    @Resource
    private PmEventCommentMapper commentMapper;

    @Resource
    private PmEventCommentLikeMapper likeMapper;

    @Resource
    private PmEventService eventService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PmEventCommentDO createComment(Long userId, AppEventCommentCreateReqVO reqVO) {
        // 1. 校验评论内容
        if (reqVO.getContent() == null || reqVO.getContent().trim().isEmpty()) {
            throw exception(EVENT_COMMENT_CONTENT_EMPTY);
        }
        if (reqVO.getContent().length() > 500) {
            throw exception(EVENT_COMMENT_CONTENT_EXCEED_LIMIT);
        }

        // 2. 校验事件存在且已上架
        eventService.validateEventPublished(reqVO.getEventId());

        // 3. 处理回复逻辑
        Long actualParentId = 0L;
        Long replyCommentId = null;
        Long replyUserId = null;

        if (reqVO.getParentId() != null && reqVO.getParentId() > 0) {
            PmEventCommentDO targetComment = validateCommentExists(reqVO.getParentId());
            replyUserId = targetComment.getUserId();
            replyCommentId = reqVO.getParentId();

            // parentId 始终指向一级评论
            if (targetComment.getParentId() == 0L) {
                // 回复的是一级评论
                actualParentId = targetComment.getId();
            } else {
                // 回复的是子评论，parentId 使用被回复评论的 parentId（即一级评论）
                actualParentId = targetComment.getParentId();
            }

            // 更新一级评论的回复数
            commentMapper.updateReplyCount(actualParentId, 1);
        }

        // 4. 创建评论
        PmEventCommentDO comment = PmEventCommentDO.builder()
                .eventId(reqVO.getEventId())
                .userId(userId)
                .parentId(actualParentId)
                .replyCommentId(replyCommentId)
                .replyUserId(replyUserId)
                .content(reqVO.getContent())
                .likeCount(0)
                .replyCount(0)
                .status(CommentStatusEnum.NORMAL.getStatus())
                .build();
        commentMapper.insert(comment);

        return comment;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppEventCommentDeleteRespVO deleteComment(Long userId, Long commentId) {
        // 1. 校验评论存在
        PmEventCommentDO comment = validateCommentExists(commentId);

        // 2. 校验是否是自己的评论
        if (!comment.getUserId().equals(userId)) {
            throw exception(EVENT_COMMENT_NOT_YOURS);
        }

        // 3. 执行删除
        doDeleteComment(comment);

        // 4. 返回删除后的数量信息
        AppEventCommentDeleteRespVO respVO = new AppEventCommentDeleteRespVO();
        if (comment.getParentId() != null && comment.getParentId() > 0) {
            // 删除的是回复，返回父评论的回复数
            PmEventCommentDO parentComment = commentMapper.selectById(comment.getParentId());
            respVO.setReplyCount(parentComment != null ? parentComment.getReplyCount() : 0);
        } else {
            // 删除的是一级评论，返回事件的总评论数
            Integer totalCount = commentMapper.selectCountByEventId(comment.getEventId());
            respVO.setTotalCommentCount(totalCount != null ? totalCount : 0);
        }
        return respVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCommentByAdmin(Long commentId) {
        // 1. 校验评论存在
        PmEventCommentDO comment = validateCommentExists(commentId);

        // 2. 执行删除
        doDeleteComment(comment);
    }

    private void doDeleteComment(PmEventCommentDO comment) {
        // 标记删除
        commentMapper.updateById(PmEventCommentDO.builder()
                .id(comment.getId())
                .status(CommentStatusEnum.DELETED.getStatus())
                .build());

        // 如果是回复，更新一级评论的回复数
        if (comment.getParentId() != null && comment.getParentId() > 0) {
            commentMapper.updateReplyCount(comment.getParentId(), -1);
        }
    }

    @Override
    public PmEventCommentDO getComment(Long id) {
        return commentMapper.selectById(id);
    }

    @Override
    public PmEventCommentDO validateCommentExists(Long id) {
        PmEventCommentDO comment = commentMapper.selectById(id);
        if (comment == null) {
            throw exception(EVENT_COMMENT_NOT_EXISTS);
        }
        if (CommentStatusEnum.DELETED.getStatus().equals(comment.getStatus())) {
            throw exception(EVENT_COMMENT_DELETED);
        }
        return comment;
    }

    @Override
    public PageResult<PmEventCommentDO> getCommentPage(AppEventCommentPageReqVO reqVO) {
        return commentMapper.selectPage(reqVO);
    }

    @Override
    public PageResult<PmEventCommentDO> getReplyPage(AppEventCommentReplyPageReqVO reqVO) {
        return commentMapper.selectReplyPage(reqVO);
    }

    @Override
    public PageResult<PmEventCommentDO> getCommentPage(EventCommentPageReqVO reqVO) {
        return commentMapper.selectPage(reqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean likeComment(Long userId, Long commentId) {
        // 1. 校验评论存在
        validateCommentExists(commentId);

        // 2. 检查是否已点赞
        PmEventCommentLikeDO existingLike = likeMapper.selectByUserAndComment(userId, commentId);

        if (existingLike != null) {
            // 已点赞 → 取消点赞
            likeMapper.deleteById(existingLike.getId());
            commentMapper.updateLikeCount(commentId, -1);
            return false; // 取消点赞
        } else {
            // 未点赞 → 点赞
            PmEventCommentLikeDO like = PmEventCommentLikeDO.builder()
                    .userId(userId)
                    .commentId(commentId)
                    .build();
            likeMapper.insert(like);
            commentMapper.updateLikeCount(commentId, 1);
            return true; // 点赞成功
        }
    }

    @Override
    public boolean hasLiked(Long userId, Long commentId) {
        return likeMapper.selectByUserAndComment(userId, commentId) != null;
    }

    @Override
    public Set<Long> getLikedCommentIds(Long userId, List<Long> commentIds) {
        if (userId == null || commentIds == null || commentIds.isEmpty()) {
            return Collections.emptySet();
        }
        List<PmEventCommentLikeDO> likes = likeMapper.selectList(new LambdaQueryWrapperX<PmEventCommentLikeDO>()
                .eq(PmEventCommentLikeDO::getUserId, userId)
                .in(PmEventCommentLikeDO::getCommentId, commentIds));
        return likes.stream()
                .map(PmEventCommentLikeDO::getCommentId)
                .collect(Collectors.toSet());
    }

}
