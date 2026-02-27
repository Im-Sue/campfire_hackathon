package cn.iocoder.yudao.module.treasure.controller.app.pool;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.treasure.controller.app.pool.vo.*;
import cn.iocoder.yudao.module.treasure.dal.dataobject.TreasurePoolDO;
import cn.iocoder.yudao.module.treasure.dal.dataobject.TreasureTicketDO;
import cn.iocoder.yudao.module.treasure.dal.dataobject.TreasureWinnerDO;
import cn.iocoder.yudao.module.treasure.dal.mysql.TreasureTicketMapper;
import cn.iocoder.yudao.module.treasure.dal.mysql.TreasureWinnerMapper;
import cn.iocoder.yudao.module.treasure.enums.PoolStatusEnum;
import cn.iocoder.yudao.module.treasure.service.pool.TreasurePoolService;
import cn.iocoder.yudao.module.treasure.service.config.TreasureConfigService;
import cn.iocoder.yudao.module.treasure.service.contract.TreasureContractService;
import cn.iocoder.yudao.module.wallet.dal.dataobject.WalletUserDO;
import cn.iocoder.yudao.module.wallet.service.WalletUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * 用户端 - 奖池管理 Controller
 *
 * @author Sue
 */
@Tag(name = "用户端 - 奖池管理")
@RestController
@RequestMapping("/treasure/pool")
@Validated
@Slf4j
public class AppTreasurePoolController {

    private static final BigDecimal WEI_DECIMALS = BigDecimal.TEN.pow(18);
    /** 平台费率 10% */
    private static final BigDecimal PLATFORM_FEE_RATE = new BigDecimal("0.10");

    @Resource
    private TreasurePoolService poolService;

    @Resource
    private TreasureTicketMapper ticketMapper;

    @Resource
    private TreasureWinnerMapper winnerMapper;

    @Resource
    private WalletUserService walletUserService;

    @Resource
    private TreasureConfigService treasureConfigService;

    @Resource
    private TreasureContractService treasureContractService;

    // ==================== API 0: 获取夺宝模块配置 ====================

    @GetMapping("/config")
    @Operation(summary = "获取夺宝模块公开配置")
    public CommonResult<AppTreasureConfigVO> getConfig() {
        Integer platformFeeRate = null;
        try {
            platformFeeRate = treasureContractService.getPlatformFeeRate().intValue();
        } catch (Exception e) {
            log.warn("获取平台手续费率失败，使用默认值", e);
            platformFeeRate = 500; // 默认 5%
        }
        return success(AppTreasureConfigVO.builder()
                .contractAddress(treasureConfigService.getContractAddress())
                .chainId(treasureConfigService.getChainId())
                .platformFeeRate(platformFeeRate)
                .build());
    }

    // ==================== API 1: 获取当前活跃奖池 ====================

    @GetMapping("/active")
    @Operation(summary = "获取当前活跃奖池")
    public CommonResult<AppActivePoolRespVO> getActivePool() {
        TreasurePoolDO pool = poolService.getFirstActivePool();
        if (pool == null) {
            return success(null);
        }

        AppActivePoolRespVO respVO = buildActivePoolResp(pool);

        // 如果用户已登录，查询参与信息
        String userAddress = getOptionalUserAddress();
        if (userAddress != null) {
            TreasureTicketDO ticket = ticketMapper.selectByUserAndPool(
                    userAddress, pool.getPoolId(), pool.getContractAddress(), pool.getChainId());
            if (ticket != null) {
                respVO.setUserParticipation(buildActiveParticipation(ticket));
            }
        }

        return success(respVO);
    }

    // ==================== API 2: 获取历史奖池列表 ====================

