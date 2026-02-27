package cn.iocoder.yudao.module.ai.controller.app.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 互动统计 Response VO")
@Data
public class AppInteractionStatsRespVO {

    @Schema(description = "目标类型 1-房间消息 2-事件评论", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer targetType;

    @Schema(description = "目标ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Long targetId;

    @Schema(description = "鲜花数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "128")
    private Integer flowerCount;

    @Schema(description = "鸡蛋数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "32")
    private Integer eggCount;

    @Schema(description = "评论数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "15")
    private Integer commentCount;

    @Schema(description = "当前用户是否已送鲜花", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean hasFlower;

    @Schema(description = "当前用户是否已砸鸡蛋", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean hasEgg;

}
