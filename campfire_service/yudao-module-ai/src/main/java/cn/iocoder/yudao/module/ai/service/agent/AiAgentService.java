package cn.iocoder.yudao.module.ai.service.agent;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiAgentPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiAgentSaveReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiAgentDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * AI Agent Service 接口
 *
 * @author campfire
 */
public interface AiAgentService {

    /**
     * 创建 Agent
     *
     * @param createReqVO 创建信息
     * @return Agent ID
     */
    Long createAgent(@Valid AiAgentSaveReqVO createReqVO);

    /**
     * 更新 Agent
     *
     * @param updateReqVO 更新信息
     */
    void updateAgent(@Valid AiAgentSaveReqVO updateReqVO);

    /**
     * 删除 Agent
     *
     * @param id Agent ID
     */
    void deleteAgent(Long id);

    /**
     * 获取 Agent
     *
     * @param id Agent ID
     * @return Agent
     */
    AiAgentDO getAgent(Long id);

    /**
     * 获取 Agent 分页
     *
     * @param pageReqVO 分页查询
     * @return Agent 分页
     */
    PageResult<AiAgentDO> getAgentPage(AiAgentPageReqVO pageReqVO);

    /**
     * 获取所有启用的 Agent 列表
     *
     * @return Agent 列表
     */
    List<AiAgentDO> getEnabledAgentList();

    /**
     * 充值积分
     *
     * @param agentId Agent ID
     * @param points  积分数量
     */
    void rechargePoints(Long agentId, Long points);

    /**
     * 获取 Agent 可用余额
     *
     * @param agentId Agent ID
     * @return 可用余额
     */
    Long getAvailableBalance(Long agentId);

    /**
     * 更新 Agent 战绩
     *
     * @param agentId Agent ID
     * @param win     是否胜利
     * @param profit  盈亏
     */
    void updateStats(Long agentId, boolean win, Long profit);

    /**
     * 批量获取 Agent
     *
     * @param agentIds Agent ID 列表
     * @return Agent 列表
     */
    List<AiAgentDO> getAgentsByIds(List<Long> agentIds);

    /**
     * 获取 Agent 余额图表数据
     *
     * @param reqVO 查询参数
     * @return 图表数据
     */
    cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppAgentBalanceChartRespVO getBalanceChart(
            cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppAgentBalanceChartReqVO reqVO);

    /**
     * 获取 AI Agent 订单分页
     *
     * @param reqVO 查询参数
     * @return 订单分页
     */
    PageResult<cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppAgentOrderRespVO> getAgentOrderPage(
            cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppAgentOrderPageReqVO reqVO);

    /**
     * 获取 AI Agent 持仓分页
     *
     * @param reqVO 查询参数
     * @return 持仓分页
     */
    PageResult<cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppAgentPositionRespVO> getAgentPositionPage(
            cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppAgentPositionPageReqVO reqVO);

}
