package cn.iocoder.yudao.module.task.controller.app.vo;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户 App - 邀请信息 Response VO
 */
@Schema(description = "用户 App - 邀请信息 Response VO")
@Data
public class AppInviteInfoRespVO {

    @Schema(description = "邀请码", example = "ABC12345")
    private String inviteCode;

    @Schema(description = "完整邀请链接", example = "https://xxx.com?inviteCode=ABC12345")
    private String inviteUrl;

    @Schema(description = "总邀请人数", example = "15")
    private Integer inviteCount;

    @Schema(description = "邀请任务可领取积分（包含历史未领取）", example = "300")
    private Long todayClaimablePoints;

    @Schema(description = "被邀请人列表（分页）")
    private PageResult<InviteeVO> invitees;

    /**
     * 被邀请人信息
     */
    @Data
    public static class InviteeVO {

        @Schema(description = "用户ID", example = "123")
        private Long userId;

        @Schema(description = "昵称", example = "Campfire_abc123")
        private String nickname;

        @Schema(description = "头像", example = "https://xxx.com/avatar.jpg")
        private String avatar;

        @Schema(description = "注册时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime registerTime;
    }

}
