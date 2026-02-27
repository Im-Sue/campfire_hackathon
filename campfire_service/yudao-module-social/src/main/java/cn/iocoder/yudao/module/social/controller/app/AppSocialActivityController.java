package cn.iocoder.yudao.module.social.controller.app;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.social.controller.app.vo.AppActivityRespVO;
import cn.iocoder.yudao.module.social.service.SocialActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 App - 互动记录")
@RestController
@RequestMapping("/social/activity")
@Validated
public class AppSocialActivityController {

    @Resource
    private SocialActivityService socialActivityService;

    @GetMapping("/list")
    @Operation(summary = "获取互动记录（增强版，包含用户信息和内容摘要）")
    public CommonResult<PageResult<AppActivityRespVO>> getActivityList(PageParam pageParam) {
        PageResult<AppActivityRespVO> pageResult = socialActivityService.getActivityList(getLoginUserId(), pageParam);
        return success(pageResult);
    }

}
