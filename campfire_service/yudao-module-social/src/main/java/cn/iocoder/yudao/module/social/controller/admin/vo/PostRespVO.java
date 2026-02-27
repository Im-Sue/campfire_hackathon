package cn.iocoder.yudao.module.social.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 帖子 Response VO")
@Data
public class PostRespVO {

    @Schema(description = "帖子 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "用户 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long userId;

    @Schema(description = "用户钱包地址", example = "0x1234567890abcdef1234567890abcdef12345678")
    private String userAddress;

    @Schema(description = "帖子内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "今天的预测市场太刺激了！")
    private String content;

    @Schema(description = "图片列表", example = "[\"https://example.com/image1.jpg\"]")
    private List<String> images;

    @Schema(description = "点赞数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer likeCount;

    @Schema(description = "评论数", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Integer commentCount;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

}
