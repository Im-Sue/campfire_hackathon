package cn.iocoder.yudao.module.social.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "用户 App - 用户主页信息响应")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppUserProfileRespVO {

    @Schema(description = "用户ID", required = true)
    private Long userId;

    @Schema(description = "钱包地址（完整）", example = "0x1234567890abcdef1234567890abcdef12345678")
    private String walletAddress;

    @Schema(description = "钱包地址（格式化）", example = "0x1234...5678")
    private String walletAddressShort;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "个人简介")
    private String bio;

    @Schema(description = "关注数", required = true)
    private Long followingCount;

    @Schema(description = "粉丝数", required = true)
    private Long followersCount;

    @Schema(description = "帖子数", required = true)
    private Integer postCount;

    @Schema(description = "当前登录用户是否已关注（未登录返回 false）", required = true)
    private Boolean followed;

}
