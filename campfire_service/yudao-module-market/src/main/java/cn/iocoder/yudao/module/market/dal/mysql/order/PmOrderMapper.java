package cn.iocoder.yudao.module.market.dal.mysql.order;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.market.controller.admin.order.vo.OrderPageReqVO;
import cn.iocoder.yudao.module.market.dal.dataobject.order.PmOrderDO;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 预测市场订单 Mapper
 */
@Mapper
public interface PmOrderMapper extends BaseMapperX<PmOrderDO> {

    default PmOrderDO selectByOrderNo(String orderNo) {
        return selectOne(PmOrderDO::getOrderNo, orderNo);
    }

    default List<PmOrderDO> selectByUserId(Long userId) {
        return selectList(new LambdaQueryWrapperX<PmOrderDO>()
                .eq(PmOrderDO::getUserId, userId)
                .orderByDesc(PmOrderDO::getCreateTime));
    }

    default PageResult<PmOrderDO> selectPageByUserId(Long userId, PageParam pageParam) {
        return selectPage(pageParam, new LambdaQueryWrapperX<PmOrderDO>()
                .eq(PmOrderDO::getUserId, userId)
                .orderByDesc(PmOrderDO::getCreateTime));
    }

    default List<PmOrderDO> selectPendingOrders() {
        return selectList(new LambdaQueryWrapperX<PmOrderDO>()
                .eq(PmOrderDO::getStatus, 0)); // 待成交
    }

    default List<PmOrderDO> selectPendingOrdersByMarketId(Long marketId) {
        return selectList(new LambdaQueryWrapperX<PmOrderDO>()
                .eq(PmOrderDO::getMarketId, marketId)
                .eq(PmOrderDO::getStatus, 0)); // 待成交
    }

    /**
     * 原子更新限价单状态为已成交（防止重复成交）
     * 
     * @param id             订单 ID
     * @param requiredStatus 要求的当前状态（通常是 PENDING）
     * @param filledQuantity 成交数量
     * @param filledAmount   成交金额
     * @param filledPrice    成交价格
     * @param filledAt       成交时间
     * @return 更新行数（0 表示已被其他线程处理）
     */
    default int atomicFillOrder(Long id, Integer requiredStatus, BigDecimal filledQuantity,
            Long filledAmount, java.math.BigDecimal filledPrice,
            java.time.LocalDateTime filledAt) {
        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<PmOrderDO> updateWrapper = new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
        updateWrapper
                .eq(PmOrderDO::getId, id)
                .eq(PmOrderDO::getStatus, requiredStatus)
                .set(PmOrderDO::getStatus, 1) // FILLED
                .set(PmOrderDO::getFilledQuantity, filledQuantity)
                .set(PmOrderDO::getFilledAmount, filledAmount)
                .set(PmOrderDO::getFilledPrice, filledPrice)
                .set(PmOrderDO::getFilledAt, filledAt);
        return update(null, updateWrapper);
    }

    // ========== 链上记账 ==========

    /**
     * 查询待上链的已成交订单（只查 2026-02-26 之后的订单）
     */
    default List<PmOrderDO> selectPendingChainOrders(int limit) {
        return selectList(new LambdaQueryWrapperX<PmOrderDO>()
                .eq(PmOrderDO::getStatus, 1) // FILLED
                .in(PmOrderDO::getChainStatus, 0, 3) // PENDING or FAILED
                .ge(PmOrderDO::getCreateTime, LocalDateTime.of(2026, 2, 26, 0, 0, 0)) // 只上链新订单
                .orderByAsc(PmOrderDO::getId)
                .last("LIMIT " + limit));
    }

    /**
     * 批量更新链上状态
     */
    default void updateChainStatus(List<Long> ids, Integer chainStatus,
                                   Long batchId, String txHash) {
        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<PmOrderDO> wrapper =
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
        wrapper.in(PmOrderDO::getId, ids)
                .set(PmOrderDO::getChainStatus, chainStatus);
        if (batchId != null) {
            wrapper.set(PmOrderDO::getChainBatchId, batchId);
        }
        if (txHash != null) {
            wrapper.set(PmOrderDO::getChainTxHash, txHash);
        }
        update(null, wrapper);
    }

    // ========== 管理端 ==========

    /**
     * 管理端分页查询订单
     */
    default PageResult<PmOrderDO> selectPage(OrderPageReqVO pageReqVO) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<PmOrderDO>()
                .likeIfPresent(PmOrderDO::getWalletAddress, pageReqVO.getWalletAddress())
                .likeIfPresent(PmOrderDO::getOrderNo, pageReqVO.getOrderNo())
                .eqIfPresent(PmOrderDO::getUserId, pageReqVO.getUserId())
                .eqIfPresent(PmOrderDO::getMarketId, pageReqVO.getMarketId())
                .eqIfPresent(PmOrderDO::getOrderType, pageReqVO.getOrderType())
                .eqIfPresent(PmOrderDO::getSide, pageReqVO.getSide())
                .eqIfPresent(PmOrderDO::getStatus, pageReqVO.getStatus())
                .betweenIfPresent(PmOrderDO::getCreateTime, pageReqVO.getCreateTime())
                .orderByDesc(PmOrderDO::getCreateTime));
    }

}
