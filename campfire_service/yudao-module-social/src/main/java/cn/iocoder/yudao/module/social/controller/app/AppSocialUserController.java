package cn.iocoder.yudao.module.social.controller.app;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.social.controller.app.vo.AppUserProfileRespVO;
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

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wallet.enums.ErrorCodeConstants.WALLET_USER_NOT_EXISTS;

/**
 * 用户 App - 社交用户资料
 */
@Tag(name = "用户 App - 社交用户资料")
@RestController
@RequestMapping("/social/user")
@Validated
public class AppSocialUserController {

    @Resource
    private WalletUserService walletUserService;

    @Resource
    private SocialFollowService followService;

    @Resource
    private SocialPostService postService;

    @GetMapping("/profile")
    @Operation(summary = "获取用户主页信息")
    @Parameter(name = "userId", description = "用户 ID", required = true)
    @PermitAll
    public CommonResult<AppUserProfileRespVO> getUserProfile(@RequestParam("userId") Long userId) {
        // 获取用户基本信息
        WalletUserDO user = walletUserService.getUser(userId);
        if (user == null) {
            throw exception(WALLET_USER_NOT_EXISTS);
        }

        // 获取关注/粉丝数
        Long[] followCounts = followService.getFollowCount(userId);

        // 获取帖子数
        Integer postCount = postService.getPostCountByUserId(userId);

        // 获取当前登录用户是否已关注
        Long loginUserId = getLoginUserId();
        boolean followed = false;
        if (loginUserId != null && !loginUserId.equals(userId)) {
            followed = followService.hasFollowed(loginUserId, userId);
        }

        // 构建响应
        AppUserProfileRespVO respVO = AppUserProfileRespVO.builder()
                .userId(user.getId())
                .walletAddress(user.getWalletAddress())
                .walletAddressShort(formatAddress(user.getWalletAddress()))
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .bio(user.getBio())
                .followingCount(followCounts[0])
                .followersCount(followCounts[1])
                .postCount(postCount)
                .followed(followed)
                .build();

        return success(respVO);
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
