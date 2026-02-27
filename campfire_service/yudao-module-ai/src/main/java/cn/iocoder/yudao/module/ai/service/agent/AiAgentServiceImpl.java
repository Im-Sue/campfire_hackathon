package cn.iocoder.yudao.module.ai.service.agent;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiAgentPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.agent.vo.AiAgentSaveReqVO;
import cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppAgentBalanceChartReqVO;
import cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppAgentBalanceChartRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiAgentBalanceSnapshotDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.agent.AiAgentDO;
import cn.iocoder.yudao.module.ai.dal.mysql.agent.AiAgentBalanceSnapshotMapper;
import cn.iocoder.yudao.module.ai.dal.mysql.agent.AiAgentMapper;
import cn.iocoder.yudao.module.point.service.PointService;
import cn.iocoder.yudao.module.wallet.dal.dataobject.WalletUserDO;
import cn.iocoder.yudao.module.wallet.service.WalletUserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;

/**
 * AI Agent Service 实现类
 *
 * @author campfire
 */
@Service
@Validated
@Slf4j
public class AiAgentServiceImpl implements AiAgentService {

    @Resource
    private AiAgentMapper agentMapper;

    @Resource
    private WalletUserService walletUserService;

    @Resource
    private PointService pointService;

    @Resource
    private AiAgentBalanceSnapshotMapper snapshotMapper;

    @Resource
    private cn.iocoder.yudao.module.market.service.order.PmOrderService pmOrderService;

    @Resource
    private cn.iocoder.yudao.module.market.service.position.PmPositionService pmPositionService;

    @Resource
    private cn.iocoder.yudao.module.market.service.market.PmMarketService pmMarketService;

    @Resource
    private cn.iocoder.yudao.module.market.service.settlement.PmSettlementService pmSettlementService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAgent(AiAgentSaveReqVO createReqVO) {
        // 1. 校验钱包用户是否存在
        Long walletUserId = createReqVO.getWalletUserId();
        if (walletUserId == null) {
            throw exception(AI_AGENT_NOT_EXISTS, "钱包用户ID不能为空");
        }

        WalletUserDO walletUser = walletUserService.getUser(walletUserId);
        if (walletUser == null) {
            throw exception(AI_AGENT_NOT_EXISTS, "钱包用户不存在");
        }

        // 2. 确保积分账户存在
        pointService.getOrCreateAccount(walletUserId, walletUser.getWalletAddress());

        // 3. 充值初始积分（如果有）
        Long initialPoints = createReqVO.getInitialPoints();
        if (initialPoints != null && initialPoints > 0) {
            Map<String, Object> extension = new HashMap<>();
            extension.put("source", "agent_create");
            pointService.addPoints(walletUserId, walletUser.getWalletAddress(), initialPoints,
                    1, // type: 系统充值
                    "agent_recharge", // bizType
                    null, // bizId
                    "Agent创建初始充值",
                    extension);
            log.info("[createAgent] 为Agent充值初始积分 {}", initialPoints);
        }

        // 4. 创建 Agent 记录
        AiAgentDO agent = AiAgentDO.builder()
                .name(createReqVO.getName())
                .avatar(createReqVO.getAvatar())
                .walletUserId(walletUserId)
                .roleId(createReqVO.getRoleId())
                .description(createReqVO.getDescription())
                .personality(createReqVO.getPersonality())
                .riskLevel(createReqVO.getRiskLevel())
                .minBetAmount(createReqVO.getMinBetAmount() != null ? createReqVO.getMinBetAmount() : 100L)
                .maxBetAmount(createReqVO.getMaxBetAmount() != null ? createReqVO.getMaxBetAmount() : 10000L)
                .maxBetRatio(createReqVO.getMaxBetRatio() != null ? createReqVO.getMaxBetRatio() : new BigDecimal("0.30"))
                .totalEvents(0)
                .winCount(0)
                .totalProfit(0L)
                .creatorType(1) // 系统创建
                .status(createReqVO.getStatus() != null ? createReqVO.getStatus() : CommonStatusEnum.ENABLE.getStatus())
                .build();
        agentMapper.insert(agent);

        log.info("[createAgent] 创建Agent成功, id={}, name={}, walletUserId={}",
                agent.getId(), agent.getName(), walletUserId);
        return agent.getId();
    }

