package cn.iocoder.yudao.module.market.controller.app.comment;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.market.controller.app.comment.vo.*;
import cn.iocoder.yudao.module.market.convert.comment.EventCommentConvert;
import cn.iocoder.yudao.module.market.dal.dataobject.comment.PmEventCommentDO;
import cn.iocoder.yudao.module.market.dal.mysql.comment.PmEventCommentMapper;
import cn.iocoder.yudao.module.market.service.comment.EventCommentService;
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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 App - 事件评论")
@RestController
@RequestMapping("/event/comment")
@Validated
public class AppEventCommentController {

    @Resource
    private EventCommentService commentService;

    @Resource
    private WalletUserService walletUserService;

    @Resource
    private PmEventCommentMapper commentMapper;

    @GetMapping("/list")
    @Operation(summary = "获取一级评论列表")
    @PermitAll
    public CommonResult<PageResult<AppEventCommentRespVO>> getCommentList(@Valid AppEventCommentPageReqVO reqVO) {
        PageResult<PmEventCommentDO> pageResult = commentService.getCommentPage(reqVO);
        PageResult<AppEventCommentRespVO> result = EventCommentConvert.INSTANCE.convertPage(pageResult);

        // 填充用户信息和点赞状态
        fillCommentInfo(result.getList());

        return success(result);
    }

    @GetMapping("/reply/list")
    @Operation(summary = "获取回复列表")
    @PermitAll
    public CommonResult<PageResult<AppEventCommentRespVO>> getReplyList(@Valid AppEventCommentReplyPageReqVO reqVO) {
        PageResult<PmEventCommentDO> pageResult = commentService.getReplyPage(reqVO);
        PageResult<AppEventCommentRespVO> result = EventCommentConvert.INSTANCE.convertPage(pageResult);

        // 填充用户信息和点赞状态
        fillCommentInfo(result.getList());

        return success(result);
    }

    @PostMapping("/create")
    @Operation(summary = "发表评论")
    public CommonResult<AppEventCommentRespVO> createComment(@Valid @RequestBody AppEventCommentCreateReqVO reqVO) {
        Long userId = getLoginUserId();
        PmEventCommentDO comment = commentService.createComment(userId, reqVO);
        AppEventCommentRespVO respVO = EventCommentConvert.INSTANCE.convert(comment);

        // 填充当前用户信息
        fillSingleUserInfo(respVO, userId);
        respVO.setLiked(false);

        return success(respVO);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除评论")
    @Parameter(name = "id", description = "评论 ID", required = true, example = "1001")
    public CommonResult<AppEventCommentDeleteRespVO> deleteComment(@RequestParam("id") Long id) {
        Long userId = getLoginUserId();
        AppEventCommentDeleteRespVO respVO = commentService.deleteComment(userId, id);
        return success(respVO);
    }

    @PostMapping("/like")
    @Operation(summary = "点赞评论（Toggle 模式）")
    @Parameter(name = "id", description = "评论 ID", required = true, example = "1001")
    public CommonResult<Boolean> likeComment(@RequestParam("id") Long id) {
        Long userId = getLoginUserId();
        boolean liked = commentService.likeComment(userId, id);
        return success(liked); // true=点赞成功, false=取消点赞
    }

    // ========== 私有方法 ==========

    /**
     * 批量填充评论信息（用户信息、点赞状态、回复预览）
     */
    private void fillCommentInfo(List<AppEventCommentRespVO> comments) {
        if (comments == null || comments.isEmpty()) {
            return;
        }

        // 收集所有用户 ID（包括回复的用户）
        Set<Long> userIds = comments.stream()
                .flatMap(c -> {
                    if (c.getReplyUserId() != null) {
                        return java.util.stream.Stream.of(c.getUserId(), c.getReplyUserId());
                    }
                    return java.util.stream.Stream.of(c.getUserId());
                })
                .collect(Collectors.toSet());

        // 批量查询用户信息
        Map<Long, WalletUserDO> userMap = userIds.stream()
                .map(walletUserService::getUser)
                .filter(u -> u != null)
                .collect(Collectors.toMap(WalletUserDO::getId, Function.identity()));

        Long loginUserId = getLoginUserId();

        // 批量查询点赞状态
        Set<Long> likedIds = Collections.emptySet();
        if (loginUserId != null) {
            List<Long> commentIds = comments.stream().map(AppEventCommentRespVO::getId).collect(Collectors.toList());
            likedIds = commentService.getLikedCommentIds(loginUserId, commentIds);
        }

        for (AppEventCommentRespVO comment : comments) {
            // 填充用户信息
            WalletUserDO user = userMap.get(comment.getUserId());
            if (user != null) {
                comment.setUserAddress(formatAddress(user.getWalletAddress()));
                comment.setUserNickname(user.getNickname());
                comment.setUserAvatar(user.getAvatar());
            }

            // 填充被回复用户信息
            if (comment.getReplyUserId() != null) {
                WalletUserDO replyUser = userMap.get(comment.getReplyUserId());
                if (replyUser != null) {
                    comment.setReplyUserAddress(formatAddress(replyUser.getWalletAddress()));
                    comment.setReplyUserNickname(replyUser.getNickname());
                    comment.setReplyUserAvatar(replyUser.getAvatar());
                }
            }

            // 填充点赞状态
            comment.setLiked(likedIds.contains(comment.getId()));

            // 填充回复数量和回复预览（只对一级评论填充）
            if (comment.getParentId() == null || comment.getParentId() == 0L) {
                comment.setReplyCount(commentMapper.selectReplyCountByParentId(comment.getId()));
                // 获取前5条回复作为预览
                if (comment.getReplyCount() != null && comment.getReplyCount() > 0) {
                    List<PmEventCommentDO> topReplies = commentMapper.selectTopReplies(comment.getId(), 5);
                    List<AppEventCommentRespVO> replyVOs = EventCommentConvert.INSTANCE.convertList(topReplies);
                    // 填充回复的用户信息
                    fillReplyInfo(replyVOs, userMap, loginUserId);
                    comment.setReplies(replyVOs);
                }
            } else {
                comment.setReplyCount(0); // 回复不再嵌套
            }
        }
    }

    /**
     * 填充回复信息（简化版，复用 userMap）
     */
    private void fillReplyInfo(List<AppEventCommentRespVO> replies, Map<Long, WalletUserDO> userMap, Long loginUserId) {
        if (replies == null || replies.isEmpty()) {
            return;
        }

        // 收集需要额外查询的用户 ID
        Set<Long> additionalUserIds = new HashSet<>();
        for (AppEventCommentRespVO reply : replies) {
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
        for (AppEventCommentRespVO reply : replies) {
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
                reply.setLiked(commentService.hasLiked(loginUserId, reply.getId()));
            } else {
                reply.setLiked(false);
            }
            reply.setReplyCount(0); // 回复不再嵌套
        }
    }

    /**
     * 填充单个用户信息
     */
    private void fillSingleUserInfo(AppEventCommentRespVO vo, Long userId) {
        WalletUserDO user = walletUserService.getUser(userId);
        if (user != null) {
            vo.setUserAddress(formatAddress(user.getWalletAddress()));
            vo.setUserNickname(user.getNickname());
            vo.setUserAvatar(user.getAvatar());
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
