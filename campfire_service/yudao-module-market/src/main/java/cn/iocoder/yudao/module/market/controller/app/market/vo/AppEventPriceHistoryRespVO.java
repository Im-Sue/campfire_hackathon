package cn.iocoder.yudao.module.market.controller.app.market.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "用户 App - 事件K线历史 Response VO")
@Data
public class AppEventPriceHistoryRespVO {

    @Schema(description = "事件 ID", example = "123")
    private Long eventId;

    @Schema(description = "各市场的价格历史")
    private List<AppMarketPriceHistoryVO> markets;

}
