package cn.iocoder.yudao.module.market.controller.app.market.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "用户 App - 单个市场K线历史 VO")
@Data
public class AppMarketPriceHistoryVO {

    @Schema(description = "市场 ID", example = "456")
    private Long marketId;

    @Schema(description = "Polymarket 市场 ID", example = "529044")
    private String polymarketId;

    @Schema(description = "市场问题", example = "50+ bps decrease")
    private String question;

    @Schema(description = "是否启用订单簿（有订单簿才有K线）", example = "true")
    private Boolean enableOrderBook;

    @Schema(description = "价格历史 [{t: 时间戳, p: 价格}, ...]")
    private List<Map<String, Object>> priceHistory;

}
