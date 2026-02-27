package cn.iocoder.yudao.module.market.dal.mysql.comment;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.market.dal.dataobject.comment.PmEventCommentLikeDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 事件评论点赞 Mapper
 */
@Mapper
public interface PmEventCommentLikeMapper extends BaseMapperX<PmEventCommentLikeDO> {

    /**
     * 根据用户和评论查询点赞记录
     */
    default PmEventCommentLikeDO selectByUserAndComment(Long userId, Long commentId) {
        return selectOne(new LambdaQueryWrapperX<PmEventCommentLikeDO>()
                .eq(PmEventCommentLikeDO::getUserId, userId)
                .eq(PmEventCommentLikeDO::getCommentId, commentId));
    }

}
