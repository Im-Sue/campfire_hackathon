package cn.iocoder.yudao.module.market.service.sync;

import cn.iocoder.yudao.module.market.api.PolymarketApiClient;
import cn.iocoder.yudao.module.market.api.dto.PolymarketEventDTO;
import cn.iocoder.yudao.module.market.dal.dataobject.event.PmEventDO;
import cn.iocoder.yudao.module.market.dal.dataobject.market.PmMarketDO;
import cn.iocoder.yudao.module.market.enums.EventStatusEnum;
import cn.iocoder.yudao.module.market.enums.MarketStatusEnum;
import cn.iocoder.yudao.module.market.service.event.PmEventService;
import cn.iocoder.yudao.module.market.service.market.PmMarketService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.market.enums.ErrorCodeConstants.*;

/**
 * Polymarket 同步服务实现
 */
@Service
@Validated
@Slf4j
public class PolymarketSyncServiceImpl implements PolymarketSyncService {

    @Resource
    private PolymarketApiClient polymarketApiClient;

    @Resource
    private PmEventService pmEventService;

    @Resource
    private PmMarketService pmMarketService;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * Polymarket 分类标签映射
     * 将前端使用的友好分类名映射为 Polymarket API 实际使用的 tag_slug
     * 
     * 关键说明：
     * - crypto: 官网 Crypto 分类实际使用 'crypto-prices' 标签，返回 Bitcoin/Ethereum 等价格预测事件
     * - 简单使用 'crypto' 标签会返回与加密货币相关的通用事件（如 MicroStrategy、IPO 等），但不是价格预测
     */
    private static final java.util.Map<String, String> CATEGORY_TAG_MAPPING = Collections.singletonMap("crypto",
            "crypto-prices"); // 加密货币价格预测