    @GetMapping("/history-page")
    @Operation(summary = "获取历史奖池列表（分页）")
    public CommonResult<PageResult<AppPoolHistoryRespVO>> getHistoryPoolPage(@Valid PageParam pageParam) {
        PageResult<TreasurePoolDO> pageResult = poolService.getHistoryPoolPage(pageParam);

        if (pageResult.getList().isEmpty()) {
            return success(new PageResult<>(Collections.emptyList(), pageResult.getTotal()));
        }

        // 收集所有 poolId
        List<Long> poolIds = pageResult.getList().stream()
                .map(TreasurePoolDO::getPoolId)
                .collect(Collectors.toList());

        // 批量查中奖记录（用于构建 settlement）
        Map<Long, List<TreasureWinnerDO>> winnersByPool = new HashMap<>();
        for (Long poolId : poolIds) {
            List<TreasureWinnerDO> winners = winnerMapper.selectSimpleByPoolId(poolId);
            if (!winners.isEmpty()) {
                winnersByPool.put(poolId, winners);
            }
        }

        // 如果用户已登录，批量查参与情况
        Map<Long, TreasureTicketDO> userTicketsByPool = new HashMap<>();
        String userAddress = getOptionalUserAddress();
        if (userAddress != null && !poolIds.isEmpty()) {
            List<TreasureTicketDO> userTickets = ticketMapper.selectByOwnerAddressAndPoolIds(userAddress, poolIds);
            userTicketsByPool = userTickets.stream()
                    .collect(Collectors.toMap(TreasureTicketDO::getPoolId, t -> t, (a, b) -> a));
        }

        // 组装响应
        List<AppPoolHistoryRespVO> respList = new ArrayList<>();
        for (TreasurePoolDO pool : pageResult.getList()) {
            AppPoolHistoryRespVO respVO = buildHistoryPoolResp(pool);

            // 结算信息
            List<TreasureWinnerDO> winners = winnersByPool.get(pool.getPoolId());
            if (pool.getStatus().equals(PoolStatusEnum.SETTLED.getStatus()) && winners != null) {
                respVO.setSettlement(buildSettlement(pool, winners));
            }

            // 用户参与信息
            TreasureTicketDO userTicket = userTicketsByPool.get(pool.getPoolId());
            if (userTicket != null) {
                respVO.setUserParticipation(buildFullParticipation(userTicket, winners));
            }

            respList.add(respVO);
        }

        return success(new PageResult<>(respList, pageResult.getTotal()));
    }

    // ==================== API 3: 获取指定奖池详情 ====================

    @GetMapping("/detail")
    @Operation(summary = "获取指定奖池详细信息")
    @Parameter(name = "poolId", description = "链上奖池 ID", required = true, example = "1")
    public CommonResult<AppPoolDetailRespVO> getPoolDetail(@RequestParam("poolId") Long poolId) {
        TreasurePoolDO pool = poolService.getPoolByPoolId(poolId);
        if (pool == null) {
            return success(null);
        }

        AppPoolDetailRespVO respVO = buildDetailResp(pool);

        // 查中奖记录
        List<TreasureWinnerDO> winners = winnerMapper.selectSimpleByPoolId(poolId);

        // 结算信息
        if (pool.getStatus().equals(PoolStatusEnum.SETTLED.getStatus()) && !winners.isEmpty()) {
            respVO.setSettlement(buildSettlement(pool, winners));
        }

        // 参与者列表
        List<TreasureTicketDO> allTickets = ticketMapper.selectSimpleByPoolId(poolId);
        respVO.setParticipants(allTickets.stream()
                .map(t -> AppParticipantVO.builder()
                        .address(t.getOwnerAddress())
                        .ticketIndex(t.getTicketIndex())
                        .displayCode(t.getDisplayCode())
                        .purchaseTime(t.getPurchaseTime())
                        .build())
                .collect(Collectors.toList()));
        respVO.setParticipantCount(allTickets.size());

        // 用户参与信息
        String userAddress = getOptionalUserAddress();
        if (userAddress != null) {
            TreasureTicketDO userTicket = allTickets.stream()
                    .filter(t -> userAddress.equalsIgnoreCase(t.getOwnerAddress()))
                    .findFirst()
                    .orElse(null);
            if (userTicket != null) {
                respVO.setUserParticipation(buildFullParticipation(userTicket, winners));
            }
        }

        return success(respVO);
    }

    // ==================== API 4: 我参与的奖池记录 ====================