    @Override
    public void updateAgent(AiAgentSaveReqVO updateReqVO) {
        // 校验存在
        validateAgentExists(updateReqVO.getId());

        // 更新
        AiAgentDO updateObj = AiAgentDO.builder()
                .id(updateReqVO.getId())
                .name(updateReqVO.getName())
                .avatar(updateReqVO.getAvatar())
                .roleId(updateReqVO.getRoleId())
                .description(updateReqVO.getDescription())
                .personality(updateReqVO.getPersonality())
                .riskLevel(updateReqVO.getRiskLevel())
                .minBetAmount(updateReqVO.getMinBetAmount())
                .maxBetAmount(updateReqVO.getMaxBetAmount())
                .maxBetRatio(updateReqVO.getMaxBetRatio())
                .status(updateReqVO.getStatus())
                .build();
        agentMapper.updateById(updateObj);
    }

    @Override
    public void deleteAgent(Long id) {
        // 校验存在
        validateAgentExists(id);
        // 删除
        agentMapper.deleteById(id);
    }

    @Override
    public AiAgentDO getAgent(Long id) {
        return agentMapper.selectById(id);
    }

    @Override
    public PageResult<AiAgentDO> getAgentPage(AiAgentPageReqVO pageReqVO) {
        return agentMapper.selectPage(pageReqVO);
    }

    @Override
    public List<AiAgentDO> getEnabledAgentList() {
        return agentMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
    }

    @Override
    public void rechargePoints(Long agentId, Long points) {
        AiAgentDO agent = validateAgentExists(agentId);
        
        // 获取钱包用户信息
        WalletUserDO walletUser = walletUserService.getUser(agent.getWalletUserId());
        if (walletUser == null) {
            throw exception(AI_AGENT_NOT_EXISTS);
        }

        // 充值积分
        Map<String, Object> extension = new HashMap<>();
        extension.put("source", "admin_recharge");
        extension.put("agentId", agentId);
        pointService.addPoints(agent.getWalletUserId(), walletUser.getWalletAddress(), points,
                1, // type: 系统充值
                "agent_recharge", // bizType
                agentId, // bizId
                "Agent充值",
                extension);
        
        log.info("[rechargePoints] Agent {} 充值 {} 积分成功", agentId, points);
    }

    @Override
    public Long getAvailableBalance(Long agentId) {
        AiAgentDO agent = validateAgentExists(agentId);
        return pointService.getAvailablePoints(agent.getWalletUserId());
    }

    @Override
    public void updateStats(Long agentId, boolean win, Long profit) {
        AiAgentDO agent = validateAgentExists(agentId);
        AiAgentDO updateObj = new AiAgentDO();
        updateObj.setId(agentId);
        updateObj.setTotalEvents(agent.getTotalEvents() + 1);
        updateObj.setWinCount(agent.getWinCount() + (win ? 1 : 0));
        updateObj.setTotalProfit(agent.getTotalProfit() + profit);
        agentMapper.updateById(updateObj);
    }

