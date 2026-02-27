package cn.iocoder.yudao.module.ai.tool.chat;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.market.dal.dataobject.event.PmEventDO;
import cn.iocoder.yudao.module.market.service.event.PmEventService;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * C端对话工具：搜索预测市场事件
 *
 * 用户可以通过关键词、类别、状态等条件搜索事件
 *
 * @author campfire
 */
@Component("searchEvents")
@Slf4j
public class SearchEventsToolFunction implements Function<SearchEventsToolFunction.Request, SearchEventsToolFunction.Response> {

    @Resource
    private PmEventService eventService;

    @Data
    @JsonClassDescription("搜索预测市场事件，可根据关键词、类别、状态等条件筛选")
    public static class Request {

        @JsonProperty(value = "keyword")
        @JsonPropertyDescription("关键词，用于搜索事件标题")
        private String keyword;

        @JsonProperty(value = "category")
        @JsonPropertyDescription("事件类别，如：体育、政治、娱乐、加密货币等")
        private String category;

        @JsonProperty(value = "status")
        @JsonPropertyDescription("事件状态：1-未开始，2-进行中，3-已结束")
        private Integer status;

        @JsonProperty(value = "limit")
        @JsonPropertyDescription("返回数量限制，默认10，最大20")
        private Integer limit;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class Response {

        private List<EventItem> events;
        private Integer total;
        private String error;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class EventItem {
            private Long id;
            private String title;
            private String category;
            private Integer status;
            private LocalDateTime endDate;
        }

    }

    @Override
    public Response apply(Request request) {
        log.info("[SearchEventsToolFunction] 搜索事件, keyword={}, category={}, status={}", 
                request.getKeyword(), request.getCategory(), request.getStatus());

        try {
            // 限制返回数量
            int limit = request.getLimit() != null ? Math.min(request.getLimit(), 20) : 10;

            // 使用现有的分页查询方法
            PageParam pageParam = new PageParam();
            pageParam.setPageNo(1);
            pageParam.setPageSize(limit);
            
            PageResult<PmEventDO> pageResult = eventService.getEventPage(
                    request.getStatus(), 
                    request.getCategory(), 
                    request.getKeyword(),
                    pageParam);

            List<Response.EventItem> items = pageResult.getList().stream()
                    .map(e -> new Response.EventItem(
                            e.getId(),
                            e.getTitle(),
                            e.getCategory(),
                            e.getStatus(),
                            e.getEndDate()))
                    .collect(Collectors.toList());

            return new Response()
                    .setEvents(items)
                    .setTotal(items.size());

        } catch (Exception e) {
            log.error("[SearchEventsToolFunction] 搜索异常", e);
            return new Response().setError("搜索失败: " + e.getMessage());
        }
    }

}
