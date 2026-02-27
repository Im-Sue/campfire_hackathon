package cn.iocoder.yudao.module.task.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.task.controller.admin.vo.TaskRecordPageReqVO;
import cn.iocoder.yudao.module.task.controller.admin.vo.TaskRecordRespVO;
import cn.iocoder.yudao.module.task.service.TaskConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 任务记录")
@RestController
@RequestMapping("/task/record")
@Validated
public class TaskRecordController {

    @Resource
    private TaskConfigService taskConfigService;

    @GetMapping("/page")
    @Operation(summary = "分页查询任务记录")
    @PreAuthorize("@ss.hasPermission('task:record:query')")
    public CommonResult<PageResult<TaskRecordRespVO>> getRecordPage(@Valid TaskRecordPageReqVO reqVO) {
        return success(taskConfigService.getTaskRecordPage(reqVO));
    }

}
