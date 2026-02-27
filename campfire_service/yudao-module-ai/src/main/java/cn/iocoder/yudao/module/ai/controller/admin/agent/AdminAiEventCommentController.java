package cn.iocoder.yudao.module.ai.controller.admin.agent;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiEventCommentPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiEventCommentDO;
import cn.iocoder.yudao.module.ai.service.agent.AiEventCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - Agent 事件评论
 *
 * @author campfire
 */
@Tag(name = "管理后台 - Agent 事件评论")
@RestController
@RequestMapping("/ai/event-comment")
public class AdminAiEventCommentController {

    @Resource
    private AiEventCommentService aiEventCommentService;

    @GetMapping("/page")
    @Operation(summary = "获取 Agent 评论分页")
    @PreAuthorize("@ss.hasPermission('ai:event-comment:query')")
    public CommonResult<PageResult<AiEventCommentDO>> getCommentPage(@Valid AiEventCommentPageReqVO reqVO) {
        return success(aiEventCommentService.getCommentPage(reqVO));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除 Agent 评论")
    @Parameter(name = "id", description = "评论 ID", required = true)
    @PreAuthorize("@ss.hasPermission('ai:event-comment:delete')")
    public CommonResult<Boolean> deleteComment(@RequestParam("id") Long id) {
        aiEventCommentService.deleteComment(id);
        return success(true);
    }

    @PostMapping("/regenerate")
    @Operation(summary = "重新生成 Agent 评论")
    @PreAuthorize("@ss.hasPermission('ai:event-comment:update')")
    public CommonResult<Boolean> regenerateComment(
            @RequestParam("eventId") Long eventId,
            @RequestParam(value = "agentId", required = false) Long agentId) {
        aiEventCommentService.regenerateComment(eventId, agentId);
        return success(true);
    }
}
