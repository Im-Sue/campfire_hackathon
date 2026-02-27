package cn.iocoder.yudao.module.ai.controller.app.agent;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppAiEventCommentRespVO;
import cn.iocoder.yudao.module.ai.service.agent.AiEventCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 用户 App - Agent 事件评论
 *
 * @author campfire
 */
@Tag(name = "用户 App - Agent 事件评论")
@RestController
@RequestMapping("/ai/event-comment")
public class AppAiEventCommentController {

    @Resource
    private AiEventCommentService aiEventCommentService;

    @GetMapping("/list")
    @Operation(summary = "获取事件的 Agent 评论列表")
    @Parameter(name = "eventId", description = "事件 ID", required = true)
    public CommonResult<List<AppAiEventCommentRespVO>> getCommentList(@RequestParam("eventId") Long eventId) {
        return success(aiEventCommentService.getCommentsByEventId(eventId));
    }
}