    @GetMapping("/my-page")
    @Operation(summary = "获取我参与的奖池记录（分页）")
    public CommonResult<PageResult<AppPoolHistoryRespVO>> getMyPoolPage(@Valid PageParam pageParam) {
        // 1. 获取当前登录用户的钱包地址（必须登录）
        String userAddress = getOptionalUserAddress();
        if (userAddress == null) {
            return success(new PageResult<>(Collections.emptyList(), 0L));
        }

        // 2. 分页查询用户的票号记录（按购买时间倒序）
        PageResult<TreasureTicketDO> ticketPage = ticketMapper.selectPageByOwnerAddress(userAddress, pageParam);
        if (ticketPage.getList().isEmpty()) {
            return success(new PageResult<>(Collections.emptyList(), ticketPage.getTotal()));
        }

        // 3. 收集 poolId，批量查奖池信息
        List<Long> poolIds = ticketPage.getList().stream()
                .map(TreasureTicketDO::getPoolId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, TreasurePoolDO> poolMap = new HashMap<>();
        for (Long poolId : poolIds) {
            TreasurePoolDO pool = poolService.getPoolByPoolId(poolId);
            if (pool != null) {
                poolMap.put(poolId, pool);
            }
        }

        // 4. 批量查中奖记录
        Map<Long, List<TreasureWinnerDO>> winnersByPool = new HashMap<>();
        for (Long poolId : poolIds) {
            List<TreasureWinnerDO> winners = winnerMapper.selectSimpleByPoolId(poolId);
            if (!winners.isEmpty()) {
                winnersByPool.put(poolId, winners);
            }
        }

        // 5. 组装响应（以 ticket 为维度，每条 ticket 对应一个奖池）
        List<AppPoolHistoryRespVO> respList = new ArrayList<>();
        for (TreasureTicketDO ticket : ticketPage.getList()) {
            TreasurePoolDO pool = poolMap.get(ticket.getPoolId());
            if (pool == null) {
                continue;
            }

            AppPoolHistoryRespVO respVO = buildHistoryPoolResp(pool);

            // 结算信息
            List<TreasureWinnerDO> winners = winnersByPool.get(pool.getPoolId());
            if (pool.getStatus().equals(PoolStatusEnum.SETTLED.getStatus()) && winners != null) {
                respVO.setSettlement(buildSettlement(pool, winners));
            }

            // 用户参与信息（一定存在，因为是从用户 ticket 反查的）
            respVO.setUserParticipation(buildFullParticipation(ticket, winners));

            respList.add(respVO);
        }

        return success(new PageResult<>(respList, ticketPage.getTotal()));
    }

    // ==================== 兼容旧端点 ====================

    @GetMapping("/list")
    @Operation(summary = "获得活跃奖池列表（兼容端点，建议使用 /active）")
    public CommonResult<AppActivePoolRespVO> getPoolList() {
        return getActivePool();
    }

    @GetMapping("/get")
    @Operation(summary = "获得奖池详情（兼容端点，建议使用 /detail）")
    @Parameter(name = "poolId", description = "链上奖池 ID", required = true, example = "1")
    public CommonResult<AppPoolDetailRespVO> getPool(@RequestParam("poolId") Long poolId) {
        return getPoolDetail(poolId);
    }

    // ==================== 内部辅助方法 ====================

    /**
     * 获取当前登录用户的钱包地址（可选）
     */
    private String getOptionalUserAddress() {
        Long userId = getLoginUserId();
        if (userId == null) {
            return null;
        }
        try {
            WalletUserDO walletUser = walletUserService.getUser(userId);
            return walletUser != null ? walletUser.getWalletAddress() : null;
        } catch (Exception e) {
            log.warn("获取用户钱包地址失败: userId={}", userId, e);
            return null;
        }
    }

    /**
     * wei 字符串转 MON 展示字符串
     */
    private String weiToDisplay(String wei) {
        if (wei == null || wei.isEmpty() || "0".equals(wei)) {
            return "0";
        }
        try {
            return new BigDecimal(wei).divide(WEI_DECIMALS, 18, RoundingMode.DOWN)
                    .stripTrailingZeros().toPlainString();
        } catch (Exception e) {
            return "0";
        }
    }

    /**
     * 计算总奖金池 (wei)
     */
    private BigInteger calcTotalPrize(TreasurePoolDO pool) {
        BigInteger ticketRevenue = new BigInteger(pool.getPrice()).multiply(BigInteger.valueOf(pool.getSoldShares()));
        BigInteger initialPrize = pool.getInitialPrize() != null ? new BigInteger(pool.getInitialPrize()) : BigInteger.ZERO;
        return ticketRevenue.add(initialPrize);
    }

    /**
     * 计算预估每人奖金 (MON display)
     */
    private String calcEstimatedPrizePerWinner(TreasurePoolDO pool) {
        if (pool.getWinnerCount() == 0 || pool.getSoldShares() == 0) {
            return "0";
        }
        BigDecimal ticketRevenue = new BigDecimal(pool.getPrice())
                .multiply(BigDecimal.valueOf(pool.getSoldShares()));
        BigDecimal initialPrize = pool.getInitialPrize() != null ? new BigDecimal(pool.getInitialPrize()) : BigDecimal.ZERO;
        BigDecimal totalPrize = ticketRevenue.add(initialPrize);
        BigDecimal netPrize = totalPrize.multiply(BigDecimal.ONE.subtract(PLATFORM_FEE_RATE));
        BigDecimal perWinner = netPrize.divide(BigDecimal.valueOf(pool.getWinnerCount()), 18, RoundingMode.DOWN);
        return perWinner.divide(WEI_DECIMALS, 18, RoundingMode.DOWN)
                .stripTrailingZeros().toPlainString();
    }


    // ===== VO 构建 =====

    private AppActivePoolRespVO buildActivePoolResp(TreasurePoolDO pool) {
        BigInteger totalPrize = calcTotalPrize(pool);
        return AppActivePoolRespVO.builder()
                .poolId(pool.getPoolId())
                .contractAddress(pool.getContractAddress())
                .chainId(pool.getChainId())
                .price(pool.getPrice())
                .priceDisplay(weiToDisplay(pool.getPrice()))
                .totalShares(pool.getTotalShares())
                .soldShares(pool.getSoldShares())
                .winnerCount(pool.getWinnerCount())
                .endTime(pool.getEndTime())
                .status(pool.getStatus())
                .createTime(pool.getCreateTime())
                .totalPrize(totalPrize.toString())
                .totalPrizeDisplay(weiToDisplay(totalPrize.toString()))
                .estimatedPrizePerWinner(calcEstimatedPrizePerWinner(pool))
                .build();
    }

    private AppPoolHistoryRespVO buildHistoryPoolResp(TreasurePoolDO pool) {
        BigInteger totalPrize = calcTotalPrize(pool);
        return AppPoolHistoryRespVO.builder()
                .poolId(pool.getPoolId())
                .price(pool.getPrice())
                .priceDisplay(weiToDisplay(pool.getPrice()))
                .totalShares(pool.getTotalShares())
                .soldShares(pool.getSoldShares())
                .winnerCount(pool.getWinnerCount())
                .endTime(pool.getEndTime())
                .status(pool.getStatus())
                .createTime(pool.getCreateTime())
                .totalPrize(totalPrize.toString())
                .totalPrizeDisplay(weiToDisplay(totalPrize.toString()))
                .build();
    }

    private AppPoolDetailRespVO buildDetailResp(TreasurePoolDO pool) {
        BigInteger totalPrize = calcTotalPrize(pool);
        return AppPoolDetailRespVO.builder()
                .poolId(pool.getPoolId())
                .contractAddress(pool.getContractAddress())
                .chainId(pool.getChainId())
                .price(pool.getPrice())
                .priceDisplay(weiToDisplay(pool.getPrice()))
                .totalShares(pool.getTotalShares())
                .soldShares(pool.getSoldShares())
                .winnerCount(pool.getWinnerCount())
                .endTime(pool.getEndTime())
                .status(pool.getStatus())
                .createTime(pool.getCreateTime())
                .totalPrize(totalPrize.toString())
                .totalPrizeDisplay(weiToDisplay(totalPrize.toString()))
                .build();
    }

    private AppSettlementVO buildSettlement(TreasurePoolDO pool, List<TreasureWinnerDO> winners) {
        long claimed = winners.stream().filter(w -> Boolean.TRUE.equals(w.getIsClaimed())).count();
        return AppSettlementVO.builder()
                .prizePerWinner(pool.getPrizePerWinner())
                .prizePerWinnerDisplay(weiToDisplay(pool.getPrizePerWinner()))
                .drawTime(pool.getDrawTime())
                .drawTxHash(pool.getDrawTxHash())
                .winnerAddresses(winners.stream()
                        .map(TreasureWinnerDO::getWinnerAddress)
                        .collect(Collectors.toList()))
                .winnerCount(winners.size())
                .totalClaimed((int) claimed)
                .totalUnclaimed(winners.size() - (int) claimed)
                .build();
    }

    private AppUserParticipationVO buildActiveParticipation(TreasureTicketDO ticket) {
        return AppUserParticipationVO.builder()
                .hasJoined(true)
                .ticketIndex(ticket.getTicketIndex())
                .displayCode(ticket.getDisplayCode())
                .purchaseTime(ticket.getPurchaseTime())
                .purchaseTxHash(ticket.getPurchaseTxHash())
                .build();
    }

    private AppUserParticipationVO buildFullParticipation(TreasureTicketDO ticket, List<TreasureWinnerDO> winners) {
        AppUserParticipationVO vo = buildActiveParticipation(ticket);
        // 补充中奖信息
        vo.setIsWinner(Boolean.TRUE.equals(ticket.getIsWinner()));
        if (Boolean.TRUE.equals(ticket.getIsWinner())) {
            // 从 winner 列表中找到对应的中奖记录
            TreasureWinnerDO winnerRecord = winners != null ? winners.stream()
                    .filter(w -> ticket.getOwnerAddress().equalsIgnoreCase(w.getWinnerAddress()))
                    .findFirst()
                    .orElse(null) : null;
            if (winnerRecord != null) {
                vo.setPrizeAmount(winnerRecord.getPrizeAmount());
                vo.setPrizeAmountDisplay(weiToDisplay(winnerRecord.getPrizeAmount()));
                vo.setIsClaimed(Boolean.TRUE.equals(winnerRecord.getIsClaimed()));
                vo.setClaimTime(winnerRecord.getClaimTime());
                vo.setClaimTxHash(winnerRecord.getClaimTxHash());
            } else {
                // fallback to ticket data
                vo.setPrizeAmount(ticket.getPrizeAmount());
                vo.setPrizeAmountDisplay(weiToDisplay(ticket.getPrizeAmount()));
                vo.setIsClaimed(Boolean.TRUE.equals(ticket.getIsClaimed()));
                vo.setClaimTime(ticket.getClaimTime());
                vo.setClaimTxHash(ticket.getClaimTxHash());
            }
        }
        return vo;
    }
}
