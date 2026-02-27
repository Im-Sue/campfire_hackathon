package cn.iocoder.yudao.module.social.dal.mysql;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.social.dal.dataobject.SocialActivityDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 社交互动记录 Mapper
 */
@Mapper
public interface SocialActivityMapper extends BaseMapperX<SocialActivityDO> {

    /**
     * 查询用户相关的互动记录（我发起的 + 对我的）
     */
    default PageResult<SocialActivityDO> selectPageByUserId(Long userId, PageParam pageParam) {
        return selectPage(pageParam, new LambdaQueryWrapperX<SocialActivityDO>()
                .eq(SocialActivityDO::getActorUserId, userId)
                .or()
                .eq(SocialActivityDO::getTargetUserId, userId)
                .orderByDesc(SocialActivityDO::getCreateTime));
    }

    /**
     * 查询我发起的互动
     */
    default PageResult<SocialActivityDO> selectPageByActorUserId(Long userId, PageParam pageParam) {
        return selectPage(pageParam, new LambdaQueryWrapperX<SocialActivityDO>()
                .eq(SocialActivityDO::getActorUserId, userId)
                .orderByDesc(SocialActivityDO::getCreateTime));
    }

    /**
     * 查询对我的互动
     */
    default PageResult<SocialActivityDO> selectPageByTargetUserId(Long userId, PageParam pageParam) {
        return selectPage(pageParam, new LambdaQueryWrapperX<SocialActivityDO>()
                .eq(SocialActivityDO::getTargetUserId, userId)
                .orderByDesc(SocialActivityDO::getCreateTime));
    }

}
