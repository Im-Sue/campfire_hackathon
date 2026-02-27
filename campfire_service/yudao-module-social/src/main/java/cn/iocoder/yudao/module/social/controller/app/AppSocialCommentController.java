package cn.iocoder.yudao.module.social.controller.app;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.social.controller.app.vo.*;
import cn.iocoder.yudao.module.social.convert.SocialCommentConvert;
import cn.iocoder.yudao.module.social.dal.dataobject.SocialCommentDO;
import cn.iocoder.yudao.module.social.dal.mysql.SocialCommentMapper;
import cn.iocoder.yudao.module.social.service.SocialCommentService;
import cn.iocoder.yudao.module.wallet.dal.dataobject.WalletUserDO;
import cn.iocoder.yudao.module.wallet.service.WalletUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * 用户 App - 社交评论
 */
@Tag(name = "用户 App - 社交评论")
@RestController
@RequestMapping("/social/comment")
@Validated
public class AppSocialCommentController {

    @Resource
    private SocialCommentService commentService;

    @Resource
    private SocialCommentMapper commentMapper;

    @Resource
    private WalletUserService walletUserService;

    @PostMapping("/create")
    @Operation(summary = "发表评论")
    public CommonResult<AppCommentRespVO> createComment(@Valid @RequestBody AppCommentCreateReqVO reqVO) {
        SocialCommentDO comment = commentService.createComment(getLoginUserId(), reqVO);
        AppCommentRespVO result = SocialCommentConvert.INSTANCE.convertApp(comment);
        // 填充当前用户信息
        fillCommentInfo(Collections.singletonList(result));
        return success(result);
    }

    @GetMapping("/list")
    @Operation(summary = "获取帖子一级评论列表")
    @PermitAll
    public CommonResult<PageResult<AppCommentRespVO>> getCommentList(@Valid AppCommentPageReqVO reqVO) {
        PageResult<SocialCommentDO> pageResult = commentService.getCommentPage(reqVO);
        PageResult<AppCommentRespVO> result = SocialCommentConvert.INSTANCE.convertAppPage(pageResult);
        // 填充用户信息和点赞状态
        fillCommentInfo(result.getList());
        return success(result);
    }