    @Override
    public List<AiAgentDO> getAgentsByIds(List<Long> agentIds) {
        if (agentIds == null || agentIds.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return agentMapper.selectBatchIds(agentIds);
    }

    @Override
    public AppAgentBalanceChartRespVO getBalanceChart(AppAgentBalanceChartReqVO reqVO) {
        // 1. 确定时间范围
        LocalDateTime startTime = reqVO.getStartTime() != null ? reqVO.getStartTime() : LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = reqVO.getEndTime() != null ? reqVO.getEndTime() : LocalDateTime.now();

        // 2. 确定查询的 Agent 列表
        List<Long> agentIds = reqVO.getAgentIds();
        if (agentIds == null || agentIds.isEmpty()) {
            agentIds = getEnabledAgentList().stream()
                .map(AiAgentDO::getId)
                .collect(Collectors.toList());
        }

        // 3. 批量查询快照数据
        List<AiAgentBalanceSnapshotDO> snapshots = snapshotMapper.selectListByAgentIdsAndTimeRange(
            agentIds, startTime, endTime
        );

        // 4. 按 Agent 分组
        Map<Long, List<AiAgentBalanceSnapshotDO>> groupedSnapshots = snapshots.stream()
            .collect(Collectors.groupingBy(AiAgentBalanceSnapshotDO::getAgentId));

        // 5. 批量获取 Agent 信息
        Map<Long, AiAgentDO> agentMap = getAgentsByIds(agentIds).stream()
            .collect(Collectors.toMap(AiAgentDO::getId, agent -> agent));

        // 6. 构建响应数据
        List<AppAgentBalanceChartRespVO.AgentChartItem> chartItems = new ArrayList<>();
        for (Long agentId : agentIds) {
            List<AiAgentBalanceSnapshotDO> agentSnapshots = groupedSnapshots.getOrDefault(agentId, new ArrayList<>());
            AiAgentDO agent = agentMap.get(agentId);
            if (agent != null) {
                chartItems.add(buildChartItem(agent, agentSnapshots));
            }
        }

        // 7. 返回结果
        AppAgentBalanceChartRespVO.TimeRange timeRange = new AppAgentBalanceChartRespVO.TimeRange(startTime, endTime);
        return new AppAgentBalanceChartRespVO(chartItems, timeRange);
    }

    private AppAgentBalanceChartRespVO.AgentChartItem buildChartItem(
            AiAgentDO agent,
            List<AiAgentBalanceSnapshotDO> snapshots) {

        AppAgentBalanceChartRespVO.AgentChartItem item = new AppAgentBalanceChartRespVO.AgentChartItem();
        item.setAgentId(agent.getId());
        item.setAgentName(agent.getName());
        item.setAgentAvatar(agent.getAvatar());

        // 构建数据点列表
        List<AppAgentBalanceChartRespVO.DataPoint> dataPoints = new ArrayList<>();
        Long lastBalance = null;

        for (AiAgentBalanceSnapshotDO snapshot : snapshots) {
            Long change = lastBalance != null ? snapshot.getBalance() - lastBalance : 0L;
            dataPoints.add(new AppAgentBalanceChartRespVO.DataPoint(
                snapshot.getSnapshotTime(),
                snapshot.getBalance(),
                change
            ));
            lastBalance = snapshot.getBalance();
        }

        item.setData(dataPoints);

        // 计算统计信息
        if (!snapshots.isEmpty()) {
            Long initialBalance = snapshots.get(0).getBalance();
            Long currentBalance = snapshots.get(snapshots.size() - 1).getBalance();
            Long changeAmount = currentBalance - initialBalance;
            BigDecimal changePercent = initialBalance > 0
                ? BigDecimal.valueOf(changeAmount).multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(initialBalance), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

            item.setInitialBalance(initialBalance);
            item.setCurrentBalance(currentBalance);
            item.setChangeAmount(changeAmount);
            item.setChangePercent(changePercent);
        } else {
            // 如果没有快照数据，使用当前余额
            Long currentBalance = getAvailableBalance(agent.getId());
            item.setInitialBalance(currentBalance);
            item.setCurrentBalance(currentBalance);
            item.setChangeAmount(0L);
            item.setChangePercent(BigDecimal.ZERO);
        }

        return item;
    }

    private AiAgentDO validateAgentExists(Long id) {
        AiAgentDO agent = agentMapper.selectById(id);
        if (agent == null) {
            throw exception(AI_AGENT_NOT_EXISTS);
        }
        return agent;
    }

    @Override
    public PageResult<cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppAgentOrderRespVO> getAgentOrderPage(
            cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppAgentOrderPageReqVO reqVO) {
        // 1. 确定查询的 Agent 列表
        List<AiAgentDO> agents = new ArrayList<>();
        if (reqVO.getAgentId() != null) {
            AiAgentDO agent = agentMapper.selectById(reqVO.getAgentId());
            if (agent != null) {
                agents.add(agent);
            }
        } else {
            agents = getEnabledAgentList();
        }

        if (agents.isEmpty()) {
            return PageResult.empty();
        }

        // 2. 查询所有Agent的订单并合并
        List<cn.iocoder.yudao.module.market.dal.dataobject.order.PmOrderDO> allOrders = new ArrayList<>();
        for (AiAgentDO agent : agents) {
            List<cn.iocoder.yudao.module.market.dal.dataobject.order.PmOrderDO> orders =
                pmOrderService.getOrdersByUserId(agent.getWalletUserId());
            allOrders.addAll(orders);
        }

        // 3. 按条件筛选
        List<cn.iocoder.yudao.module.market.dal.dataobject.order.PmOrderDO> filteredOrders = allOrders.stream()
            .filter(order -> reqVO.getMarketId() == null || order.getMarketId().equals(reqVO.getMarketId()))
            .filter(order -> reqVO.getStatus() == null || order.getStatus().equals(reqVO.getStatus()))
            .sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()))
            .collect(Collectors.toList());

        // 4. 手动分页
        int total = filteredOrders.size();
        int pageNo = reqVO.getPageNo() != null ? reqVO.getPageNo() : 1;
        int pageSize = reqVO.getPageSize() != null ? reqVO.getPageSize() : 10;
        int fromIndex = (pageNo - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);

        if (fromIndex >= total) {
            return PageResult.empty((long) total);
        }

