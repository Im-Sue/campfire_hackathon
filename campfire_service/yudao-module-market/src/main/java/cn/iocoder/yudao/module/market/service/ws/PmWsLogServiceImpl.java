package cn.iocoder.yudao.module.market.service.ws;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.market.controller.admin.ws.vo.WsLogPageReqVO;
import cn.iocoder.yudao.module.market.dal.dataobject.ws.PmWsLogDO;
import cn.iocoder.yudao.module.market.dal.mysql.ws.PmWsLogMapper;
import cn.iocoder.yudao.module.market.enums.ws.WsLogRefTypeEnum;
import cn.iocoder.yudao.module.market.enums.ws.WsLogTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * WS 日志 Service 实现类
 * 
 * 使用 @Async 异步写入日志，确保不影响 WS 消息处理性能
 * 使用 @TenantIgnore 忽略租户检查（系统级日志无需按租户隔离）
 */
@Service
@Slf4j
public class PmWsLogServiceImpl implements PmWsLogService {

    @Resource
    private PmWsLogMapper wsLogMapper;

    @Override
    @Async
    @TenantIgnore
    public void logAsync(WsLogTypeEnum type, String refId, WsLogRefTypeEnum refType,
            String event, String message, String description) {
        try {
            PmWsLogDO logDO = new PmWsLogDO();
            logDO.setType(type.getCode());
            logDO.setRefId(truncate(refId, 128));
            logDO.setRefType(refType != null ? refType.getCode() : WsLogRefTypeEnum.NONE.getCode());
            logDO.setEvent(truncate(event, 64));
            logDO.setMessage(message); // TEXT 类型，无需截断
            logDO.setDescription(truncate(description, 500)); // 预留一点空间
            logDO.setTenantId(1L); // 系统日志统一使用租户1
            wsLogMapper.insert(logDO);
        } catch (Exception e) {
            // 日志记录失败不应影响业务，仅打印错误日志
            log.error("[logAsync][记录 WS 日志失败 type={}, refId={}]", type, refId, e);
        }
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
    @Async
    @TenantIgnore
    public void logAsync(WsLogTypeEnum type, String description) {
        logAsync(type, null, WsLogRefTypeEnum.NONE, null, null, description);
    }

    @Override
    @Async
    @TenantIgnore
    public void logAsync(WsLogTypeEnum type, String message, String description) {
        logAsync(type, null, WsLogRefTypeEnum.NONE, null, message, description);
    }

    @Override
    @TenantIgnore
    public PageResult<PmWsLogDO> getWsLogPage(WsLogPageReqVO pageReqVO) {
        return wsLogMapper.selectPage(pageReqVO);
    }

    @Override
    @TenantIgnore
    public List<PmWsLogDO> getLatestLogs(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 50;
        }
        if (limit > 500) {
            limit = 500;
        }
        return wsLogMapper.selectLatest(limit);
    }

    @Override
    @TenantIgnore
    public int deleteByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return wsLogMapper.deleteByTimeRange(startTime, endTime);
    }

    @Override
    @TenantIgnore
    public int cleanExpiredLogs(int retentionDays) {
        if (retentionDays <= 0) {
            retentionDays = 15; // 默认保留 15 天
        }
        LocalDateTime beforeTime = LocalDateTime.now().minusDays(retentionDays);
        int deleted = wsLogMapper.deleteBeforeTime(beforeTime);
        log.info("[cleanExpiredLogs][清理 {} 天前的日志，删除 {} 条]", retentionDays, deleted);
        return deleted;
    }

}
