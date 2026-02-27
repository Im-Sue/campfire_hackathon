package cn.iocoder.yudao.module.ai.tool.chat;

import cn.iocoder.yudao.module.market.dal.dataobject.position.PmPositionDO;
import cn.iocoder.yudao.module.market.service.position.PmPositionService;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * C端对话工具：获取用户持仓
 *
 * @author campfire
 */
@Component("getUserPortfolio")
@Slf4j
public class GetUserPortfolioToolFunction implements Function<GetUserPortfolioToolFunction.Request, GetUserPortfolioToolFunction.Response> {

    @Resource
    private PmPositionService positionService;

    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();

    public static void setCurrentUserId(Long userId) {
        CURRENT_USER_ID.set(userId);
    }

    public static void clearCurrentUserId() {
        CURRENT_USER_ID.remove();
    }

    @Data
    @JsonClassDescription("获取当前用户的持仓情况，包括持有的市场头寸")
    public static class Request {

        @JsonProperty(value = "marketId")
        @JsonPropertyDescription("市场ID，可选，用于筛选特定市场的持仓")
        private Long marketId;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class Response {

        private List<PositionItem> positions;
        private Integer total;
        private String error;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class PositionItem {
            private Long marketId;
            private String outcome;
            private BigDecimal quantity;
            private BigDecimal avgPrice;
            private Long totalCost;
        }

    }

    @Override
    public Response apply(Request request) {
        Long userId = CURRENT_USER_ID.get();
        log.info("[GetUserPortfolioToolFunction] 获取用户持仓, userId={}, marketId={}", 
                userId, request.getMarketId());

        if (userId == null) {
            return new Response().setError("无法获取用户信息");
        }

        try {
            List<PmPositionDO> positions = positionService.getPositionsByUserId(userId);
            
            if (request.getMarketId() != null) {
                positions = positions.stream()
                        .filter(p -> request.getMarketId().equals(p.getMarketId()))
                        .collect(Collectors.toList());
            }

            List<Response.PositionItem> items = positions.stream()
                    .map(p -> new Response.PositionItem(
                            p.getMarketId(),
                            p.getOutcome(),
                            p.getQuantity(),
                            p.getAvgPrice(),
                            p.getTotalCost()))
                    .collect(Collectors.toList());

            return new Response()
                    .setPositions(items)
                    .setTotal(items.size());

        } catch (Exception e) {
            log.error("[GetUserPortfolioToolFunction] 查询异常", e);
            return new Response().setError("查询失败: " + e.getMessage());
        }
    }

}
