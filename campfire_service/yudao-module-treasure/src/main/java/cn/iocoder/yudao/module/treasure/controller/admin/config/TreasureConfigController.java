package cn.iocoder.yudao.module.treasure.controller.admin.config;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.treasure.dal.dataobject.TreasureConfigDO;
import cn.iocoder.yudao.module.treasure.service.config.TreasureConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 夺宝模块配置 Controller
 *
 * @author Sue
 */
@Tag(name = "管理后台 - 夺宝配置")
@RestController
@RequestMapping("/admin-api/treasure/config")
@Validated
public class TreasureConfigController {

    @Resource
    private TreasureConfigService configService;

    @GetMapping("/list")
    @Operation(summary = "获取所有配置")
    @PreAuthorize("@ss.hasPermission('treasure:config:query')")
    public CommonResult<List<TreasureConfigDO>> listConfigs() {
        return success(configService.listConfigs());
    }

    @PutMapping("/update")
    @Operation(summary = "更新配置")
    @PreAuthorize("@ss.hasPermission('treasure:config:update')")
    public CommonResult<Boolean> updateConfig(@Validated @RequestBody ConfigUpdateReqVO reqVO) {
        configService.updateConfig(reqVO.getConfigKey(), reqVO.getConfigValue());
        return success(true);
    }

    @PostMapping("/refresh-cache")
    @Operation(summary = "刷新配置缓存")
    @PreAuthorize("@ss.hasPermission('treasure:config:update')")
    public CommonResult<Boolean> refreshCache() {
        configService.refreshCache();
        return success(true);
    }

    @Data
    public static class ConfigUpdateReqVO {
        @NotBlank(message = "配置键不能为空")
        private String configKey;
        @NotBlank(message = "配置值不能为空")
        private String configValue;
    }
}
