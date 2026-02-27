package cn.iocoder.yudao.module.market.controller.app.event;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.market.controller.app.event.vo.AppEventRespVO;
import cn.iocoder.yudao.module.market.controller.app.event.vo.AppMarketSimpleVO;
import cn.iocoder.yudao.module.market.convert.event.PmEventConvert;
import cn.iocoder.yudao.module.market.convert.market.PmMarketConvert;
import cn.iocoder.yudao.module.market.dal.dataobject.event.PmEventDO;
import cn.iocoder.yudao.module.market.dal.dataobject.market.PmMarketDO;
import cn.iocoder.yudao.module.market.service.event.EventMarketFilterService;
import cn.iocoder.yudao.module.market.service.event.PmEventService;
import cn.iocoder.yudao.module.market.service.market.PmMarketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 预测市场事件")
@RestController
@RequestMapping("/app-market/event")
@Validated
@Slf4j
public class AppPmEventController {

    @Resource
    private PmEventService pmEventService;

    @Resource
    private PmMarketService pmMarketService;

    @Resource
    private EventMarketFilterService eventMarketFilterService;

    @GetMapping("/list")
    @Operation(summary = "获取已上架事件列表")
    @PermitAll
    public CommonResult<List<AppEventRespVO>> getEventList(
            @RequestParam(required = false) @Parameter(description = "分类") String category) {
        List<PmEventDO> events;
        if (category != null && !category.isEmpty()) {
            events = pmEventService.getPublishedEventsByCategory(category);
        } else {
            events = pmEventService.getPublishedEvents();
        }
        List<AppEventRespVO> result = PmEventConvert.INSTANCE.convertToAppList(events);

        // 批量并发获取有效市场ID，然后填充
        fillEventMarketsBatch(result, events);

        return success(result);
    }

    @GetMapping("/page")
    @Operation(summary = "分页获取已上架事件")
    @PermitAll
    public CommonResult<PageResult<AppEventRespVO>> getEventPage(
            @RequestParam(required = false) @Parameter(description = "搜索关键词") String title,
            @RequestParam(required = false) @Parameter(description = "分类") String category,
            PageParam pageParam) {
        PageResult<PmEventDO> pageResult;

        if (title != null && !title.trim().isEmpty()) {
            pageResult = pmEventService.getEventPage(1, category, title, pageParam);
        } else {
            pageResult = pmEventService.getActiveEventPage(category, pageParam);
        }

        PageResult<AppEventRespVO> result = PmEventConvert.INSTANCE.convertToAppPage(pageResult);
        List<PmEventDO> eventList = pageResult.getList();

        // 批量并发获取有效市场ID，然后填充
        fillEventMarketsBatch(result.getList(), eventList);

        return success(result);
    }

    @GetMapping("/get")
    @Operation(summary = "获取事件详情")
    @PermitAll
    @Parameter(name = "id", description = "事件编号", required = true)
    public CommonResult<AppEventRespVO> getEvent(@RequestParam("id") Long id) {
        PmEventDO event = pmEventService.getEvent(id);
        AppEventRespVO result = PmEventConvert.INSTANCE.convertToApp(event);
        // 单个事件直接调用
        fillEventMarkets(result, event, null);
        return success(result);
    }

