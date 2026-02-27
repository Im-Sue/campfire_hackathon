package cn.iocoder.yudao.module.social.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.social.dal.dataobject.SocialFollowDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 社交关注 Mapper
 */
@Mapper
public interface SocialFollowMapper extends BaseMapperX<SocialFollowDO> {

    /**
     * 根据关注者和被关注者查询
     */
    default SocialFollowDO selectByUserAndFollowUser(Long userId, Long followUserId) {
        return selectOne(new LambdaQueryWrapperX<SocialFollowDO>()
                .eq(SocialFollowDO::getUserId, userId)
                .eq(SocialFollowDO::getFollowUserId, followUserId));
    }

    /**
     * 获取关注列表
     */
    default List<SocialFollowDO> selectFollowingList(Long userId) {
        return selectList(new LambdaQueryWrapperX<SocialFollowDO>()
                .eq(SocialFollowDO::getUserId, userId)
                .orderByDesc(SocialFollowDO::getCreateTime));
    }

    /**
     * 获取粉丝列表
     */
    default List<SocialFollowDO> selectFollowersList(Long followUserId) {
        return selectList(new LambdaQueryWrapperX<SocialFollowDO>()
                .eq(SocialFollowDO::getFollowUserId, followUserId)
                .orderByDesc(SocialFollowDO::getCreateTime));
    }

    /**
     * 统计关注数
     */
    default Long selectFollowingCount(Long userId) {
        return selectCount(new LambdaQueryWrapperX<SocialFollowDO>()
                .eq(SocialFollowDO::getUserId, userId));
    }

    /**
     * 统计粉丝数
     */
    default Long selectFollowersCount(Long followUserId) {
        return selectCount(new LambdaQueryWrapperX<SocialFollowDO>()
                .eq(SocialFollowDO::getFollowUserId, followUserId));
    }

    /**
     * 物理删除关注记录（绕过逻辑删除）
     * 
     * 关注记录不需要保留历史，使用物理删除避免唯一键冲突
     */
    @Delete("DELETE FROM social_follow WHERE id = #{id}")
    int physicalDeleteById(@Param("id") Long id);

}
