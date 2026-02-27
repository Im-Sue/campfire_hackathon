package cn.iocoder.yudao.module.social.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "用户 App - 关注用户响应")
@Data
@NoArgsConstructor
public class AppFollowUserRespVO {

    @Schema(description = "用户ID", required = true)
    private Long userId;

    @Schema(description = "钱包地址（格式化）", example = "0x1234...5678")
    private String userAddress;

    @Schema(description = "用户昵称")
    private String userNickname;

    @Schema(description = "用户头像")
    private String userAvatar;

    @Schema(description = "关注时间", required = true)
    private LocalDateTime followTime;

    /**
     * 简化构造器（用于 Controller 转换）
     */
    public AppFollowUserRespVO(Long userId, LocalDateTime followTime) {
        this.userId = userId;
        this.followTime = followTime;
    }

}
