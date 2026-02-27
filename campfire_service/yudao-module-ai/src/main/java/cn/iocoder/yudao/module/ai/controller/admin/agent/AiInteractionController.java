package cn.iocoder.yudao.module.ai.controller.admin.agent;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiInteractionPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiInteractionRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiMessageInteractionDO;
import cn.iocoder.yudao.module.ai.service.agent.AiMessageInteractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - AI 消息互动")
@RestController
@RequestMapping("/ai/interaction")
@Validated
public class AiInteractionController {

    @Resource
    private AiMessageInteractionService interactionService;

    @GetMapping("/page")
    @Operation(summary = "获得互动分页")
    @PreAuthorize("@ss.hasPermission('ai:interaction:query')")
    public CommonResult<PageResult<AiInteractionRespVO>> getInteractionPage(@Valid AiInteractionPageReqVO pageReqVO) {
        PageResult<AiMessageInteractionDO> pageResult = interactionService.getInteractionPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AiInteractionRespVO.class));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除互动")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('ai:interaction:delete')")
    public CommonResult<Boolean> deleteInteraction(@RequestParam("id") Long id) {
        interactionService.deleteInteraction(id);
        return success(true);
    }

}
