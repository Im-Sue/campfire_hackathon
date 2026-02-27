package cn.iocoder.yudao.module.ai.tool.agent;

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
import java.util.function.Function;

/**
 * 工具：获取预测市场事件的详细信息
 *
 * @author campfire
 */
@Component("getEventInfo")
@Slf4j
public class GetEventInfoToolFunction implements Function<GetEventInfoToolFunction.Request, GetEventInfoToolFunction.Response> {

    @Resource
    private PmEventService eventService;

    @Data
    @JsonClassDescription("获取预测市场事件的详细信息，包括标题、描述、截止时间等")
    public static class Request {

        @JsonProperty(required = true, value = "eventId")
        @JsonPropertyDescription("事件ID")
        private Long eventId;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class Response {

        private Long id;
        private String title;
        private String imageUrl;
        private String category;
        private Integer status;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private String error;

    }

    @Override
    public Response apply(Request request) {
        log.info("[GetEventInfoToolFunction] 获取事件信息, eventId={}", request.getEventId());

        PmEventDO event = eventService.getEvent(request.getEventId());
        if (event == null) {
            return new Response().setError("事件不存在");
        }

        return new Response()
                .setId(event.getId())
                .setTitle(event.getTitle())
                .setImageUrl(event.getImageUrl())
                .setCategory(event.getCategory())
                .setStatus(event.getStatus())
                .setStartDate(event.getStartDate())
                .setEndDate(event.getEndDate());
    }

}
