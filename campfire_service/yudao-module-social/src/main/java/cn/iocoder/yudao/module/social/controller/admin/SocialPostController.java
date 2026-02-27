package cn.iocoder.yudao.module.social.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.social.controller.admin.vo.*;
import cn.iocoder.yudao.module.social.convert.SocialPostConvert;
import cn.iocoder.yudao.module.social.dal.dataobject.SocialPostDO;
import cn.iocoder.yudao.module.social.service.SocialPostService;
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
 * 管理后台 - 社交帖子
 */
@Tag(name = "管理后台 - 社交帖子")
@RestController
@RequestMapping("/social/post")
@Validated
public class SocialPostController {

    @Resource
    private SocialPostService postService;

    @Resource
    private WalletUserService walletUserService;

    @GetMapping("/page")
    @Operation(summary = "获取帖子分页")
    @PreAuthorize("@ss.hasPermission('social:post:query')")
    public CommonResult<PageResult<PostRespVO>> getPostPage(@Valid PostPageReqVO reqVO) {
        PageResult<SocialPostDO> pageResult = postService.getPostPage(reqVO);
        PageResult<PostRespVO> result = SocialPostConvert.INSTANCE.convertPage(pageResult);
        // 填充用户信息
        fillUserInfo(result.getList());
        return success(result);
    }

    @GetMapping("/get")
    @Operation(summary = "获取帖子详情")
    @Parameter(name = "id", description = "帖子 ID", required = true)
    @PreAuthorize("@ss.hasPermission('social:post:query')")
    public CommonResult<PostRespVO> getPost(@RequestParam("id") Long id) {
        SocialPostDO post = postService.getPost(id);
        PostRespVO result = SocialPostConvert.INSTANCE.convert(post);
        if (result != null) {
            // 填充用户信息
            WalletUserDO user = walletUserService.getUser(result.getUserId());
            if (user != null) {
                result.setUserAddress(user.getWalletAddress());
            }
        }
        return success(result);
    }

    @PostMapping("/audit")
    @Operation(summary = "审核帖子")
    @PreAuthorize("@ss.hasPermission('social:post:audit')")
    public CommonResult<Boolean> auditPost(@Valid @RequestBody PostAuditReqVO reqVO) {
        postService.auditPost(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除帖子")
    @Parameter(name = "id", description = "帖子 ID", required = true)
    @PreAuthorize("@ss.hasPermission('social:post:delete')")
    public CommonResult<Boolean> deletePost(@RequestParam("id") Long id) {
        postService.deletePostByAdmin(id);
        return success(true);
    }

    @DeleteMapping("/batch-delete")
    @Operation(summary = "批量删除帖子")
    @PreAuthorize("@ss.hasPermission('social:post:delete')")
    public CommonResult<Boolean> batchDeletePost(@RequestBody List<Long> ids) {
        postService.batchDeletePostByAdmin(ids);
        return success(true);
    }

    // ========== 私有方法 ==========

    /**
     * 批量填充用户信息
     */
    private void fillUserInfo(List<PostRespVO> posts) {
        if (posts == null || posts.isEmpty()) {
            return;
        }
        Set<Long> userIds = posts.stream().map(PostRespVO::getUserId).collect(Collectors.toSet());
        Map<Long, WalletUserDO> userMap = userIds.stream()
                .map(walletUserService::getUser)
                .filter(u -> u != null)
                .collect(Collectors.toMap(WalletUserDO::getId, Function.identity()));

        for (PostRespVO post : posts) {
            WalletUserDO user = userMap.get(post.getUserId());
            if (user != null) {
                post.setUserAddress(user.getWalletAddress());
            }
        }
    }

}
