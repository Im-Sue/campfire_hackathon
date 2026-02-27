package cn.iocoder.yudao.module.market.service.api;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.market.controller.admin.api.vo.ApiLogPageReqVO;
import cn.iocoder.yudao.module.market.controller.admin.api.vo.ApiLogStatsVO;
import cn.iocoder.yudao.module.market.dal.dataobject.api.PmApiLogDO;
import cn.iocoder.yudao.module.market.dal.mysql.api.PmApiLogMapper;
import cn.iocoder.yudao.module.market.enums.api.ApiLogRefTypeEnum;
import cn.iocoder.yudao.module.market.enums.api.ApiLogStatusEnum;
import cn.iocoder.yudao.module.market.enums.api.ApiLogTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * API 日志 Service 实现类
 * 
 * 使用 @Async 异步写入日志，确保不影响 API 调用性能
 * 使用 @TenantIgnore 忽略租户检查（系统级日志无需按租户隔离）
 */
@Service
@Slf4j
public class PmApiLogServiceImpl implements PmApiLogService {

    @Resource
    private PmApiLogMapper apiLogMapper;

    @Override
    @Async
    @TenantIgnore
    public void logAsync(ApiLogTypeEnum apiType, String method, String url,
            String params, ApiLogStatusEnum status, Integer httpCode,
            Long responseTime, String errorMessage,
            String refId, ApiLogRefTypeEnum refType) {
        try {
            PmApiLogDO logDO = new PmApiLogDO();
            logDO.setApiType(apiType.getCode());
            logDO.setMethod(truncate(method, 64));
            logDO.setUrl(truncate(url, 512));
            logDO.setParams(params); // TEXT 类型，无需截断
            logDO.setStatus(status.getCode());
            logDO.setHttpCode(httpCode);
            logDO.setResponseTime(responseTime);
            logDO.setErrorMessage(truncate(errorMessage, 500));
            logDO.setRefId(truncate(refId, 128));
            logDO.setRefType(refType != null ? refType.getCode() : ApiLogRefTypeEnum.NONE.getCode());
            logDO.setTenantId(1L); // 系统日志统一使用租户1
            apiLogMapper.insert(logDO);
        } catch (Exception e) {
            // 日志记录失败不应影响业务，仅打印错误日志
            log.error("[logAsync][记录 API 日志失败 method={}, refId={}]", method, refId, e);
        }
    }

    @Override
    @Async
    @TenantIgnore
    public void logSuccessAsync(ApiLogTypeEnum apiType, String method, String url,
            String params, Long responseTime,
            String refId, ApiLogRefTypeEnum refType) {
        logAsync(apiType, method, url, params, ApiLogStatusEnum.SUCCESS, 200,
                responseTime, null, refId, refType);
    }

    @Override
    @Async
    @TenantIgnore
    public void logFailAsync(ApiLogTypeEnum apiType, String method, String url,
            String params, Integer httpCode, Long responseTime,
            String errorMessage, String refId, ApiLogRefTypeEnum refType) {
        logAsync(apiType, method, url, params, ApiLogStatusEnum.FAIL, httpCode,
                responseTime, errorMessage, refId, refType);
    }

    /**
     * 截断字符串到指定长度
     */
    private String truncate(String str, int maxLength) {
        if (str == null)
            return null;
        return str.length() > maxLength ? str.substring(0, maxLength) : str;
    }

    @Override
    @TenantIgnore
    public PageResult<PmApiLogDO> getApiLogPage(ApiLogPageReqVO pageReqVO) {
        return apiLogMapper.selectPage(pageReqVO);
    }

    @Override
    @TenantIgnore
    public List<PmApiLogDO> getLatestLogs(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 50;
        }
        if (limit > 500) {
            limit = 500;
        }
        return apiLogMapper.selectLatest(limit);
    }

    @Override
    @TenantIgnore
    public int deleteByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return apiLogMapper.deleteByTimeRange(startTime, endTime);
    }

    @Override
    @TenantIgnore
    public int cleanExpiredLogs(int retentionDays) {
        if (retentionDays <= 0) {
            retentionDays = 15; // 默认保留 15 天
        }
        LocalDateTime beforeTime = LocalDateTime.now().minusDays(retentionDays);
        int deleted = apiLogMapper.deleteBeforeTime(beforeTime);
        log.info("[cleanExpiredLogs][清理 {} 天前的 API 日志，删除 {} 条]", retentionDays, deleted);
        return deleted;
    }

    @Override
    @TenantIgnore
    public ApiLogStatsVO getStats() {
        ApiLogStatsVO stats = new ApiLogStatsVO();

        long total = apiLogMapper.countAll();
        long success = apiLogMapper.countByStatus(ApiLogStatusEnum.SUCCESS.getCode());
        long fail = apiLogMapper.countByStatus(ApiLogStatusEnum.FAIL.getCode());
        long today = apiLogMapper.countToday();

        stats.setTotalCount(total);
        stats.setSuccessCount(success);
        stats.setFailCount(fail);
        stats.setTodayCount(today);

        // 计算成功率
        if (total > 0) {
            BigDecimal rate = new BigDecimal(success)
                    .divide(new BigDecimal(total), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
            stats.setSuccessRate(rate);
        } else {
            stats.setSuccessRate(BigDecimal.ZERO);
        }

        return stats;
    }

}
