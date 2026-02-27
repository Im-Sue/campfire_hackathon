package cn.iocoder.yudao.module.social.dal.mysql;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.social.controller.admin.vo.CommentPageReqVO;
import cn.iocoder.yudao.module.social.controller.app.vo.AppCommentPageReqVO;
import cn.iocoder.yudao.module.social.controller.app.vo.AppReplyPageReqVO;
import cn.iocoder.yudao.module.social.dal.dataobject.SocialCommentDO;
import cn.iocoder.yudao.module.social.enums.PostStatusEnum;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 社交评论 Mapper
 */
@Mapper
public interface SocialCommentMapper extends BaseMapperX<SocialCommentDO> {

    /**
     * 分页查询帖子一级评论 (用户端)
     * 只查询 parentId = 0 的一级评论
     */
    default PageResult<SocialCommentDO> selectPage(AppCommentPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SocialCommentDO>()
                .eq(SocialCommentDO::getPostId, reqVO.getPostId())
                .eq(SocialCommentDO::getParentId, 0L) // 只查一级评论
                .eq(SocialCommentDO::getStatus, PostStatusEnum.NORMAL.getStatus())
                .orderByDesc(SocialCommentDO::getCreateTime));
    }

    /**
     * 分页查询评论的回复 (用户端)
     */
    default PageResult<SocialCommentDO> selectReplyPage(AppReplyPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SocialCommentDO>()
                .eq(SocialCommentDO::getParentId, reqVO.getCommentId())
                .eq(SocialCommentDO::getStatus, PostStatusEnum.NORMAL.getStatus())
                .orderByAsc(SocialCommentDO::getCreateTime)); // 回复按时间正序
    }

    /**
     * 分页查询评论 (管理端)
     */
    default PageResult<SocialCommentDO> selectPage(CommentPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SocialCommentDO>()
                .eqIfPresent(SocialCommentDO::getPostId, reqVO.getPostId())
                .eqIfPresent(SocialCommentDO::getUserId, reqVO.getUserId())
                .eqIfPresent(SocialCommentDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(SocialCommentDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SocialCommentDO::getCreateTime));
    }

    /**
     * 根据帖子 ID 查询评论列表
     */
    default List<SocialCommentDO> selectListByPostId(Long postId) {
        return selectList(new LambdaQueryWrapperX<SocialCommentDO>()
                .eq(SocialCommentDO::getPostId, postId)
                .eq(SocialCommentDO::getStatus, PostStatusEnum.NORMAL.getStatus())
                .orderByDesc(SocialCommentDO::getCreateTime));
    }

    /**
     * 增加点赞数
     */
    @Update("UPDATE social_comment SET like_count = like_count + #{delta} WHERE id = #{id}")
    int updateLikeCount(@Param("id") Long id, @Param("delta") int delta);

    /**
     * 统计帖子的评论数
     */
    default Long selectCountByPostId(Long postId) {
        return selectCount(new LambdaQueryWrapperX<SocialCommentDO>()
                .eq(SocialCommentDO::getPostId, postId)
                .eq(SocialCommentDO::getStatus, PostStatusEnum.NORMAL.getStatus()));
    }

    /**
     * 统计某评论的回复数
     */
    default Integer selectReplyCountByParentId(Long parentId) {
        return Math.toIntExact(selectCount(new LambdaQueryWrapperX<SocialCommentDO>()
                .eq(SocialCommentDO::getParentId, parentId)
                .eq(SocialCommentDO::getStatus, PostStatusEnum.NORMAL.getStatus())));
    }

    /**
     * 获取某评论的前N条回复（用于预览）
     */
    default List<SocialCommentDO> selectTopReplies(Long parentId, int limit) {
        return selectList(new LambdaQueryWrapperX<SocialCommentDO>()
                .eq(SocialCommentDO::getParentId, parentId)
                .eq(SocialCommentDO::getStatus, PostStatusEnum.NORMAL.getStatus())
                .orderByAsc(SocialCommentDO::getCreateTime)
                .last("LIMIT " + limit));
    }

    /**
     * 统计帖子的回复总数（不含一级评论，即 parentId > 0 的评论数）
     */
    default Integer selectPostReplyCount(Long postId) {
        return Math.toIntExact(selectCount(new LambdaQueryWrapperX<SocialCommentDO>()
                .eq(SocialCommentDO::getPostId, postId)
                .gt(SocialCommentDO::getParentId, 0L)
                .eq(SocialCommentDO::getStatus, PostStatusEnum.NORMAL.getStatus())));
    }

}
