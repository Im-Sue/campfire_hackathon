package cn.iocoder.yudao.module.social.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.social.controller.admin.vo.*;
import cn.iocoder.yudao.module.social.convert.SocialCommentConvert;
import cn.iocoder.yudao.module.social.dal.dataobject.SocialCommentDO;
import cn.iocoder.yudao.module.social.service.SocialCommentService;
import cn.iocoder.yudao.module.wallet.dal.dataobject.WalletUserDO;
import cn.iocoder.yudao.module.wallet.service.WalletUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 社交评论
 */
@Tag(name = "管理后台 - 社交评论")
@RestController
@RequestMapping("/social/comment")
@Validated
public class SocialCommentController {

    @Resource
    private SocialCommentService commentService;

    @Resource
    private WalletUserService walletUserService;

    @GetMapping("/page")
    @Operation(summary = "获取评论分页")
    @PreAuthorize("@ss.hasPermission('social:comment:query')")
    public CommonResult<PageResult<CommentRespVO>> getCommentPage(@Valid CommentPageReqVO reqVO) {
        PageResult<SocialCommentDO> pageResult = commentService.getCommentPage(reqVO);
        PageResult<CommentRespVO> result = SocialCommentConvert.INSTANCE.convertPage(pageResult);
        // 填充用户信息
        fillUserInfo(result.getList());
        return success(result);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除评论")
    @Parameter(name = "id", description = "评论 ID", required = true)
    @PreAuthorize("@ss.hasPermission('social:comment:delete')")
    public CommonResult<Boolean> deleteComment(@RequestParam("id") Long id) {
        commentService.deleteCommentByAdmin(id);
        return success(true);
    }

    @DeleteMapping("/batch-delete")
    @Operation(summary = "批量删除评论")
    @PreAuthorize("@ss.hasPermission('social:comment:delete')")
    public CommonResult<Boolean> batchDeleteComment(@RequestBody List<Long> ids) {
        commentService.batchDeleteCommentByAdmin(ids);
        return success(true);
    }

    // ========== 私有方法 ==========

    /**
     * 批量填充用户信息
     */
    private void fillUserInfo(List<CommentRespVO> comments) {
        if (comments == null || comments.isEmpty()) {
            return;
        }
        Set<Long> userIds = comments.stream().map(CommentRespVO::getUserId).collect(Collectors.toSet());
        Map<Long, WalletUserDO> userMap = userIds.stream()
                .map(walletUserService::getUser)
                .filter(u -> u != null)
                .collect(Collectors.toMap(WalletUserDO::getId, Function.identity()));

        for (CommentRespVO comment : comments) {
            WalletUserDO user = userMap.get(comment.getUserId());
            if (user != null) {
                comment.setUserAddress(user.getWalletAddress());
            }
        }
    }

}
