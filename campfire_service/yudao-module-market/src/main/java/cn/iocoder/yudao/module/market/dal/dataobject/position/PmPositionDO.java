package cn.iocoder.yudao.module.market.dal.dataobject.position;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * 用户持仓 DO
 */
@TableName("pm_position")
@KeySequence("pm_position_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PmPositionDO extends BaseDO {

    /**
     * 持仓编号
     */
    @TableId
    private Long id;

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
     * 选项: Yes/No/选项名
     */
    private String outcome;

    /**
     * 持仓份数
     */
    private BigDecimal quantity;

    /**
     * 持仓均价 (USD)
     */
    private BigDecimal avgPrice;

    /**
     * 总成本 (积分)
     */
    private Long totalCost;

    /**
     * 已实现盈亏 (积分)
     */
    private Long realizedPnl;

    /**
     * 是否已结算
     */
    private Boolean settled;

    /**
     * 乐观锁版本（注：未配置 OptimisticLockerInnerInterceptor，暂不使用）
     */
    private Integer version;

}