    @Override
    public List<PolymarketEventDTO> browseEvents(String category, int pageNo, int pageSize) {
        // 映射分类标签
        String tagSlug = CATEGORY_TAG_MAPPING.getOrDefault(category, category);
        // 计算 offset
        int offset = (pageNo - 1) * pageSize;
        log.info("[browseEvents][开始浏览 Polymarket 事件, category={}, tagSlug={}, pageNo={}, pageSize={}, offset={}]",
                category, tagSlug, pageNo, pageSize, offset);

        List<PolymarketEventDTO> events = polymarketApiClient.getEvents(true, false, tagSlug, pageSize, offset);

        // 如果是 sports 分类，只保留今天及之后有 gameId 的单场比赛，按 gameStartTime 升序排序
        if ("sports".equals(category) && !CollectionUtils.isEmpty(events)) {
            String today = LocalDate.now().toString(); // 格式: 2026-01-11

            events = events.stream()
                    // 1. 只保留有 gameId 的单场比赛
                    .filter(e -> e.getGameId() != null)
                    // 2. 只保留今天及之后的比赛 (eventDate >= 今天)
                    .filter(e -> e.getEventDate() != null && e.getEventDate().compareTo(today) >= 0)
                    // 3. 按 gameStartTime 升序排序
                    .sorted((e1, e2) -> {
                        String time1 = getGameStartTime(e1);
                        String time2 = getGameStartTime(e2);
                        if (time1 == null && time2 == null)
                            return 0;
                        if (time1 == null)
                            return 1;
                        if (time2 == null)
                            return -1;
                        return time1.compareTo(time2);
                    })
                    .collect(Collectors.toList());

            log.info("[browseEvents][体育分类: 今日({})及之后单场比赛 {} 个, 已按开赛时间排序]", today, events.size());
        }

        // 标记已导入的事件
        if (!CollectionUtils.isEmpty(events)) {
            for (PolymarketEventDTO event : events) {
                event.setImported(isEventImported(event.getId()));
            }
        }

        log.info("[browseEvents][获取到 {} 个事件]", events.size());
        return events;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long importEvent(String polymarketEventId) {
        log.info("[importEvent][开始导入事件, polymarketEventId={}]", polymarketEventId);

        // 1. 检查是否已导入
        if (isEventImported(polymarketEventId)) {
            throw exception(EVENT_ALREADY_IMPORTED);
        }

        // 2. 从 Polymarket 获取事件详情
        PolymarketEventDTO eventDTO = polymarketApiClient.getEventDetail(polymarketEventId);
        if (eventDTO == null) {
            throw exception(POLYMARKET_EVENT_NOT_FOUND);
        }

        // 3. 检查是否为 Scalar 类型（暂不支持）
        if (isScalarEvent(eventDTO)) {
            throw exception(SCALAR_MARKET_NOT_SUPPORTED);
        }

        // 4. 转换并保存 Event
        PmEventDO event = convertToEventDO(eventDTO);
        pmEventService.createEvent(event);
        log.info("[importEvent][Event 保存成功, id={}, title={}]", event.getId(), event.getTitle());

        // 5. 转换并保存 Markets
        List<PmMarketDO> markets = convertToMarketDOs(eventDTO.getMarkets(), event.getId());
        if (!CollectionUtils.isEmpty(markets)) {
            pmMarketService.createMarketBatch(markets);
            log.info("[importEvent][Markets 保存成功, count={}]", markets.size());
        }

        // 6. 更新 Event 的 market_count
        event.setMarketCount(markets.size());
        pmEventService.updateEvent(event);

        log.info("[importEvent][导入完成, polymarketEventId={}, localEventId={}, markets={}]",
                polymarketEventId, event.getId(), markets.size());

        return event.getId();
    }

    @Override
    public boolean isEventImported(String polymarketEventId) {
        PmEventDO existingEvent = pmEventService.getEventByPolymarketEventId(polymarketEventId);
        return existingEvent != null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addMarketToEvent(String polymarketEventId, String polymarketMarketId,
            String conditionId, String question,
            List<String> outcomes, List<String> tokenIds) {
        log.info("[addMarketToEvent][收到新市场 polymarketEventId={}, polymarketMarketId={}, question={}]",
                polymarketEventId, polymarketMarketId, question);

        // 1. 查找本地事件
        PmEventDO event = pmEventService.getEventByPolymarketEventId(polymarketEventId);
        if (event == null) {
            log.debug("[addMarketToEvent][事件不存在, 忽略 polymarketEventId={}]", polymarketEventId);
            return null;
        }

        // 2. 检查市场是否已存在（去重）
        PmMarketDO existingMarket = pmMarketService.getMarketByPolymarketId(polymarketMarketId);
        if (existingMarket != null) {
            log.debug("[addMarketToEvent][市场已存在, 忽略 polymarketMarketId={}]", polymarketMarketId);
            return null;
        }

        // 3. 创建市场
        PmMarketDO market = new PmMarketDO();
        market.setEventId(event.getId());
        market.setPolymarketId(polymarketMarketId);
        market.setConditionId(conditionId);
        market.setQuestion(question);
        market.setOutcomes(outcomes);
        market.setClobTokenIds(tokenIds);

        // 4. 根据父事件状态确定市场状态
        Integer marketStatus = determineNewMarketStatus(event);
        market.setStatus(marketStatus);

        // 5. 保存市场
        pmMarketService.createMarket(market);
        log.info("[addMarketToEvent][新市场创建成功 localMarketId={}, status={}, eventId={}]",
                market.getId(), marketStatus, event.getId());

        // 6. 更新事件的 marketCount
        Integer currentCount = event.getMarketCount() != null ? event.getMarketCount() : 0;
        event.setMarketCount(currentCount + 1);
        pmEventService.updateEvent(event);

        return market.getId();
    }

    @Override
    public List<PolymarketEventDTO> searchEvents(String keyword, int limit) {
        log.info("[searchEvents][开始搜索 Polymarket 事件, keyword={}, limit={}]", keyword, limit);

        List<PolymarketEventDTO> events = polymarketApiClient.searchEvents(keyword, limit);

        // 标记已导入的事件
        if (!CollectionUtils.isEmpty(events)) {
            for (PolymarketEventDTO event : events) {
                event.setImported(isEventImported(event.getId()));
            }
        }

        log.info("[searchEvents][搜索到 {} 个事件]", events.size());
        return events;
    }

    /**
     * 根据父事件状态确定新市场状态
     * - 父事件已上架 → TRADING (交易中)
     * - 父事件未上架/已下架 → DRAFT (待上架)
     */
    private Integer determineNewMarketStatus(PmEventDO parentEvent) {
        if (EventStatusEnum.PUBLISHED.getStatus().equals(parentEvent.getStatus())) {
            return MarketStatusEnum.TRADING.getStatus();
        }
        return MarketStatusEnum.DRAFT.getStatus();
    }

    /**
     * 检测是否为 Scalar 类型市场
     * Scalar 市场的 outcomes 包含 "Long" 或 "Short"
     */
    private boolean isScalarEvent(PolymarketEventDTO eventDTO) {
        if (eventDTO.getMarkets() == null || eventDTO.getMarkets().isEmpty()) {
            return false;
        }

        for (PolymarketEventDTO.MarketDTO market : eventDTO.getMarkets()) {
            String outcomes = market.getOutcomes();
            if (StringUtils.hasText(outcomes) &&
                    (outcomes.contains("\"Long\"") || outcomes.contains("\"Short\""))) {
                log.warn("[isScalarEvent][检测到 Scalar 市场, marketId={}, outcomes={}]",
                        market.getId(), outcomes);
                return true;
            }
        }
        return false;
    }

    /**
     * 将 PolymarketEventDTO 转换为 PmEventDO
     */
    private PmEventDO convertToEventDO(PolymarketEventDTO dto) {
        PmEventDO event = new PmEventDO();
        event.setPolymarketEventId(dto.getId());
        event.setTicker(dto.getTicker());
        event.setSlug(dto.getSlug());
        event.setTitle(dto.getTitle());
        event.setImageUrl(dto.getImageUrl());
        event.setCategory(extractCategory(dto.getTags()));
        event.setTags(dto.getTags());
        event.setNegRisk(Boolean.TRUE.equals(dto.getNegRisk()));
        event.setStartDate(parseDateTime(dto.getStartDate()));
        event.setEndDate(parseDateTime(dto.getEndDate()));
        event.setStatus(EventStatusEnum.DRAFT.getStatus()); // 待上架
        event.setMarketCount(dto.getMarkets() != null ? dto.getMarkets().size() : 0);

        // 体育专有字段
        event.setSeriesId(dto.getSeriesId());
        event.setSeriesSlug(dto.getSeriesSlug());
        event.setGameId(dto.getGameId());
        event.setHomeTeamName(dto.getHomeTeamName());
        event.setAwayTeamName(dto.getAwayTeamName());
        event.setEventDate(parseDate(dto.getEventDate()));
        event.setEventWeek(dto.getEventWeek());

        return event;
    }

    /**
     * 将 MarketDTO 列表转换为 PmMarketDO 列表
     */
    private List<PmMarketDO> convertToMarketDOs(List<PolymarketEventDTO.MarketDTO> dtos, Long eventId) {
        if (CollectionUtils.isEmpty(dtos)) {
            return Collections.emptyList();
        }

        List<PmMarketDO> markets = new ArrayList<>();
        for (PolymarketEventDTO.MarketDTO dto : dtos) {
            PmMarketDO market = new PmMarketDO();
            market.setEventId(eventId);
            market.setPolymarketId(dto.getId());
            market.setConditionId(dto.getConditionId());
            market.setQuestion(dto.getQuestion());
            market.setGroupItemTitle(dto.getGroupItemTitle());
            market.setOutcomes(parseJsonArray(dto.getOutcomes()));
            market.setClobTokenIds(parseJsonArray(dto.getClobTokenIds()));

            // 根据 Polymarket 状态设置本地状态
            market.setStatus(determineMarketStatus(dto));

            market.setStartDate(parseDateTime(dto.getStartDate()));
            market.setEndDate(parseDateTime(dto.getEndDate()));
            market.setNegRisk(dto.getNegRisk());

            // 体育盘口
            market.setSportsMarketType(dto.getSportsMarketType());
            market.setLine(dto.getLine());
            market.setGameStartTime(parseDateTime(dto.getGameStartTime()));

            markets.add(market);
        }

        return markets;
    }

    /**
     * 根据 Polymarket 市场状态确定本地状态
     * - closed = true → 待结算 (PENDING_SETTLEMENT)
     * - active = false → 已封盘 (SUSPENDED)
     * - acceptingOrders = false → 已封盘 (SUSPENDED)
     * - 其他 → 待上架 (DRAFT)，需管理员手动上架
     */
    private Integer determineMarketStatus(PolymarketEventDTO.MarketDTO dto) {
        // 已关闭的市场 → 已封盘（导入后无法参与交易也无法结算）
        if (Boolean.TRUE.equals(dto.getClosed())) {
            log.info("[determineMarketStatus][市场已关闭, marketId={}, 设置为已封盘]", dto.getId());
            return MarketStatusEnum.SUSPENDED.getStatus();
        }

        // 不活跃或不接单 → 已封盘
        if (Boolean.FALSE.equals(dto.getActive()) || Boolean.FALSE.equals(dto.getAcceptingOrders())) {
            log.info("[determineMarketStatus][市场不活跃或暂停接单, marketId={}, 设置为已封盘]", dto.getId());
            return MarketStatusEnum.SUSPENDED.getStatus();
        }

        // 默认为待上架，需管理员审核后手动上架
        return MarketStatusEnum.DRAFT.getStatus();
    }

    /**
     * 从 tags 中提取主分类
     */
    private String extractCategory(List<Map<String, Object>> tags) {
        if (CollectionUtils.isEmpty(tags)) {
            return null;
        }

        // 优先匹配已知分类
        List<String> knownCategories = java.util.Arrays.asList("politics", "sports", "crypto", "culture", "business");

        for (Map<String, Object> tag : tags) {
            Object slug = tag.get("slug");
            if (slug != null && knownCategories.contains(slug.toString().toLowerCase())) {
                return slug.toString().toLowerCase();
            }
        }

        // 如果没有匹配到已知分类，返回第一个 tag 的 slug
        Object firstSlug = tags.get(0).get("slug");
        return firstSlug != null ? firstSlug.toString() : null;
    }

    /**
     * 解析日期时间字符串
     */
    private LocalDateTime parseDateTime(String dateStr) {
        if (!StringUtils.hasText(dateStr)) {
            return null;
        }

        try {
            // 尝试多种格式
            if (dateStr.contains("T")) {
                // ISO 格式: 2025-12-31T23:30:00Z 或 2025-12-31T23:30:00+00:00
                dateStr = dateStr.replace("Z", "").replace("+00:00", "");
                if (dateStr.contains(".")) {
                    dateStr = dateStr.substring(0, dateStr.indexOf("."));
                }
                return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } else if (dateStr.contains(" ")) {
                // 空格分隔: 2025-12-31 23:30:00
                return LocalDateTime.parse(dateStr.substring(0, 19),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
        } catch (Exception e) {
            log.warn("[parseDateTime][日期解析失败, dateStr={}]", dateStr, e);
        }

        return null;
    }

    /**
     * 解析日期字符串
     */
    private LocalDate parseDate(String dateStr) {
        if (!StringUtils.hasText(dateStr)) {
            return null;
        }

        try {
            return LocalDate.parse(dateStr.substring(0, 10), DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            log.warn("[parseDate][日期解析失败, dateStr={}]", dateStr, e);
            return null;
        }
    }

    /**
     * 解析 JSON 数组字符串为 List
     */
    private List<String> parseJsonArray(String jsonStr) {
        if (!StringUtils.hasText(jsonStr)) {
            return null;
        }

        try {
            return objectMapper.readValue(jsonStr, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            log.warn("[parseJsonArray][JSON 解析失败, jsonStr={}]", jsonStr, e);
            return null;
        }
    }

    /**
     * 获取事件的比赛开始时间 (从第一个 market 中提取 gameStartTime)
     */
    private String getGameStartTime(PolymarketEventDTO event) {
        if (event == null || CollectionUtils.isEmpty(event.getMarkets())) {
            return null;
        }
        return event.getMarkets().get(0).getGameStartTime();
    }

}
