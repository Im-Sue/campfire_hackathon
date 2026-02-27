package cn.iocoder.yudao.module.market.dal.mysql.comment;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.market.controller.admin.comment.vo.EventCommentPageReqVO;
import cn.iocoder.yudao.module.market.controller.app.comment.vo.AppEventCommentPageReqVO;
import cn.iocoder.yudao.module.market.controller.app.comment.vo.AppEventCommentReplyPageReqVO;
import cn.iocoder.yudao.module.market.dal.dataobject.comment.PmEventCommentDO;
import cn.iocoder.yudao.module.market.enums.CommentStatusEnum;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 事件评论 Mapper
 */
@Mapper
public interface PmEventCommentMapper extends BaseMapperX<PmEventCommentDO> {

    /**
     * 查询一级评论列表（分页）
     */
    default PageResult<PmEventCommentDO> selectPage(AppEventCommentPageReqVO reqVO) {
        LambdaQueryWrapperX<PmEventCommentDO> wrapper = new LambdaQueryWrapperX<PmEventCommentDO>()
                .eq(PmEventCommentDO::getEventId, reqVO.getEventId())
                .eq(PmEventCommentDO::getParentId, 0L) // 只查一级评论
                .eq(PmEventCommentDO::getStatus, CommentStatusEnum.NORMAL.getStatus());

        // 排序：热度（点赞数倒序）或时间（创建时间倒序）
        if ("time".equals(reqVO.getOrderBy())) {
            wrapper.orderByDesc(PmEventCommentDO::getCreateTime);
        } else {
            // 默认按热度排序
            wrapper.orderByDesc(PmEventCommentDO::getLikeCount)
                    .orderByDesc(PmEventCommentDO::getCreateTime);
        }

        return selectPage(reqVO, wrapper);
    }

    /**
     * 查询回复列表（分页）
     */
    default PageResult<PmEventCommentDO> selectReplyPage(AppEventCommentReplyPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PmEventCommentDO>()
                .eq(PmEventCommentDO::getParentId, reqVO.getParentId())
                .eq(PmEventCommentDO::getStatus, CommentStatusEnum.NORMAL.getStatus())
                .orderByAsc(PmEventCommentDO::getCreateTime)); // 回复按时间正序
    }

    /**
     * 管理端查询评论列表（分页）
     */
    default PageResult<PmEventCommentDO> selectPage(EventCommentPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PmEventCommentDO>()
                .eqIfPresent(PmEventCommentDO::getEventId, reqVO.getEventId())
                .eqIfPresent(PmEventCommentDO::getUserId, reqVO.getUserId())
                .eqIfPresent(PmEventCommentDO::getStatus, reqVO.getStatus())
                .likeIfPresent(PmEventCommentDO::getContent, reqVO.getContent())
                .orderByDesc(PmEventCommentDO::getCreateTime));
    }

    /**
     * 更新点赞数
     */
    @Update("UPDATE pm_event_comment SET like_count = like_count + #{delta} WHERE id = #{id}")
    void updateLikeCount(@Param("id") Long id, @Param("delta") int delta);

    /**
     * 更新回复数
     */
    @Update("UPDATE pm_event_comment SET reply_count = reply_count + #{delta} WHERE id = #{id}")
    void updateReplyCount(@Param("id") Long id, @Param("delta") int delta);

    /**
     * 查询某一级评论下的回复数量
     */
    default Integer selectReplyCountByParentId(Long parentId) {
        return Math.toIntExact(selectCount(new LambdaQueryWrapperX<PmEventCommentDO>()
                .eq(PmEventCommentDO::getParentId, parentId)
                .eq(PmEventCommentDO::getStatus, CommentStatusEnum.NORMAL.getStatus())));
    }

    /**
     * 获取某一级评论下的前N条回复（用于预览）
     */
    default java.util.List<PmEventCommentDO> selectTopReplies(Long parentId, int limit) {
        return selectList(new LambdaQueryWrapperX<PmEventCommentDO>()
                .eq(PmEventCommentDO::getParentId, parentId)
                .eq(PmEventCommentDO::getStatus, CommentStatusEnum.NORMAL.getStatus())
                .orderByAsc(PmEventCommentDO::getCreateTime)
                .last("LIMIT " + limit));
    }

    /**
     * 查询某事件下的正常评论总数
     */
    default Integer selectCountByEventId(Long eventId) {
        return Math.toIntExact(selectCount(new LambdaQueryWrapperX<PmEventCommentDO>()
                .eq(PmEventCommentDO::getEventId, eventId)
                .eq(PmEventCommentDO::getStatus, CommentStatusEnum.NORMAL.getStatus())));
    }

}
