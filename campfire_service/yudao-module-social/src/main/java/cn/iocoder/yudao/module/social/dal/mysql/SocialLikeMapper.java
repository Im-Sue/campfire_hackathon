package cn.iocoder.yudao.module.social.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.social.dal.dataobject.SocialLikeDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 社交点赞 Mapper
 */
@Mapper
public interface SocialLikeMapper extends BaseMapperX<SocialLikeDO> {

    /**
     * 根据用户、目标类型、目标 ID 查询点赞记录
     */
    default SocialLikeDO selectByUserAndTarget(Long userId, Integer targetType, Long targetId) {
        return selectOne(new LambdaQueryWrapperX<SocialLikeDO>()
                .eq(SocialLikeDO::getUserId, userId)
                .eq(SocialLikeDO::getTargetType, targetType)
                .eq(SocialLikeDO::getTargetId, targetId));
    }

    /**
     * 删除点赞记录
     */
    default int deleteByUserAndTarget(Long userId, Integer targetType, Long targetId) {
        return delete(new LambdaQueryWrapperX<SocialLikeDO>()
                .eq(SocialLikeDO::getUserId, userId)
                .eq(SocialLikeDO::getTargetType, targetType)
                .eq(SocialLikeDO::getTargetId, targetId));
    }

    /**
     * 检查用户是否已点赞
     */
    default boolean existsByUserAndTarget(Long userId, Integer targetType, Long targetId) {
        return selectByUserAndTarget(userId, targetType, targetId) != null;
    }

    /**
     * 物理删除点赞记录（绕过逻辑删除）
     * 
     * 点赞记录不需要保留历史，使用物理删除避免唯一键冲突
     */
    @Delete("DELETE FROM social_like WHERE id = #{id}")
    int physicalDeleteById(@Param("id") Long id);

    /**
     * 根据用户和目标物理删除点赞记录（绕过逻辑删除）
     */
    @Delete("DELETE FROM social_like WHERE user_id = #{userId} AND target_type = #{targetType} AND target_id = #{targetId}")
    int physicalDeleteByUserAndTarget(@Param("userId") Long userId,
            @Param("targetType") Integer targetType,
            @Param("targetId") Long targetId);

}