    @GetMapping("/reply/list")
    @Operation(summary = "获取评论的回复列表")
    @PermitAll
    public CommonResult<PageResult<AppCommentRespVO>> getReplyList(@Valid AppReplyPageReqVO reqVO) {
        PageResult<SocialCommentDO> pageResult = commentService.getReplyPage(reqVO);
        PageResult<AppCommentRespVO> result = SocialCommentConvert.INSTANCE.convertAppPage(pageResult);
        // 填充用户信息和点赞状态
        fillCommentInfo(result.getList());
        return success(result);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除评论（仅自己）")
    @Parameter(name = "id", description = "评论 ID", required = true)
    public CommonResult<AppCommentDeleteRespVO> deleteComment(@RequestParam("id") Long id) {
        AppCommentDeleteRespVO respVO = commentService.deleteComment(getLoginUserId(), id);
        return success(respVO);
    }

    @PostMapping("/like")
    @Operation(summary = "点赞评论")
    @Parameter(name = "id", description = "评论 ID", required = true)
    public CommonResult<Boolean> likeComment(@RequestParam("id") Long id) {
        commentService.likeComment(getLoginUserId(), id);
        return success(true);
    }

    @PostMapping("/unlike")
    @Operation(summary = "取消点赞评论")
    @Parameter(name = "id", description = "评论 ID", required = true)
    public CommonResult<Boolean> unlikeComment(@RequestParam("id") Long id) {
        commentService.unlikeComment(getLoginUserId(), id);
        return success(true);
    }

    // ========== 私有方法 ==========

    /**
     * 批量填充评论信息（用户信息、点赞状态）
     */
    private void fillCommentInfo(List<AppCommentRespVO> comments) {
        if (comments == null || comments.isEmpty()) {
            return;
        }
        // 获取所有用户 ID（包括回复的用户）
        Set<Long> userIds = comments.stream()
                .flatMap(c -> {
                    if (c.getReplyUserId() != null) {
                        return java.util.stream.Stream.of(c.getUserId(), c.getReplyUserId());
                    }
                    return java.util.stream.Stream.of(c.getUserId());
                })
                .collect(Collectors.toSet());

        Map<Long, WalletUserDO> userMap = userIds.stream()
                .map(walletUserService::getUser)
                .filter(u -> u != null)
                .collect(Collectors.toMap(WalletUserDO::getId, Function.identity()));

        Long loginUserId = getLoginUserId();

        for (AppCommentRespVO comment : comments) {
            // 填充用户信息
            WalletUserDO user = userMap.get(comment.getUserId());
            if (user != null) {
                comment.setUserAddress(formatAddress(user.getWalletAddress()));
                comment.setUserNickname(user.getNickname());
                comment.setUserAvatar(user.getAvatar());
            }
            // 填充回复用户信息
            if (comment.getReplyUserId() != null) {
                WalletUserDO replyUser = userMap.get(comment.getReplyUserId());
                if (replyUser != null) {
                    comment.setReplyUserAddress(formatAddress(replyUser.getWalletAddress()));
                    comment.setReplyUserNickname(replyUser.getNickname());
                    comment.setReplyUserAvatar(replyUser.getAvatar());
                }
            }
            // 填充点赞状态
            if (loginUserId != null) {
                comment.setLiked(commentService.hasLikedComment(loginUserId, comment.getId()));
            } else {
                comment.setLiked(false);
            }
            // 填充回复数量和回复预览（只对一级评论填充）
            if (comment.getParentId() == null || comment.getParentId() == 0L) {
                comment.setReplyCount(commentMapper.selectReplyCountByParentId(comment.getId()));
                // 获取前5条回复作为预览
                if (comment.getReplyCount() > 0) {
                    List<SocialCommentDO> topReplies = commentMapper.selectTopReplies(comment.getId(), 5);
                    List<AppCommentRespVO> replyVOs = SocialCommentConvert.INSTANCE.convertAppList(topReplies);
                    // 填充回复的用户信息
                    fillReplyInfo(replyVOs, userMap, loginUserId);
                    comment.setReplies(replyVOs);
                }
            } else {
                comment.setReplyCount(0); // 回复不有子回复
            }
        }
    }

    /**
     * 填充回复信息（简化版，复用 userMap）
     */
    private void fillReplyInfo(List<AppCommentRespVO> replies, Map<Long, WalletUserDO> userMap, Long loginUserId) {
        if (replies == null || replies.isEmpty()) {
            return;
        }
        // 收集需要额外查询的用户 ID
        Set<Long> additionalUserIds = new java.util.HashSet<>();
        for (AppCommentRespVO reply : replies) {
            if (!userMap.containsKey(reply.getUserId())) {
                additionalUserIds.add(reply.getUserId());
            }
            if (reply.getReplyUserId() != null && !userMap.containsKey(reply.getReplyUserId())) {
                additionalUserIds.add(reply.getReplyUserId());
            }
        }
        // 补充查询用户信息
        for (Long userId : additionalUserIds) {
            WalletUserDO user = walletUserService.getUser(userId);
            if (user != null) {
                userMap.put(user.getId(), user);
            }
        }
        // 填充信息
        for (AppCommentRespVO reply : replies) {
            WalletUserDO user = userMap.get(reply.getUserId());
            if (user != null) {
                reply.setUserAddress(formatAddress(user.getWalletAddress()));
                reply.setUserNickname(user.getNickname());
                reply.setUserAvatar(user.getAvatar());
            }
            if (reply.getReplyUserId() != null) {
                WalletUserDO replyUser = userMap.get(reply.getReplyUserId());
                if (replyUser != null) {
                    reply.setReplyUserAddress(formatAddress(replyUser.getWalletAddress()));
                    reply.setReplyUserNickname(replyUser.getNickname());
                    reply.setReplyUserAvatar(replyUser.getAvatar());
                }
            }
            if (loginUserId != null) {
                reply.setLiked(commentService.hasLikedComment(loginUserId, reply.getId()));
            } else {
                reply.setLiked(false);
            }
            reply.setReplyCount(0); // 回复不再嵌套
        }
    }

    /**
     * 格式化钱包地址 (0x1234...5678)
     */
    private String formatAddress(String address) {
        if (address == null || address.length() < 10) {
            return address;
        }
        return address.substring(0, 6) + "..." + address.substring(address.length() - 4);
    }

}
