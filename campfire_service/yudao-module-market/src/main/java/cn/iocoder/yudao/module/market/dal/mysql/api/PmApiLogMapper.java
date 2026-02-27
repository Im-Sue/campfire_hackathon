package cn.iocoder.yudao.module.market.dal.mysql.api;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.market.controller.admin.api.vo.ApiLogPageReqVO;
import cn.iocoder.yudao.module.market.dal.dataobject.api.PmApiLogDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * API 调用日志 Mapper
 */
@Mapper
public interface PmApiLogMapper extends BaseMapperX<PmApiLogDO> {

    /**
     * 分页查询
     */
    default PageResult<PmApiLogDO> selectPage(ApiLogPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PmApiLogDO>()
                .eqIfPresent(PmApiLogDO::getApiType, reqVO.getApiType())
                .eqIfPresent(PmApiLogDO::getMethod, reqVO.getMethod())
                .eqIfPresent(PmApiLogDO::getStatus, reqVO.getStatus())
                .likeIfPresent(PmApiLogDO::getRefId, reqVO.getRefId())
                .betweenIfPresent(PmApiLogDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PmApiLogDO::getId));
    }

    /**
     * 获取最新日志
     */
    default List<PmApiLogDO> selectLatest(int limit) {
        return selectList(new LambdaQueryWrapperX<PmApiLogDO>()
                .orderByDesc(PmApiLogDO::getId)
                .last("LIMIT " + limit));
    }

    /**
     * 按时间范围删除
     */
    default int deleteByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return delete(new LambdaQueryWrapperX<PmApiLogDO>()
                .ge(PmApiLogDO::getCreateTime, startTime)
                .le(PmApiLogDO::getCreateTime, endTime));
    }

    /**
     * 删除指定时间之前的日志
     */
    default int deleteBeforeTime(LocalDateTime beforeTime) {
        return delete(new LambdaQueryWrapperX<PmApiLogDO>()
                .lt(PmApiLogDO::getCreateTime, beforeTime));
    }

    /**
     * 统计总数
     */
    default long countAll() {
        return selectCount();
    }

    /**
     * 统计成功数
     */
    default long countByStatus(String status) {
        return selectCount(new LambdaQueryWrapperX<PmApiLogDO>()
                .eq(PmApiLogDO::getStatus, status));
    }

    /**
     * 统计今日请求数
     */
    default long countToday() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        return selectCount(new LambdaQueryWrapperX<PmApiLogDO>()
                .ge(PmApiLogDO::getCreateTime, startOfDay));
    }

}
