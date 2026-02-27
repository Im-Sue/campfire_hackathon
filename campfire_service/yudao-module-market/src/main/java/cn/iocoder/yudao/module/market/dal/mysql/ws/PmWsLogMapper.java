package cn.iocoder.yudao.module.market.dal.mysql.ws;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.market.controller.admin.ws.vo.WsLogPageReqVO;
import cn.iocoder.yudao.module.market.dal.dataobject.ws.PmWsLogDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * WS 日志 Mapper
 */
@Mapper
public interface PmWsLogMapper extends BaseMapperX<PmWsLogDO> {

    /**
     * 分页查询
     */
    default PageResult<PmWsLogDO> selectPage(WsLogPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PmWsLogDO>()
                .eqIfPresent(PmWsLogDO::getType, reqVO.getType())
                .eqIfPresent(PmWsLogDO::getRefId, reqVO.getRefId())
                .eqIfPresent(PmWsLogDO::getRefType, reqVO.getRefType())
                .eqIfPresent(PmWsLogDO::getEvent, reqVO.getEvent())
                .betweenIfPresent(PmWsLogDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PmWsLogDO::getId));
    }

    /**
     * 获取最新 N 条日志
     */
    default List<PmWsLogDO> selectLatest(Integer limit) {
        return selectList(new LambdaQueryWrapperX<PmWsLogDO>()
                .orderByDesc(PmWsLogDO::getId)
                .last("LIMIT " + limit));
    }

    /**
     * 删除指定时间范围的日志
     */
    default int deleteByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return delete(new LambdaQueryWrapperX<PmWsLogDO>()
                .ge(PmWsLogDO::getCreateTime, startTime)
                .le(PmWsLogDO::getCreateTime, endTime));
    }

    /**
     * 删除指定时间之前的日志（用于定时清理）
     */
    default int deleteBeforeTime(LocalDateTime beforeTime) {
        return delete(new LambdaQueryWrapperX<PmWsLogDO>()
                .lt(PmWsLogDO::getCreateTime, beforeTime));
    }

}
