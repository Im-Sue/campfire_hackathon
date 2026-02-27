package cn.iocoder.yudao.module.market.service.event;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.market.dal.dataobject.event.PmEventDO;
import cn.iocoder.yudao.module.market.dal.mysql.event.PmEventMapper;
import cn.iocoder.yudao.module.market.enums.EventStatusEnum;
import cn.iocoder.yudao.module.market.event.EventPublishedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.*;
import cn.hutool.core.util.StrUtil;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.market.enums.ErrorCodeConstants.*;

/**
 * 预测市场事件 Service 实现类
 */
@Service
@Validated
@Slf4j
public class PmEventServiceImpl implements PmEventService {

    @Resource
    private PmEventMapper pmEventMapper;

    @Resource
    private cn.iocoder.yudao.module.market.dal.mysql.market.PmMarketMapper pmMarketMapper;

    @Resource
    private ApplicationEventPublisher eventPublisher;

    @Override
    public Long createEvent(PmEventDO event) {
        pmEventMapper.insert(event);
        return event.getId();
    }

    @Override
    public void updateEvent(PmEventDO event) {
        pmEventMapper.updateById(event);
    }

    @Override
    public PmEventDO getEvent(Long id) {
        return pmEventMapper.selectById(id);
    }

    @Override
    public PmEventDO getEventByPolymarketEventId(String polymarketEventId) {
        return pmEventMapper.selectByPolymarketEventId(polymarketEventId);
    }

    @Override
    public List<PmEventDO> getPublishedEvents() {
        return pmEventMapper.selectByStatus(EventStatusEnum.PUBLISHED.getStatus());
    }

    @Override
    public List<PmEventDO> getPublishedEventsByCategory(String category) {
        return pmEventMapper.selectByCategory(category);
    }

    @Override
    public PageResult<PmEventDO> getEventPage(Integer status, String category, PageParam pageParam) {
        return pmEventMapper.selectPage(status, category, pageParam);
    }

    @Override
    public PageResult<PmEventDO> getEventPage(Integer status, String category, String title, PageParam pageParam) {
        // 无搜索词时，走原有逻辑
        if (StrUtil.isBlank(title)) {
            return pmEventMapper.selectPage(status, category, pageParam);
        }

        // 第一阶段：搜索 Event.title
        List<Long> eventIdsByTitle = pmEventMapper.selectIdsByTitleLike(title, status);

        // 第二阶段：搜索 Market.question → 获取 eventId
        List<Long> eventIdsByMarket = pmMarketMapper.selectEventIdsByQuestionLike(title);

        // 合并去重（保持顺序）
        Set<Long> allEventIds = new LinkedHashSet<>(eventIdsByTitle);
        allEventIds.addAll(eventIdsByMarket);

        // 无匹配结果
        if (allEventIds.isEmpty()) {
            return new PageResult<>(Collections.emptyList(), 0L);
        }

        // 根据 EventIds 分页查询
        return pmEventMapper.selectPageByIds(new ArrayList<>(allEventIds), status, category, pageParam);
    }

    @Resource
    private cn.iocoder.yudao.module.market.service.market.PmMarketService pmMarketService;

    @Override
    public void publishEvent(Long id) {
        // 校验事件存在
        PmEventDO event = validateEventExists(id);

        // 校验状态
        if (EventStatusEnum.PUBLISHED.getStatus().equals(event.getStatus())) {
            throw exception(EVENT_ALREADY_PUBLISHED);
        }

        // 更新事件状态
        event.setStatus(EventStatusEnum.PUBLISHED.getStatus());
        pmEventMapper.updateById(event);

        // 更新该事件下的市场状态：DRAFT → TRADING，已封盘的市场保持不变
        java.util.List<cn.iocoder.yudao.module.market.dal.dataobject.market.PmMarketDO> markets = pmMarketService
                .getMarketsByEventId(id);
        int tradingCount = 0;
        int skippedCount = 0;
        for (cn.iocoder.yudao.module.market.dal.dataobject.market.PmMarketDO market : markets) {
            if (cn.iocoder.yudao.module.market.enums.MarketStatusEnum.DRAFT.getStatus().equals(market.getStatus())) {
                market.setStatus(cn.iocoder.yudao.module.market.enums.MarketStatusEnum.TRADING.getStatus());
                pmMarketService.updateMarket(market);
                tradingCount++;
            } else {
                // 已封盘或其他状态的市场保持不变
                skippedCount++;
            }
        }

        // 发布事件上架事件，触发 Agent 评论生成
        eventPublisher.publishEvent(new EventPublishedEvent(this, id));

        log.info("[publishEvent][事件 {} 上架成功, 市场上架: {}, 跳过: {}]", id, tradingCount, skippedCount);
    }

    @Override
    public void unpublishEvent(Long id) {
        // 校验事件存在
        PmEventDO event = validateEventExists(id);

        // 校验状态
        if (!EventStatusEnum.PUBLISHED.getStatus().equals(event.getStatus())) {
            throw exception(EVENT_NOT_PUBLISHED);
        }

        // 更新事件状态
        event.setStatus(EventStatusEnum.UNPUBLISHED.getStatus());
        pmEventMapper.updateById(event);

        // 更新该事件下交易中的市场状态：TRADING → DRAFT（恢复为待上架，支持二次上架）
        java.util.List<cn.iocoder.yudao.module.market.dal.dataobject.market.PmMarketDO> markets = pmMarketService
                .getMarketsByEventId(id);
        int draftCount = 0;
        for (cn.iocoder.yudao.module.market.dal.dataobject.market.PmMarketDO market : markets) {
            if (cn.iocoder.yudao.module.market.enums.MarketStatusEnum.TRADING.getStatus().equals(market.getStatus())) {
                market.setStatus(cn.iocoder.yudao.module.market.enums.MarketStatusEnum.DRAFT.getStatus());
                pmMarketService.updateMarket(market);
                draftCount++;
            }
        }

        log.info("[unpublishEvent][事件 {} 下架成功, 恢复待上架市场: {}]", id, draftCount);
    }

    private PmEventDO validateEventExists(Long id) {
        PmEventDO event = pmEventMapper.selectById(id);
        if (event == null) {
            throw exception(EVENT_NOT_EXISTS);
        }
        return event;
    }

    @Override
    public PmEventDO validateEventPublished(Long id) {
        PmEventDO event = validateEventExists(id);
        if (!EventStatusEnum.PUBLISHED.getStatus().equals(event.getStatus())) {
            throw exception(EVENT_NOT_PUBLISHED);
        }
        return event;
    }

    @Override
    public PageResult<PmEventDO> getActiveEventPage(String category, PageParam pageParam) {
        // 1. 获取有活跃市场（TRADING 状态）的事件 ID 列表
        List<Long> activeEventIds = pmMarketMapper.selectActiveEventIds();

        // 2. 根据活跃事件 ID 分页查询
        return pmEventMapper.selectActiveEventPage(activeEventIds, category, pageParam);
    }

}