        List<cn.iocoder.yudao.module.market.dal.dataobject.order.PmOrderDO> pageOrders =
            filteredOrders.subList(fromIndex, toIndex);

        // 5. 批量查询市场信息
        java.util.Set<Long> marketIds = pageOrders.stream()
            .map(cn.iocoder.yudao.module.market.dal.dataobject.order.PmOrderDO::getMarketId)
            .collect(Collectors.toSet());
        Map<Long, cn.iocoder.yudao.module.market.dal.dataobject.market.PmMarketDO> marketMap =
            pmMarketService.getMarketMap(marketIds);

        // 6. 构建 Agent Map
        Map<Long, AiAgentDO> agentMap = agents.stream()
            .collect(Collectors.toMap(AiAgentDO::getWalletUserId, agent -> agent));

        // 7. 组装返回数据
        List<cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppAgentOrderRespVO> result = pageOrders.stream()
            .map(order -> {
                cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppAgentOrderRespVO vo =
                    new cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppAgentOrderRespVO();

                // 订单基础字段
                vo.setId(order.getId());
                vo.setOrderNo(order.getOrderNo());
                vo.setMarketId(order.getMarketId());
                vo.setOrderType(order.getOrderType());
                vo.setSide(order.getSide());
                vo.setOutcome(order.getOutcome());
                vo.setPrice(order.getPrice());
                vo.setQuantity(order.getQuantity());
                vo.setAmount(order.getAmount());
                vo.setFilledQuantity(order.getFilledQuantity());
                vo.setFilledAmount(order.getFilledAmount());
                vo.setFilledPrice(order.getFilledPrice());
                vo.setFilledAt(order.getFilledAt());
                vo.setStatus(order.getStatus());
                vo.setExpireAt(order.getExpireAt());
                vo.setCreateTime(order.getCreateTime());

                // 市场信息
                cn.iocoder.yudao.module.market.dal.dataobject.market.PmMarketDO market = marketMap.get(order.getMarketId());
                if (market != null) {
                    vo.setMarketQuestion(market.getQuestion());
                }

                // Agent 信息
                AiAgentDO agent = agentMap.get(order.getUserId());
                if (agent != null) {
                    vo.setAgentId(agent.getId());
                    vo.setAgentName(agent.getName());
                    vo.setAgentAvatar(agent.getAvatar());
                    vo.setAgentPersonality(agent.getPersonality());
                    vo.setAgentRiskLevel(agent.getRiskLevel());
                }

                return vo;
            })
            .collect(Collectors.toList());

