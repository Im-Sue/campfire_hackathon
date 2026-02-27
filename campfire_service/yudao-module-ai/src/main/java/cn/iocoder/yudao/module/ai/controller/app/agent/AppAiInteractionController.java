package cn.iocoder.yudao.module.ai.controller.app.agent;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppInteractionCommentReqVO;
import cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppInteractionCommentRespVO;
import cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppInteractionReqVO;
import cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppInteractionStatsRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiMessageInteractionDO;
import cn.iocoder.yudao.module.ai.service.agent.AiMessageInteractionService;
import cn.iocoder.yudao.module.wallet.api.WalletUserApi;
import cn.iocoder.yudao.module.wallet.dal.dataobject.WalletUserDO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - AI 互动")
@RestController
@RequestMapping("/ai/app/interaction")
@Validated
public class AppAiInteractionController {

    @Resource
    private AiMessageInteractionService interactionService;

    @Resource
    private WalletUserApi walletUserApi;

    @PostMapping("/flower")
    @Operation(summary = "送鲜花")
    public CommonResult<Boolean> addFlower(@Valid @RequestBody AppInteractionReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String walletAddress = walletUserApi.getUserAddress(userId);
        return success(interactionService.addFlower(reqVO.getTargetType(), reqVO.getTargetId(), userId, walletAddress));
    }

    @PostMapping("/egg")
    @Operation(summary = "砸鸡蛋")
    public CommonResult<Boolean> addEgg(@Valid @RequestBody AppInteractionReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String walletAddress = walletUserApi.getUserAddress(userId);
        return success(interactionService.addEgg(reqVO.getTargetType(), reqVO.getTargetId(), userId, walletAddress));
    }

    @PostMapping("/comment")
    @Operation(summary = "发表评论")
    public CommonResult<Long> addComment(@Valid @RequestBody AppInteractionCommentReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String walletAddress = walletUserApi.getUserAddress(userId);
        return success(interactionService.addComment(reqVO.getTargetType(), reqVO.getTargetId(), userId, walletAddress, reqVO.getContent()));
    }

    @GetMapping("/stats")
    @Operation(summary = "获取互动统计")
    @PermitAll
    public CommonResult<AppInteractionStatsRespVO> getStats(
            @Parameter(name = "targetType", description = "目标类型", required = true) @RequestParam("targetType") Integer targetType,
            @Parameter(name = "targetId", description = "目标ID", required = true) @RequestParam("targetId") Long targetId) {
        Long userId = SecurityFrameworkUtils.getLoginUserId(); // 可选登录
        return success(interactionService.getStats(targetType, targetId, userId));
    }

    @GetMapping("/comments")
    @Operation(summary = "获取评论列表")
    @PermitAll
    public CommonResult<List<AppInteractionCommentRespVO>> getComments(
            @Parameter(name = "targetType", description = "目标类型", required = true) @RequestParam("targetType") Integer targetType,
            @Parameter(name = "targetId", description = "目标ID", required = true) @RequestParam("targetId") Long targetId) {
        List<AiMessageInteractionDO> list = interactionService.getComments(targetType, targetId);

        // 批量查询评论者用户信息
        Set<Long> userIds = list.stream()
                .map(AiMessageInteractionDO::getUserId)
                .collect(Collectors.toSet());
        Map<Long, WalletUserDO> userMap = walletUserApi.getUserMap(userIds);

        List<AppInteractionCommentRespVO> result = new ArrayList<>();
        for (AiMessageInteractionDO interaction : list) {
            String walletAddress = interaction.getWalletAddress();
            // 简单脱敏: 0x12...abcd
            String maskedAddress = walletAddress;
            if (walletAddress != null && walletAddress.length() > 10) {
                maskedAddress = walletAddress.substring(0, 4) + "..." + walletAddress.substring(walletAddress.length() - 4);
            }

            WalletUserDO user = userMap.get(interaction.getUserId());
            result.add(AppInteractionCommentRespVO.builder()
                    .id(interaction.getId())
                    .content(interaction.getContent())
                    .walletAddress(maskedAddress)
                    .nickname(user != null ? user.getNickname() : null)
                    .avatar(user != null ? user.getAvatar() : null)
                    .createTime(interaction.getCreateTime())
                    .build());
        }
        return success(result);
    }
}
