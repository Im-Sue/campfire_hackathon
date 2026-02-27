package cn.iocoder.yudao.module.market.service.order;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.market.controller.admin.order.vo.OrderPageReqVO;
import cn.iocoder.yudao.module.market.controller.app.order.vo.AppOrderCreateReqVO;
import cn.iocoder.yudao.module.market.dal.dataobject.order.PmOrderDO;

import java.util.List;

/**
 * 预测市场订单 Service 接口
 */
public interface PmOrderService {

    /**
     * 创建订单（市价单/限价单）
     *
     * @param userId      用户 ID
     * @param createReqVO 创建请求
     * @return 订单编号
     */
    Long createOrder(Long userId, AppOrderCreateReqVO createReqVO);

    /**
     * 取消订单
     *
     * @param userId  用户 ID
     * @param orderId 订单编号
     */
    void cancelOrder(Long userId, Long orderId);

    /**
     * 获取订单
     *
     * @param id 订单编号
     * @return 订单
     */
    PmOrderDO getOrder(Long id);

    /**
     * 根据订单号获取订单
     *
     * @param orderNo 订单号
     * @return 订单
     */
    PmOrderDO getOrderByOrderNo(String orderNo);

    /**
     * 获取用户的订单列表
     *
     * @param userId 用户 ID
     * @return 订单列表
     */
    List<PmOrderDO> getOrdersByUserId(Long userId);

    /**
     * 分页获取用户订单
     *
     * @param userId    用户 ID
     * @param pageParam 分页参数
     * @return 分页结果
     */
    PageResult<PmOrderDO> getOrderPageByUserId(Long userId, PageParam pageParam);

    /**
     * 处理限价单成交（价格满足条件时调用）
     *
     * @param marketId      市场 ID
     * @param outcomePrices 各选项的当前价格 {outcomeName: price}
     */
    void processLimitOrders(Long marketId, java.util.Map<String, java.math.BigDecimal> outcomePrices);

    /**
     * 处理过期的限价单
     */
    void processExpiredOrders();

    // ========== 管理端 ==========

    /**
     * 管理端分页获取订单列表
     *
     * @param pageReqVO 分页请求
     * @return 分页结果
     */
    PageResult<PmOrderDO> getOrderPage(OrderPageReqVO pageReqVO);

}
