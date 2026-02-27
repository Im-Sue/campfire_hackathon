package cn.iocoder.yudao.module.market.service.ws;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.market.controller.admin.ws.vo.WsLogPageReqVO;
import cn.iocoder.yudao.module.market.dal.dataobject.ws.PmWsLogDO;
import cn.iocoder.yudao.module.market.enums.ws.WsLogRefTypeEnum;
import cn.iocoder.yudao.module.market.enums.ws.WsLogTypeEnum;

import java.time.LocalDateTime;
import java.util.List;

/**
 * WS 日志 Service 接口
 */
public interface PmWsLogService {

    /**
     * 异步记录日志（不阻塞主流程）
     *
     * @param type        日志类型
     * @param refId       关联 ID
     * @param refType     ID 类型
     * @param event       WS 事件
     * @param message     原始消息
     * @param description 描述
     */
    void logAsync(WsLogTypeEnum type, String refId, WsLogRefTypeEnum refType,
            String event, String message, String description);

    /**
     * 记录简单日志（无关联 ID）
     */
    void logAsync(WsLogTypeEnum type, String description);

    /**
     * 记录带消息的日志
     */
    void logAsync(WsLogTypeEnum type, String message, String description);

    /**
     * 分页查询日志
     */
    PageResult<PmWsLogDO> getWsLogPage(WsLogPageReqVO pageReqVO);

    /**
     * 获取最新日志
     */
    List<PmWsLogDO> getLatestLogs(Integer limit);

    /**
     * 按时间范围删除日志
     */
    int deleteByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 清理过期日志（保留指定天数）
     */
    int cleanExpiredLogs(int retentionDays);

}
