package cn.iocoder.yudao.module.social.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "用户 App - 关注数量响应")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppFollowCountRespVO {

    @Schema(description = "关注数", required = true)
    private Long followingCount;

    @Schema(description = "粉丝数", required = true)
    private Long followersCount;

}
