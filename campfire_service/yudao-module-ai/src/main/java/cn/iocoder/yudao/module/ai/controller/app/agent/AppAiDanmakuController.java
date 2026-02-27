package cn.iocoder.yudao.module.ai.controller.app.agent;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppDanmakuRespVO;
import cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppDanmakuSendReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiRoomDanmakuDO;
import cn.iocoder.yudao.module.ai.service.agent.AiRoomDanmakuService;
import cn.iocoder.yudao.module.wallet.api.WalletUserApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - AI 竞赛弹幕")
@RestController
@RequestMapping("/ai/app/danmaku")
@Validated
public class AppAiDanmakuController {

    @Resource
    private AiRoomDanmakuService danmakuService;

    @Resource
    private WalletUserApi walletUserApi;

    @PostMapping("/send")
    @Operation(summary = "发送弹幕")
    public CommonResult<Long> sendDanmaku(@Valid @RequestBody AppDanmakuSendReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String walletAddress = walletUserApi.getUserAddress(userId);
        return success(danmakuService.sendDanmaku(reqVO.getRoomId(), userId, walletAddress, reqVO.getContent(), reqVO.getColor()));
    }

    @GetMapping("/list")
    @Operation(summary = "获取弹幕列表（轮询）")
    @PermitAll
    public CommonResult<List<AppDanmakuRespVO>> getDanmakuList(
            @Parameter(name = "roomId", description = "房间ID") @RequestParam(value = "roomId", required = false) Long roomId,
            @Parameter(name = "afterTime", description = "上一次拉取的时间") @RequestParam(value = "afterTime", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss", fallbackPatterns = {"yyyy-MM-dd'T'HH:mm:ss"}) LocalDateTime afterTime) {
        List<AiRoomDanmakuDO> list = danmakuService.getDanmakuList(roomId, afterTime);
        List<AppDanmakuRespVO> result = new ArrayList<>();
        for (AiRoomDanmakuDO danmaku : list) {
            result.add(AppDanmakuRespVO.builder()
                    .id(danmaku.getId())
                    .content(danmaku.getContent())
                    .color(danmaku.getColor())
                    .createTime(danmaku.getCreateTime())
                    .build());
        }
        return success(result);
    }

}
