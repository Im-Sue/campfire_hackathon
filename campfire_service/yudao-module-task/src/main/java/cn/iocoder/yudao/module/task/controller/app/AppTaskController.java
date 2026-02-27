package cn.iocoder.yudao.module.task.controller.app;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.task.controller.app.vo.AppInviteInfoRespVO;
import cn.iocoder.yudao.module.task.controller.app.vo.AppTaskClaimReqVO;
import cn.iocoder.yudao.module.task.controller.app.vo.AppTaskCompleteReqVO;
import cn.iocoder.yudao.module.task.controller.app.vo.AppTaskCompleteRespVO;
import cn.iocoder.yudao.module.task.controller.app.vo.AppTaskItemRespVO;
import cn.iocoder.yudao.module.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 App - 任务")
@RestController
@RequestMapping("/task")
@Validated
@Slf4j
public class AppTaskController {

    @Resource
    private TaskService taskService;

    @GetMapping("/list")
    @Operation(summary = "获取任务列表")
    @PermitAll
    public CommonResult<List<AppTaskItemRespVO>> getTaskList(
            @RequestParam(value = "category", required = false) String category) {
        Long userId = getLoginUserId();
        return success(taskService.getTaskList(userId, category));
    }

    @PostMapping("/complete")
    @Operation(summary = "完成任务（点击类任务）")
    public CommonResult<AppTaskCompleteRespVO> completeTask(@Valid @RequestBody AppTaskCompleteReqVO reqVO) {
        Long userId = getLoginUserId();
        return success(taskService.completeClickTask(userId, reqVO.getTaskType()));
    }

    @PostMapping("/sign-in")
    @Operation(summary = "每日签到")
    public CommonResult<AppTaskCompleteRespVO> signIn() {
        Long userId = getLoginUserId();
        return success(taskService.signIn(userId));
    }

    @PostMapping("/claim")
    @Operation(summary = "领取任务奖励")
    public CommonResult<Long> claimReward(@Valid @RequestBody AppTaskClaimReqVO reqVO) {
        Long userId = getLoginUserId();
        return success(taskService.claimReward(userId, reqVO.getRecordId()));
    }

    @PostMapping("/claim-all")
    @Operation(summary = "一键领取所有奖励")
    public CommonResult<Long> claimAllRewards() {
        Long userId = getLoginUserId();
        return success(taskService.claimAllRewards(userId));
    }

    @GetMapping("/invite-info")
    @Operation(summary = "获取邀请信息（含邀请列表和可领取积分）")
    @Parameter(name = "pageNo", description = "页码", example = "1")
    @Parameter(name = "pageSize", description = "每页条数", example = "10")
    public CommonResult<AppInviteInfoRespVO> getInviteInfo(
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        Long userId = getLoginUserId();
        return success(taskService.getInviteInfo(userId, pageNo, pageSize));
    }

}
