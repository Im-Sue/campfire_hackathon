package cn.iocoder.yudao.module.ai.controller.admin.agent;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiAgentPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiAgentRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiAgentSaveReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiAgentDO;
import cn.iocoder.yudao.module.ai.service.agent.AiAgentService;
import cn.iocoder.yudao.module.point.service.PointService;
import cn.iocoder.yudao.module.wallet.dal.dataobject.WalletUserDO;
import cn.iocoder.yudao.module.wallet.service.WalletUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * AI Agent 管理后台 Controller
 *
 * @author campfire
 */
@Tag(name = "管理后台 - AI Agent")
@RestController
@RequestMapping("/ai/agent")
@Validated
public class AiAgentController {

    @Resource
    private AiAgentService agentService;

    @Resource
    private WalletUserService walletUserService;

    @Resource
    private PointService pointService;

    @PostMapping("/create")
    @Operation(summary = "创建 Agent")
    @PreAuthorize("@ss.hasPermission('ai:agent:create')")
    public CommonResult<Long> createAgent(@Valid @RequestBody AiAgentSaveReqVO createReqVO) {
        return success(agentService.createAgent(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新 Agent")
    @PreAuthorize("@ss.hasPermission('ai:agent:update')")
    public CommonResult<Boolean> updateAgent(@Valid @RequestBody AiAgentSaveReqVO updateReqVO) {
        agentService.updateAgent(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除 Agent")
    @Parameter(name = "id", description = "Agent ID", required = true)
    @PreAuthorize("@ss.hasPermission('ai:agent:delete')")
    public CommonResult<Boolean> deleteAgent(@RequestParam("id") Long id) {
        agentService.deleteAgent(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取 Agent")
    @Parameter(name = "id", description = "Agent ID", required = true)
    @PreAuthorize("@ss.hasPermission('ai:agent:query')")
    public CommonResult<AiAgentRespVO> getAgent(@RequestParam("id") Long id) {
        AiAgentDO agent = agentService.getAgent(id);
        return success(BeanUtils.toBean(agent, AiAgentRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获取 Agent 分页")
    @PreAuthorize("@ss.hasPermission('ai:agent:query')")
    public CommonResult<PageResult<AiAgentRespVO>> getAgentPage(@Valid AiAgentPageReqVO pageReqVO) {
        PageResult<AiAgentDO> pageResult = agentService.getAgentPage(pageReqVO);
        PageResult<AiAgentRespVO> voPageResult = BeanUtils.toBean(pageResult, AiAgentRespVO.class);

        // 批量查询钱包信息和积分余额
        List<Long> walletUserIds = pageResult.getList().stream()
                .map(AiAgentDO::getWalletUserId)
                .collect(Collectors.toList());

        // 获取钱包用户信息
        Map<Long, WalletUserDO> walletUserMap = walletUserIds.stream()
                .map(walletUserService::getUser)
                .filter(user -> user != null)
                .collect(Collectors.toMap(WalletUserDO::getId, user -> user));

        // 填充钱包地址和积分余额
        for (AiAgentRespVO vo : voPageResult.getList()) {
            WalletUserDO walletUser = walletUserMap.get(vo.getWalletUserId());
            if (walletUser != null) {
                vo.setWalletAddress(walletUser.getWalletAddress());
            }
            // 获取积分余额
            Long balance = pointService.getAvailablePoints(vo.getWalletUserId());
            vo.setBalance(balance != null ? balance : 0L);
        }

        return success(voPageResult);
    }

    @GetMapping("/list-enabled")
    @Operation(summary = "获取启用的 Agent 列表")
    @PreAuthorize("@ss.hasPermission('ai:agent:query')")
    public CommonResult<List<AiAgentRespVO>> getEnabledAgentList() {
        List<AiAgentDO> list = agentService.getEnabledAgentList();
        return success(BeanUtils.toBean(list, AiAgentRespVO.class));
    }

    @PostMapping("/recharge")
    @Operation(summary = "充值积分")
    @PreAuthorize("@ss.hasPermission('ai:agent:update')")
    public CommonResult<Boolean> rechargePoints(@RequestParam("id") Long id,
                                                 @RequestParam("points") Long points) {
        agentService.rechargePoints(id, points);
        return success(true);
    }

    @GetMapping("/balance")
    @Operation(summary = "获取 Agent 余额")
    @Parameter(name = "id", description = "Agent ID", required = true)
    @PreAuthorize("@ss.hasPermission('ai:agent:query')")
    public CommonResult<Long> getAgentBalance(@RequestParam("id") Long id) {
        return success(agentService.getAvailableBalance(id));
    }

}
