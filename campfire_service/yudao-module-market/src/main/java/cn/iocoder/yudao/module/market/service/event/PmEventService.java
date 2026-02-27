package cn.iocoder.yudao.module.market.service.event;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.market.dal.dataobject.event.PmEventDO;

import java.util.List;

/**
 * 预测市场事件 Service 接口
 */
public interface PmEventService {

    /**
     * 创建事件
     *
     * @param event 事件
     * @return 事件编号
     */
    Long createEvent(PmEventDO event);

    /**
     * 更新事件
     *
     * @param event 事件
     */
    void updateEvent(PmEventDO event);

    /**
     * 获取事件
     *
     * @param id 事件编号
     * @return 事件
     */
    PmEventDO getEvent(Long id);

    /**
     * 根据 Polymarket Event ID 获取事件
     *
     * @param polymarketEventId Polymarket Event ID
     * @return 事件
     */
    PmEventDO getEventByPolymarketEventId(String polymarketEventId);

    /**
     * 获取已上架的事件列表
     *
     * @return 事件列表
     */
    List<PmEventDO> getPublishedEvents();

    /**
     * 获取指定分类的已上架事件列表
     *
     * @param category 分类
     * @return 事件列表
     */
    List<PmEventDO> getPublishedEventsByCategory(String category);

    /**
     * 分页查询事件
     *
     * @param status    状态
     * @param category  分类
     * @param pageParam 分页参数
     * @return 分页结果
     */
    PageResult<PmEventDO> getEventPage(Integer status, String category, PageParam pageParam);

    /**
     * 分页查询事件（支持标题搜索）
     *
     * @param status    状态
     * @param category  分类
     * @param title     搜索关键词（模糊匹配 Event.title 或 Market.question）
     * @param pageParam 分页参数
     * @return 分页结果
     */
    PageResult<PmEventDO> getEventPage(Integer status, String category, String title, PageParam pageParam);

    /**
     * 上架事件
     *
     * @param id 事件编号
     */
    void publishEvent(Long id);

    /**
     * 下架事件
     *
     * @param id 事件编号
     */
    void unpublishEvent(Long id);

    /**
     * 校验事件存在且已上架
     *
     * @param id 事件编号
     * @return 事件
     */
    PmEventDO validateEventPublished(Long id);

    /**
     * 分页查询活跃事件（有 TRADING 状态市场的已上架事件）
     * 用于 C端首页/分类列表展示
     *
     * @param category  分类
     * @param pageParam 分页参数
     * @return 分页结果
     */
    PageResult<PmEventDO> getActiveEventPage(String category, PageParam pageParam);

}
