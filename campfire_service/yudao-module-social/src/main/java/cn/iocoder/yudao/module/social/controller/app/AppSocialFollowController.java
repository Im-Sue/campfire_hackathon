package cn.iocoder.yudao.module.social.controller.app;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.social.controller.app.vo.AppFollowCountRespVO;
import cn.iocoder.yudao.module.social.controller.app.vo.AppFollowUserRespVO;
import cn.iocoder.yudao.module.social.dal.dataobject.SocialFollowDO;
import cn.iocoder.yudao.module.social.service.SocialFollowService;
import cn.iocoder.yudao.module.wallet.dal.dataobject.WalletUserDO;
import cn.iocoder.yudao.module.wallet.service.WalletUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 App - 社交关注")
@RestController
@RequestMapping("/social/follow")
@Validated
public class AppSocialFollowController {

    @Resource
    private SocialFollowService socialFollowService;

    @Resource
    private WalletUserService walletUserService;

    @PostMapping("/do")
    @Operation(summary = "关注用户")
    @Parameter(name = "userId", description = "被关注用户ID", required = true)
    public CommonResult<Boolean> doFollow(@RequestParam("userId") Long userId) {
        socialFollowService.follow(getLoginUserId(), userId);
        return success(true);
    }

    @PostMapping("/undo")
    @Operation(summary = "取消关注")
    @Parameter(name = "userId", description = "被关注用户ID", required = true)
    public CommonResult<Boolean> undoFollow(@RequestParam("userId") Long userId) {
        socialFollowService.unfollow(getLoginUserId(), userId);
        return success(true);
    }

    @GetMapping("/check")
    @Operation(summary = "检查是否已关注")
    @Parameter(name = "userId", description = "被关注用户ID", required = true)
    public CommonResult<Boolean> checkFollow(@RequestParam("userId") Long userId) {
        return success(socialFollowService.hasFollowed(getLoginUserId(), userId));
    }

    @GetMapping("/following")
    @Operation(summary = "获取关注列表")
    @PermitAll
    @Parameter(name = "userId", description = "用户ID", required = true)
    public CommonResult<List<AppFollowUserRespVO>> getFollowingList(@RequestParam("userId") Long userId) {
        List<SocialFollowDO> list = socialFollowService.getFollowingList(userId);
        // 获取被关注用户的 ID 集合
        Set<Long> userIds = list.stream().map(SocialFollowDO::getFollowUserId).collect(Collectors.toSet());
        Map<Long, WalletUserDO> userMap = getUserMap(userIds);
        
        List<AppFollowUserRespVO> result = list.stream()
                .map(f -> {
                    AppFollowUserRespVO vo = new AppFollowUserRespVO(f.getFollowUserId(), f.getCreateTime());
                    WalletUserDO user = userMap.get(f.getFollowUserId());
                    if (user != null) {
                        vo.setUserAddress(formatAddress(user.getWalletAddress()));
                        vo.setUserNickname(user.getNickname());
                        vo.setUserAvatar(user.getAvatar());
                    }
                    return vo;
                })
                .collect(Collectors.toList());
        return success(result);
    }

    @GetMapping("/followers")
    @Operation(summary = "获取粉丝列表")
    @PermitAll
    @Parameter(name = "userId", description = "用户ID", required = true)
    public CommonResult<List<AppFollowUserRespVO>> getFollowersList(@RequestParam("userId") Long userId) {
        List<SocialFollowDO> list = socialFollowService.getFollowersList(userId);
        // 获取粉丝用户的 ID 集合
        Set<Long> userIds = list.stream().map(SocialFollowDO::getUserId).collect(Collectors.toSet());
        Map<Long, WalletUserDO> userMap = getUserMap(userIds);
        
        List<AppFollowUserRespVO> result = list.stream()
                .map(f -> {
                    AppFollowUserRespVO vo = new AppFollowUserRespVO(f.getUserId(), f.getCreateTime());
                    WalletUserDO user = userMap.get(f.getUserId());
                    if (user != null) {
                        vo.setUserAddress(formatAddress(user.getWalletAddress()));
                        vo.setUserNickname(user.getNickname());
                        vo.setUserAvatar(user.getAvatar());
                    }
                    return vo;
                })
                .collect(Collectors.toList());
        return success(result);
    }

    @GetMapping("/count")
    @Operation(summary = "获取关注/粉丝数量")
    @PermitAll
    @Parameter(name = "userId", description = "用户ID", required = true)
    public CommonResult<AppFollowCountRespVO> getFollowCount(@RequestParam("userId") Long userId) {
        Long[] counts = socialFollowService.getFollowCount(userId);
        return success(new AppFollowCountRespVO(counts[0], counts[1]));
    }

    /**
     * 批量获取用户信息
     */
    private Map<Long, WalletUserDO> getUserMap(Set<Long> userIds) {
        return userIds.stream()
                .map(walletUserService::getUser)
                .filter(u -> u != null)
                .collect(Collectors.toMap(WalletUserDO::getId, Function.identity()));
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
