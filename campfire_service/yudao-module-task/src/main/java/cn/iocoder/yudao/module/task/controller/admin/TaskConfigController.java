package cn.iocoder.yudao.module.task.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.task.dal.dataobject.TaskConfigDO;
import cn.iocoder.yudao.module.task.service.TaskConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 任务配置")
@RestController
@RequestMapping("/task/config")
@Validated
public class TaskConfigController {

    @Resource
    private TaskConfigService taskConfigService;

    @GetMapping("/page")
    @Operation(summary = "分页查询任务配置")
    @PreAuthorize("@ss.hasPermission('task:config:query')")
    public CommonResult<PageResult<TaskConfigDO>> getConfigPage(@Valid PageParam pageParam) {
        List<TaskConfigDO> list = taskConfigService.getAllConfigs();
        // 简单分页处理
        int start = (pageParam.getPageNo() - 1) * pageParam.getPageSize();
        int end = Math.min(start + pageParam.getPageSize(), list.size());
        List<TaskConfigDO> pageList = start < list.size() ? list.subList(start, end) : Collections.emptyList();
        return success(new PageResult<>(pageList, (long) list.size()));

    }

    @GetMapping("/list")
    @Operation(summary = "获取任务配置列表")
    @PreAuthorize("@ss.hasPermission('task:config:query')")
    public CommonResult<List<TaskConfigDO>> getConfigList() {
        return success(taskConfigService.getAllConfigs());
    }

    @GetMapping("/get")
    @Operation(summary = "获取任务配置详情")
    @Parameter(name = "id", description = "配置ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('task:config:query')")
    public CommonResult<TaskConfigDO> getConfig(@RequestParam("id") Long id) {
        return success(taskConfigService.getConfigById(id));
    }

    @PutMapping("/update")
    @Operation(summary = "更新任务配置")
    @PreAuthorize("@ss.hasPermission('task:config:update')")
    public CommonResult<Boolean> updateConfig(@Valid @RequestBody TaskConfigDO config) {
        taskConfigService.updateConfig(config);
        return success(true);
    }

}
