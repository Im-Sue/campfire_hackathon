package cn.iocoder.yudao.module.social.controller.app;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.social.controller.app.vo.AppPostPageReqVO;
import cn.iocoder.yudao.module.social.controller.app.vo.AppPostRespVO;
import cn.iocoder.yudao.module.social.controller.app.vo.AppTopicRespVO;
import cn.iocoder.yudao.module.social.convert.SocialPostConvert;
import cn.iocoder.yudao.module.social.convert.SocialTopicConvert;
import cn.iocoder.yudao.module.social.dal.dataobject.SocialPostDO;
import cn.iocoder.yudao.module.social.dal.dataobject.SocialTopicDO;
import cn.iocoder.yudao.module.social.service.SocialPostService;
import cn.iocoder.yudao.module.social.service.SocialTopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 热门话题")
@RestController
@RequestMapping("/social/topic")
@Validated
public class AppSocialTopicController {

    @Resource
    private SocialTopicService socialTopicService;

    @Resource
    private SocialPostService socialPostService;

    @GetMapping("/hot")
    @Operation(summary = "获取热门话题列表")
    @PermitAll
    @Parameter(name = "limit", description = "数量限制，默认10")
    public CommonResult<List<AppTopicRespVO>> getHotTopicList(
            @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        List<SocialTopicDO> list = socialTopicService.getHotTopicList(limit);
        return success(SocialTopicConvert.INSTANCE.convertList(list));
    }

    /**
     * 获取话题下的帖子
     * 
     * @deprecated 推荐使用 GET /social/post/list?topicName=xxx，此接口保留仅为兼容性
     */
    @Deprecated
    @GetMapping("/posts")
    @Operation(summary = "获取话题下的帖子（已废弃，请使用 /social/post/list?topicName=xxx）")
    @Parameter(name = "name", description = "话题名称（如 #预测市场）", required = true)
    public CommonResult<PageResult<AppPostRespVO>> getPostsByTopic(
            @RequestParam("name") String topicName,
            @Valid AppPostPageReqVO reqVO) {
        // 将 topicName 设置到 reqVO 中，使用统一的 getPostPage 方法
        reqVO.setTopicName(topicName);
        PageResult<SocialPostDO> pageResult = socialPostService.getPostPage(reqVO);
        return success(SocialPostConvert.INSTANCE.convertAppPage(pageResult));
    }

}
