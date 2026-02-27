package cn.iocoder.yudao.module.market.service.api;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.market.controller.admin.api.vo.ApiLogPageReqVO;
import cn.iocoder.yudao.module.market.controller.admin.api.vo.ApiLogStatsVO;
import cn.iocoder.yudao.module.market.dal.dataobject.api.PmApiLogDO;
import cn.iocoder.yudao.module.market.enums.api.ApiLogRefTypeEnum;
import cn.iocoder.yudao.module.market.enums.api.ApiLogStatusEnum;
import cn.iocoder.yudao.module.market.enums.api.ApiLogTypeEnum;

import java.time.LocalDateTime;
import java.util.List;

/**
 * API 日志 Service 接口
 */
public interface PmApiLogService {

    /**
     * 异步记录 API 调用日志
     *
     * @param apiType      API 类型
     * @param method       方法名
     * @param url          请求 URL
     * @param params       请求参数 (JSON)
     * @param status       状态
     * @param httpCode     HTTP 状态码
     * @param responseTime 响应时间 (ms)
     * @param errorMessage 错误信息
     * @param refId        关联 ID
     * @param refType      关联类型
     */
    void logAsync(ApiLogTypeEnum apiType, String method, String url,
            String params, ApiLogStatusEnum status, Integer httpCode,
            Long responseTime, String errorMessage,
            String refId, ApiLogRefTypeEnum refType);

    /**
     * 简化的异步记录方法（成功时调用）
     */
    void logSuccessAsync(ApiLogTypeEnum apiType, String method, String url,
            String params, Long responseTime,
            String refId, ApiLogRefTypeEnum refType);

    /**
     * 简化的异步记录方法（失败时调用）
     */
    void logFailAsync(ApiLogTypeEnum apiType, String method, String url,
            String params, Integer httpCode, Long responseTime,
            String errorMessage, String refId, ApiLogRefTypeEnum refType);

    /**
     * 分页查询日志
     */
    PageResult<PmApiLogDO> getApiLogPage(ApiLogPageReqVO pageReqVO);

    /**
     * 获取最新日志
     */
    List<PmApiLogDO> getLatestLogs(Integer limit);

    /**
     * 按时间范围删除
     */
    int deleteByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 清理过期日志
     */
    int cleanExpiredLogs(int retentionDays);

    /**
     * 获取统计信息
     */
    ApiLogStatsVO getStats();

}
