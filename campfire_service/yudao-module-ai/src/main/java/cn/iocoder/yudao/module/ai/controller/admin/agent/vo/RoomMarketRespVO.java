package cn.iocoder.yudao.module.ai.controller.admin.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 房间市场价格 Response VO")
@Data
public class RoomMarketRespVO {

    @Schema(description = "市场ID", example = "1")
    private Long id;

    @Schema(description = "市场问题", example = "特朗普会赢得2024年大选吗?")
    private String question;

    @Schema(description = "选项列表（包含价格信息）")
    private List<MarketOutcomeVO> outcomes;

}