        return new PageResult<>(result, (long) total);
    }

    @Override
    public PageResult<cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppAgentPositionRespVO> getAgentPositionPage(
            cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppAgentPositionPageReqVO reqVO) {
        // 1. 确定查询的 Agent 列表
        List<AiAgentDO> agents = new ArrayList<>();
        if (reqVO.getAgentId() != null) {
            AiAgentDO agent = agentMapper.selectById(reqVO.getAgentId());
            if (agent != null) {
                agents.add(agent);
            }
        } else {
            agents = getEnabledAgentList();
        }

        if (agents.isEmpty()) {
            return PageResult.empty();
        }

        // 2. 查询所有Agent的持仓并合并
        List<cn.iocoder.yudao.module.market.dal.dataobject.position.PmPositionDO> allPositions = new ArrayList<>();
        for (AiAgentDO agent : agents) {
            List<cn.iocoder.yudao.module.market.dal.dataobject.position.PmPositionDO> positions =
                pmPositionService.getPositionsByUserId(agent.getWalletUserId());
            allPositions.addAll(positions);
        }

        // 3. 按条件筛选
        List<cn.iocoder.yudao.module.market.dal.dataobject.position.PmPositionDO> filteredPositions = allPositions.stream()
            .filter(pos -> reqVO.getMarketId() == null || pos.getMarketId().equals(reqVO.getMarketId()))
            .filter(pos -> pos.getQuantity() != null && pos.getQuantity().compareTo(BigDecimal.ZERO) > 0)
            .sorted((p1, p2) -> p2.getUpdateTime().compareTo(p1.getUpdateTime()))
            .collect(Collectors.toList());

        // 4. 手动分页
        int total = filteredPositions.size();
        int pageNo = reqVO.getPageNo() != null ? reqVO.getPageNo() : 1;
        int pageSize = reqVO.getPageSize() != null ? reqVO.getPageSize() : 10;
        int fromIndex = (pageNo - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);

        if (fromIndex >= total) {
            return PageResult.empty((long) total);
        }

        List<cn.iocoder.yudao.module.market.dal.dataobject.position.PmPositionDO> pagePositions =
            filteredPositions.subList(fromIndex, toIndex);

        // 5. 批量查询市场信息
        java.util.Set<Long> marketIds = pagePositions.stream()
            .map(cn.iocoder.yudao.module.market.dal.dataobject.position.PmPositionDO::getMarketId)
            .collect(Collectors.toSet());
        Map<Long, cn.iocoder.yudao.module.market.dal.dataobject.market.PmMarketDO> marketMap =
            pmMarketService.getMarketMap(marketIds);

        // 6. 批量获取市场价格
        Map<Long, Map<String, BigDecimal>> pricesMap = new HashMap<>();
        for (Long marketId : marketIds) {
            pricesMap.put(marketId, pmMarketService.getMarketPrices(marketId));
        }

        // 7. 批量查询结算信息
        Map<Long, cn.iocoder.yudao.module.market.dal.dataobject.settlement.PmSettlementDO> settlementMap = new HashMap<>();
        for (Long marketId : marketIds) {
            cn.iocoder.yudao.module.market.dal.dataobject.settlement.PmSettlementDO settlement =
                pmSettlementService.getSettlementByMarketId(marketId);
            if (settlement != null) {
                settlementMap.put(marketId, settlement);
            }
        }

        // 8. 构建 Agent Map
        Map<Long, AiAgentDO> agentMap = agents.stream()
            .collect(Collectors.toMap(AiAgentDO::getWalletUserId, agent -> agent));

        // 9. 组装返回数据
        List<cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppAgentPositionRespVO> result = pagePositions.stream()
            .map(position -> {
                cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppAgentPositionRespVO vo =
                    new cn.iocoder.yudao.module.ai.controller.app.agent.vo.AppAgentPositionRespVO();

                // 持仓基础字段
                vo.setId(position.getId());
                vo.setMarketId(position.getMarketId());
                vo.setOutcome(position.getOutcome());
                vo.setQuantity(position.getQuantity());
                vo.setTotalCost(position.getTotalCost());
                vo.setRealizedPnl(position.getRealizedPnl());
                vo.setUpdateTime(position.getUpdateTime());

                // avgPrice 转为积分形式（×100）
                if (position.getAvgPrice() != null) {
                    vo.setAvgPrice(position.getAvgPrice()
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(0, RoundingMode.DOWN)
                        .longValue());
                }

                // 市场信息
                cn.iocoder.yudao.module.market.dal.dataobject.market.PmMarketDO market = marketMap.get(position.getMarketId());
                if (market != null) {
                    vo.setMarketQuestion(market.getQuestion());
                    vo.setMarketStatus(market.getStatus());
                }

                // 动态价格数据
                Map<String, BigDecimal> prices = pricesMap.get(position.getMarketId());
                if (prices != null) {
                    BigDecimal currentPriceRaw = prices.get(position.getOutcome());
                    if (currentPriceRaw != null) {
                        vo.setCurrentPrice(currentPriceRaw
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(0, RoundingMode.DOWN)
                            .longValue());

                        if (vo.getQuantity() != null) {
                            Long currentValue = currentPriceRaw.multiply(vo.getQuantity())
                                .multiply(BigDecimal.valueOf(100))
                                .setScale(0, RoundingMode.DOWN)
                                .longValue();
                            vo.setCurrentValue(currentValue);

                            if (vo.getTotalCost() != null) {
                                vo.setUnrealizedPnl(currentValue - vo.getTotalCost());
                            }
                        }
                    }
                }

                // 结算信息（不包含奖励）
                cn.iocoder.yudao.module.market.dal.dataobject.settlement.PmSettlementDO settlement =
                    settlementMap.get(position.getMarketId());
                if (settlement != null) {
                    vo.setSettlementStatus(settlement.getStatus());
                    vo.setWinnerOutcome(settlement.getWinnerOutcome());
                    vo.setIsWinner(settlement.getWinnerOutcome() != null
                        && settlement.getWinnerOutcome().equalsIgnoreCase(position.getOutcome()));
                }

                // Agent 信息
                AiAgentDO agent = agentMap.get(position.getUserId());
                if (agent != null) {
                    vo.setAgentId(agent.getId());
                    vo.setAgentName(agent.getName());
                    vo.setAgentAvatar(agent.getAvatar());
                    vo.setAgentPersonality(agent.getPersonality());
                    vo.setAgentRiskLevel(agent.getRiskLevel());
                }

                return vo;
            })
            .collect(Collectors.toList());

        return new PageResult<>(result, (long) total);
    }

}

