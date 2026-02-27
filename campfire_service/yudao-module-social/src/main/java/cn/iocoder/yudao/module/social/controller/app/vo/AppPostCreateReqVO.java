package cn.iocoder.yudao.module.social.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "用户 App - 发布帖子 Request VO")
@Data
public class AppPostCreateReqVO {

    @Schema(description = "帖子内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "今天的预测市场太刺激了！")
    @NotBlank(message = "帖子内容不能为空")
    @Size(max = 2000, message = "帖子内容不能超过 2000 字符")
    private String content;

    @Schema(description = "图片列表", example = "[\"https://example.com/image1.jpg\"]")
    @Size(max = 9, message = "最多上传 9 张图片")
    private List<String> images;

}
