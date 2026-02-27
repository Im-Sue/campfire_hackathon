package cn.iocoder.yudao.module.social.controller.app;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.social.controller.app.vo.*;
import cn.iocoder.yudao.module.social.convert.SocialPostConvert;
import cn.iocoder.yudao.module.social.dal.dataobject.SocialPostDO;
import cn.iocoder.yudao.module.social.service.SocialFollowService;
import cn.iocoder.yudao.module.social.service.SocialPostService;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * 用户 App - 社交帖子
 */
@Tag(name = "用户 App - 社交帖子")
@RestController
@RequestMapping("/social/post")
@Validated
public class AppSocialPostController {

    @Resource
    private SocialPostService postService;

    @Resource
    private SocialFollowService followService;

    @Resource
    private WalletUserService walletUserService;

    @PostMapping("/create")
    @Operation(summary = "发布帖子")
    public CommonResult<AppPostRespVO> createPost(@Valid @RequestBody AppPostCreateReqVO reqVO) {
        SocialPostDO post = postService.createPost(getLoginUserId(), reqVO);
        AppPostRespVO result = SocialPostConvert.INSTANCE.convertApp(post);
        // 填充当前用户信息
        fillPostInfo(result);
        return success(result);
    }

    @GetMapping("/list")
    @Operation(summary = "获取帖子列表（广场）")
    @PermitAll
    public CommonResult<PageResult<AppPostRespVO>> getPostList(@Valid AppPostPageReqVO reqVO) {
        PageResult<SocialPostDO> pageResult = postService.getPostPage(reqVO);
        PageResult<AppPostRespVO> result = SocialPostConvert.INSTANCE.convertAppPage(pageResult);
        // 填充用户信息和点赞状态
        fillPostInfo(result.getList());
        return success(result);
    }

    @GetMapping("/hot")
    @Operation(summary = "获取热门帖子排行榜")
    @PermitAll
    public CommonResult<PageResult<AppPostRespVO>> getHotPostList(@Valid AppPostPageReqVO reqVO) {
        PageResult<SocialPostDO> pageResult = postService.getHotPostPage(reqVO);
        PageResult<AppPostRespVO> result = SocialPostConvert.INSTANCE.convertAppPage(pageResult);
        // 填充用户信息和点赞状态
        fillPostInfo(result.getList());
        return success(result);
    }

    @GetMapping("/my")
    @Operation(summary = "获取我的帖子")
    public CommonResult<PageResult<AppPostRespVO>> getMyPostList(@Valid AppPostPageReqVO reqVO) {
        reqVO.setUserId(getLoginUserId());
        PageResult<SocialPostDO> pageResult = postService.getPostPage(reqVO);
        PageResult<AppPostRespVO> result = SocialPostConvert.INSTANCE.convertAppPage(pageResult);
        // 填充用户信息和点赞状态
        fillPostInfo(result.getList());
        return success(result);
    }

    @GetMapping("/get")
    @Operation(summary = "获取帖子详情")
    @Parameter(name = "id", description = "帖子 ID", required = true)
    @PermitAll
    public CommonResult<AppPostRespVO> getPost(@RequestParam("id") Long id) {
        // 使用 getPost 触发浏览数+1和热度+1
        SocialPostDO post = postService.getPost(id);
        if (post == null) {
            throw cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception(
                    cn.iocoder.yudao.module.social.enums.ErrorCodeConstants.POST_NOT_EXISTS);
        }
        AppPostRespVO result = SocialPostConvert.INSTANCE.convertApp(post);
        // 填充用户信息和点赞状态
        fillPostInfo(result);
        return success(result);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除帖子（仅自己）")
    @Parameter(name = "id", description = "帖子 ID", required = true)
    public CommonResult<Boolean> deletePost(@RequestParam("id") Long id) {
        postService.deletePost(getLoginUserId(), id);
        return success(true);
    }

    @PostMapping("/like")
    @Operation(summary = "点赞帖子")
    @Parameter(name = "id", description = "帖子 ID", required = true)
    public CommonResult<Boolean> likePost(@RequestParam("id") Long id) {
        postService.likePost(getLoginUserId(), id);
        return success(true);
    }

    @PostMapping("/unlike")
    @Operation(summary = "取消点赞帖子")
    @Parameter(name = "id", description = "帖子 ID", required = true)
    public CommonResult<Boolean> unlikePost(@RequestParam("id") Long id) {
        postService.unlikePost(getLoginUserId(), id);
        return success(true);
    }

    /**
     * 填充帖子信息（用户地址、昵称、头像、点赞状态、关注状态）
     */
    private void fillPostInfo(AppPostRespVO post) {
        if (post == null) {
            return;
        }
        // 填充用户信息
        WalletUserDO user = walletUserService.getUser(post.getUserId());
        if (user != null) {
            post.setUserAddress(formatAddress(user.getWalletAddress()));
            post.setUserNickname(user.getNickname());
            post.setUserAvatar(user.getAvatar());
        }
        // 填充点赞和关注状态
        Long loginUserId = getLoginUserId();
        if (loginUserId != null) {
            post.setLiked(postService.hasLikedPost(loginUserId, post.getId()));
            // 不能关注自己，只有查看他人帖子时才检查关注状态
            if (!loginUserId.equals(post.getUserId())) {
                post.setFollowed(followService.hasFollowed(loginUserId, post.getUserId()));
            } else {
                post.setFollowed(false); // 自己的帖子不显示关注状态
            }
        } else {
            post.setLiked(false);
            post.setFollowed(false);
        }
    }

    /**
     * 批量填充帖子信息
     */
    private void fillPostInfo(List<AppPostRespVO> posts) {
        if (posts == null || posts.isEmpty()) {
            return;
        }
        // 获取所有用户信息
        Set<Long> userIds = posts.stream().map(AppPostRespVO::getUserId).collect(Collectors.toSet());
        Map<Long, WalletUserDO> userMap = userIds.stream()
                .map(walletUserService::getUser)
                .filter(u -> u != null)
                .collect(Collectors.toMap(WalletUserDO::getId, Function.identity()));

        Long loginUserId = getLoginUserId();

        for (AppPostRespVO post : posts) {
            // 填充用户信息
            WalletUserDO user = userMap.get(post.getUserId());
            if (user != null) {
                post.setUserAddress(formatAddress(user.getWalletAddress()));
                post.setUserNickname(user.getNickname());
                post.setUserAvatar(user.getAvatar());
            }
            // 填充点赞和关注状态
            if (loginUserId != null) {
                post.setLiked(postService.hasLikedPost(loginUserId, post.getId()));
                // 不能关注自己
                if (!loginUserId.equals(post.getUserId())) {
                    post.setFollowed(followService.hasFollowed(loginUserId, post.getUserId()));
                } else {
                    post.setFollowed(false);
                }
            } else {
                post.setLiked(false);
                post.setFollowed(false);
            }
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
