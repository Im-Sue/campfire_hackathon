package cn.iocoder.yudao.module.social.dal.mysql;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.social.controller.admin.vo.TopicPageReqVO;
import cn.iocoder.yudao.module.social.dal.dataobject.SocialTopicDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 社交话题 Mapper
 */
@Mapper
public interface SocialTopicMapper extends BaseMapperX<SocialTopicDO> {

    /**
     * 根据话题名称查询
     */
    default SocialTopicDO selectByName(String name) {
        return selectOne(new LambdaQueryWrapperX<SocialTopicDO>()
                .eq(SocialTopicDO::getName, name));
    }

    /**
     * 获取热门话题列表
     */
    default List<SocialTopicDO> selectHotList(Integer limit) {
        return selectList(new LambdaQueryWrapperX<SocialTopicDO>()
                .eq(SocialTopicDO::getStatus, 0)
                .orderByDesc(SocialTopicDO::getHeatScore)
                .last("LIMIT " + limit));
    }

    /**
     * 分页查询（管理端）
     */
    default PageResult<SocialTopicDO> selectPage(TopicPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SocialTopicDO>()
                .likeIfPresent(SocialTopicDO::getName, reqVO.getName())
                .eqIfPresent(SocialTopicDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(SocialTopicDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SocialTopicDO::getId));
    }

    /**
     * 增加热度
     */
    @Update("UPDATE social_topic SET heat_score = heat_score + #{delta} WHERE id = #{id}")
    int incrementHeatScore(@Param("id") Long id, @Param("delta") Integer delta);

}
