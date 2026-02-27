package cn.iocoder.yudao.module.social.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.social.controller.admin.vo.*;
import cn.iocoder.yudao.module.social.convert.SocialTopicConvert;
import cn.iocoder.yudao.module.social.dal.dataobject.SocialTopicDO;
import cn.iocoder.yudao.module.social.service.SocialTopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - 社交话题
 */
@Tag(name = "管理后台 - 社交话题")
@RestController
@RequestMapping("/social/topic")
@Validated
public class SocialTopicController {

    @Resource
    private SocialTopicService topicService;

    @GetMapping("/page")
    @Operation(summary = "获取话题分页")
    @PreAuthorize("@ss.hasPermission('social:topic:query')")
    public CommonResult<PageResult<TopicRespVO>> getTopicPage(@Valid TopicPageReqVO reqVO) {
        PageResult<SocialTopicDO> pageResult = topicService.getTopicPage(reqVO);
        return success(SocialTopicConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/get")
    @Operation(summary = "获取话题详情")
    @Parameter(name = "id", description = "话题 ID", required = true)
    @PreAuthorize("@ss.hasPermission('social:topic:query')")
    public CommonResult<TopicRespVO> getTopic(@RequestParam("id") Long id) {
        SocialTopicDO topic = topicService.getTopic(id);
        return success(SocialTopicConvert.INSTANCE.convertToAdmin(topic));
    }

    @PostMapping("/create")
    @Operation(summary = "创建话题")
    @PreAuthorize("@ss.hasPermission('social:topic:create')")
    public CommonResult<Long> createTopic(@Valid @RequestBody TopicCreateReqVO reqVO) {
        return success(topicService.createTopic(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新话题")
    @PreAuthorize("@ss.hasPermission('social:topic:update')")
    public CommonResult<Boolean> updateTopic(@Valid @RequestBody TopicUpdateReqVO reqVO) {
        topicService.updateTopic(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除话题")
    @Parameter(name = "id", description = "话题 ID", required = true)
    @PreAuthorize("@ss.hasPermission('social:topic:delete')")
    public CommonResult<Boolean> deleteTopic(@RequestParam("id") Long id) {
        topicService.deleteTopic(id);
        return success(true);
    }

}
