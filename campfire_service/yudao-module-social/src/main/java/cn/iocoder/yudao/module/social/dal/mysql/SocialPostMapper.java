package cn.iocoder.yudao.module.social.dal.mysql;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.social.controller.admin.vo.PostPageReqVO;
import cn.iocoder.yudao.module.social.controller.app.vo.AppPostPageReqVO;
import cn.iocoder.yudao.module.social.dal.dataobject.SocialPostDO;
import cn.iocoder.yudao.module.social.enums.PostStatusEnum;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 社交帖子 Mapper
 */
@Mapper
public interface SocialPostMapper extends BaseMapperX<SocialPostDO> {

    /**
     * 分页查询帖子 (用户端 - 广场)
     * 支持按用户ID和话题名称筛选
     */
    default PageResult<SocialPostDO> selectPage(AppPostPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SocialPostDO>()
                .eqIfPresent(SocialPostDO::getUserId, reqVO.getUserId())
                .likeIfPresent(SocialPostDO::getContent, reqVO.getTopicName())
                .eq(SocialPostDO::getStatus, PostStatusEnum.NORMAL.getStatus())
                .orderByDesc(SocialPostDO::getCreateTime));
    }

    /**
     * 分页查询帖子 (管理端)
     */
    default PageResult<SocialPostDO> selectPage(PostPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SocialPostDO>()
                .eqIfPresent(SocialPostDO::getUserId, reqVO.getUserId())
                .eqIfPresent(SocialPostDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(SocialPostDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SocialPostDO::getCreateTime));
    }

    /**
     * 增加点赞数
     */
    @Update("UPDATE social_post SET like_count = like_count + #{delta} WHERE id = #{id}")
    int updateLikeCount(@Param("id") Long id, @Param("delta") int delta);

    /**
     * 增加评论数
     */
    @Update("UPDATE social_post SET comment_count = comment_count + #{delta} WHERE id = #{id}")
    int updateCommentCount(@Param("id") Long id, @Param("delta") int delta);

    /**
     * 增加浏览数
     */
    @Update("UPDATE social_post SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(@Param("id") Long id);

    /**
     * 增加热度分数
     */
    @Update("UPDATE social_post SET heat_score = heat_score + #{delta} WHERE id = #{id}")
    int updateHeatScore(@Param("id") Long id, @Param("delta") int delta);

    /**
     * 分页查询热门帖子（按热度排序）
     */
    default PageResult<SocialPostDO> selectHotPage(AppPostPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SocialPostDO>()
                .eq(SocialPostDO::getStatus, PostStatusEnum.NORMAL.getStatus())
                .orderByDesc(SocialPostDO::getHeatScore)
                .orderByDesc(SocialPostDO::getCreateTime));
    }

    /**
     * 统计用户帖子数
     */
    default Integer selectCountByUserId(Long userId) {
        return Math.toIntExact(selectCount(new LambdaQueryWrapperX<SocialPostDO>()
                .eq(SocialPostDO::getUserId, userId)
                .eq(SocialPostDO::getStatus, PostStatusEnum.NORMAL.getStatus())));
    }

}
