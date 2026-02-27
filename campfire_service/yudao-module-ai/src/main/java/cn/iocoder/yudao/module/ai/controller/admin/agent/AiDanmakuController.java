package cn.iocoder.yudao.module.ai.controller.admin.agent;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiDanmakuPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiDanmakuRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiRoomDanmakuDO;
import cn.iocoder.yudao.module.ai.service.agent.AiRoomDanmakuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - AI 竞赛弹幕")
@RestController
@RequestMapping("/ai/danmaku")
@Validated
public class AiDanmakuController {

    @Resource
    private AiRoomDanmakuService danmakuService;

    @GetMapping("/page")
    @Operation(summary = "获得弹幕分页")
    @PreAuthorize("@ss.hasPermission('ai:danmaku:query')")
    public CommonResult<PageResult<AiDanmakuRespVO>> getDanmakuPage(@Valid AiDanmakuPageReqVO pageReqVO) {
        PageResult<AiRoomDanmakuDO> pageResult = danmakuService.getDanmakuPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AiDanmakuRespVO.class));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除弹幕")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('ai:danmaku:delete')")
    public CommonResult<Boolean> deleteDanmaku(@RequestParam("id") Long id) {
        danmakuService.deleteDanmaku(id);
        return success(true);
    }

}
