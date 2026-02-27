package cn.iocoder.yudao.module.market.dal.dataobject.order;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预测市场订单 DO
 */
@TableName("pm_order")
@KeySequence("pm_order_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PmOrderDO extends BaseDO {

    /**
     * 订单编号
     */
    @TableId
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 钱包地址
     */
    private String walletAddress;

    /**
     * 市场 ID
     */
    private Long marketId;

    /**
     * 订单类型: 1-市价单 2-限价单
     */
    private Integer orderType;

    /**
     * 方向: 1-买入 2-卖出
     */
    private Integer side;

    /**
     * 选项: Yes/No/选项名
     */
    private String outcome;

    /**
     * 价格 (USD): 市价单=成交价, 限价单=挂单价
     */
    private BigDecimal price;

    /**
     * 份数
     */
    private BigDecimal quantity;

    /**
     * 金额 (积分)
     */
    private Long amount;

    /**
     * 滑点容忍度
     */
    private BigDecimal slippageTolerance;

    /**
     * 已成交份数
     */
    private BigDecimal filledQuantity;

    /**
     * 已成交金额 (积分)
     */
    private Long filledAmount;

    /**
     * 成交价格 (USD)
     */
    private BigDecimal filledPrice;

    /**
     * 成交时间
     */
    private LocalDateTime filledAt;

    /**
     * 状态: 0-待成交 1-已成交 2-部分成交 3-已取消 4-已失效
     */
    private Integer status;

    /**
     * 过期时间（限价单）
     */
    private LocalDateTime expireAt;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 链上状态: 0-待上链 1-上链中 2-已上链 3-上链失败
     */
    private Integer chainStatus;

    /**
     * 链上交易哈希
     */
    private String chainTxHash;

    /**
     * 链上批次ID
     */
    private Long chainBatchId;

}
