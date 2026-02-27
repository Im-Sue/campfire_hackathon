package cn.iocoder.yudao.module.social.controller.app.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户 App - 互动记录响应（增强版）")
@Data
public class AppActivityRespVO {

    // ===== 基础信息 =====
    @Schema(description = "记录ID", required = true)
    private Long id;

    @Schema(description = "互动类型: 1关注 2点赞帖子 3点赞评论 4评论 5回复", required = true)
    private Integer type;

    @Schema(description = "互动类型名称: 关注/点赞帖子/点赞评论/评论/回复")
    private String typeName;

    @Schema(description = "创建时间", required = true)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    // ===== 发起者信息 =====
    @Schema(description = "发起者用户ID", required = true)
    private Long actorUserId;

    @Schema(description = "发起者钱包地址（格式化）")
    private String actorUserAddress;

    @Schema(description = "发起者昵称")
    private String actorNickname;

    @Schema(description = "发起者头像")
    private String actorAvatar;

    // ===== 目标用户信息（关注/评论/回复场景） =====
    @Schema(description = "目标用户ID")
    private Long targetUserId;

    @Schema(description = "目标用户钱包地址（格式化）")
    private String targetUserAddress;

    @Schema(description = "目标用户昵称")
    private String targetNickname;

    @Schema(description = "目标用户头像")
    private String targetAvatar;

    // ===== 目标内容信息（帖子/评论场景） =====
    @Schema(description = "目标ID（帖子/评论ID）")
    private Long targetId;

    @Schema(description = "关联帖子ID（用于跳转详情）")
    private Long postId;

    @Schema(description = "目标内容摘要（帖子/评论内容前50字）")
    private String contentSummary;

    @Schema(description = "目标内容的第一张图片URL（如有）")
    private String firstImage;

}
