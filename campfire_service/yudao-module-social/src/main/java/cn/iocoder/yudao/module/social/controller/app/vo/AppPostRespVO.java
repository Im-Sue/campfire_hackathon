package cn.iocoder.yudao.module.social.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "用户 App - 帖子 Response VO")
@Data
public class AppPostRespVO {

    @Schema(description = "帖子 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "用户 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long userId;

    @Schema(description = "用户钱包地址", example = "0x1234...5678")
    private String userAddress;

    @Schema(description = "用户昵称", example = "张三")
    private String userNickname;

    @Schema(description = "用户头像URL", example = "https://example.com/avatar.jpg")
    private String userAvatar;

    @Schema(description = "帖子内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "今天的预测市场太刺激了！")
    private String content;

    @Schema(description = "图片列表", example = "[\"https://example.com/image1.jpg\"]")
    private List<String> images;

    @Schema(description = "点赞数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer likeCount;

    @Schema(description = "评论数", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Integer commentCount;

    @Schema(description = "热度分数", example = "100")
    private Integer heatScore;

    @Schema(description = "是否已点赞", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean liked;

    @Schema(description = "是否已关注发帖人", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean followed;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
