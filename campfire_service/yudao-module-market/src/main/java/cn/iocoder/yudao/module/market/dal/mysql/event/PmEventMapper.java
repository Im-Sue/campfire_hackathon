package cn.iocoder.yudao.module.market.dal.mysql.event;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.market.dal.dataobject.event.PmEventDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 预测市场事件 Mapper
 */
@Mapper
public interface PmEventMapper extends BaseMapperX<PmEventDO> {

    default PmEventDO selectByPolymarketEventId(String polymarketEventId) {
        return selectOne(PmEventDO::getPolymarketEventId, polymarketEventId);
    }

    default List<PmEventDO> selectByStatus(Integer status) {
        return selectList(PmEventDO::getStatus, status);
    }

    default List<PmEventDO> selectByCategory(String category) {
        return selectList(new LambdaQueryWrapperX<PmEventDO>()
                .eqIfPresent(PmEventDO::getCategory, category)
                .eq(PmEventDO::getStatus, 1) // 已上架
                .orderByDesc(PmEventDO::getCreateTime));
    }

    default PageResult<PmEventDO> selectPage(Integer status, String category,
            cn.iocoder.yudao.framework.common.pojo.PageParam pageParam) {
        return selectPage(pageParam, new LambdaQueryWrapperX<PmEventDO>()
                .eqIfPresent(PmEventDO::getStatus, status)
                .eqIfPresent(PmEventDO::getCategory, category)
                .orderByDesc(PmEventDO::getCreateTime));
    }

    /**
     * 根据标题模糊搜索，返回事件 ID 列表
     *
     * @param title  搜索关键词
     * @param status 状态筛选
     * @return 事件 ID 列表
     */
    default List<Long> selectIdsByTitleLike(String title, Integer status) {
        return selectList(new LambdaQueryWrapperX<PmEventDO>()
                .likeIfPresent(PmEventDO::getTitle, title)
                .eqIfPresent(PmEventDO::getStatus, status))
                .stream()
                .map(PmEventDO::getId)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 根据事件 ID 列表分页查询
     *
     * @param ids       事件 ID 列表
     * @param status    状态筛选
     * @param category  分类筛选
     * @param pageParam 分页参数
     * @return 分页结果
     */
    default PageResult<PmEventDO> selectPageByIds(List<Long> ids, Integer status, String category,
            cn.iocoder.yudao.framework.common.pojo.PageParam pageParam) {
        return selectPage(pageParam, new LambdaQueryWrapperX<PmEventDO>()
                .in(PmEventDO::getId, ids)
                .eqIfPresent(PmEventDO::getStatus, status)
                .eqIfPresent(PmEventDO::getCategory, category)
                .orderByDesc(PmEventDO::getCreateTime));
    }

    /**
     * 分页查询活跃事件（有 TRADING 状态市场的已上架事件）
     *
     * @param activeEventIds 活跃事件 ID 列表
     * @param category       分类筛选
     * @param pageParam      分页参数
     * @return 分页结果
     */
    default PageResult<PmEventDO> selectActiveEventPage(List<Long> activeEventIds, String category,
            cn.iocoder.yudao.framework.common.pojo.PageParam pageParam) {
        if (activeEventIds == null || activeEventIds.isEmpty()) {
            return new PageResult<>(java.util.Collections.emptyList(), 0L);
        }
        return selectPage(pageParam, new LambdaQueryWrapperX<PmEventDO>()
                .in(PmEventDO::getId, activeEventIds)
                .eq(PmEventDO::getStatus, 1) // PUBLISHED
                .eqIfPresent(PmEventDO::getCategory, category)
                .orderByDesc(PmEventDO::getCreateTime));
    }

}