    /**
     * 批量填充多个 Event 的 Markets（并发调用 Polymarket API）
     */
    private void fillEventMarketsBatch(List<AppEventRespVO> resultVOs, List<PmEventDO> events) {
        if (resultVOs == null || resultVOs.isEmpty()) {
            return;
        }

        // 1. 收集所有 polymarketEventId
        List<String> polymarketIds = events.stream()
                .map(PmEventDO::getPolymarketEventId)
                .filter(id -> id != null && !id.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        // 2. 批量并发获取有效市场ID
        Map<String, List<String>> validMarketIdsMap = Collections.emptyMap();
        if (!polymarketIds.isEmpty()) {
            try {
                long start = System.currentTimeMillis();
                validMarketIdsMap = eventMarketFilterService.batchGetValidMarketIds(polymarketIds);
                log.info("[fillEventMarketsBatch][批量获取完成 events={}, 耗时={}ms]",
                        polymarketIds.size(), System.currentTimeMillis() - start);
            } catch (Exception e) {
                log.error("[fillEventMarketsBatch][批量获取失败]", e);
            }
        }

        // 3. 批量获取事件交易量
        Map<String, BigDecimal> volumeMap = eventMarketFilterService.batchGetEventVolumes(polymarketIds);

        // 4. 遍历填充每个 Event
        for (int i = 0; i < resultVOs.size(); i++) {
            AppEventRespVO eventVO = resultVOs.get(i);
            PmEventDO event = events.get(i);
            String polymarketId = event.getPolymarketEventId();
            List<String> validIds = polymarketId != null ? validMarketIdsMap.get(polymarketId) : null;
            
            // 设置交易量
            if (polymarketId != null && volumeMap.containsKey(polymarketId)) {
                eventVO.setVolume(volumeMap.get(polymarketId).toPlainString());
            }
            
            fillEventMarkets(eventVO, event, validIds);
        }
    }

    /**
     * 填充单个 Event 的 Markets 列表及价格
     * 
     * @param validMarketIds 预先获取的有效市场ID（如果为null则重新获取）
     */
    private void fillEventMarkets(AppEventRespVO eventVO, PmEventDO event, List<String> validMarketIds) {
        if (event == null) {
            eventVO.setMarkets(new ArrayList<>());
            return;
        }

        Long eventId = event.getId();
        String polymarketEventId = event.getPolymarketEventId();

        // 1. 获取本地所有市场
        List<PmMarketDO> allMarkets = pmMarketService.getMarketsByEventId(eventId);

        // 2. 如果没有预先获取有效市场ID，则单独获取
        if (validMarketIds == null && polymarketEventId != null && !polymarketEventId.isEmpty()) {
            try {
                validMarketIds = eventMarketFilterService.getValidMarketIds(polymarketEventId);
            } catch (Exception e) {
                log.warn("[fillEventMarkets][获取有效市场ID失败 eventId={}]", eventId, e);
            }
        }

        // 3. 过滤市场
        List<PmMarketDO> filteredMarkets;
        if (validMarketIds != null && !validMarketIds.isEmpty()) {
            // 转为 Set 提高 contains 查询效率
            final Set<String> validIdSet = new HashSet<>(validMarketIds);
            filteredMarkets = allMarkets.stream()
                    .filter(m -> m.getPolymarketId() != null && validIdSet.contains(m.getPolymarketId()))
                    .collect(Collectors.toList());
            log.debug("[fillEventMarkets][eventId={}, 总数={}, 过滤后={}]",
                    eventId, allMarkets.size(), filteredMarkets.size());
        } else {
            filteredMarkets = allMarkets;
        }

        // 4. 如果是体育赛事 (gameId != null)，从第一个市场提取 gameStartTime
        if (event.getGameId() != null && !filteredMarkets.isEmpty()) {
            eventVO.setGameStartTime(filteredMarkets.get(0).getGameStartTime());
        }

        // 5. 转换并填充价格
        List<AppMarketSimpleVO> marketVOs = PmMarketConvert.INSTANCE.convertToAppSimpleList(filteredMarkets);
        for (int i = 0; i < marketVOs.size(); i++) {
            AppMarketSimpleVO marketVO = marketVOs.get(i);
            PmMarketDO marketDO = filteredMarkets.get(i);
            try {
                Map<String, BigDecimal> prices = pmMarketService.getMarketPrices(marketDO.getId());
                marketVO.setOutcomePrices(prices);
            } catch (Exception e) {
                log.warn("[fillEventMarkets][获取价格失败 marketId={}]", marketDO.getId(), e);
                marketVO.setOutcomePrices(new HashMap<>());
            }
        }
        eventVO.setMarkets(marketVOs);
    }

}
