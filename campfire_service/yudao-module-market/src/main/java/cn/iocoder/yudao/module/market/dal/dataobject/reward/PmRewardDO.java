package cn.iocoder.yudao.module.market.dal.dataobject.reward;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户奖励记录 DO
 */
@TableName("pm_reward")
@KeySequence("pm_reward_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PmRewardDO extends BaseDO {

    /**
     * 奖励编号
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
     * 结算记录 ID
     */
    private Long settlementId;

    /**
     * 持仓 ID
     */
    private Long positionId;

    /**
     * 持仓选项
     */
    private String outcome;

    /**
     * 持仓份数
     */
    private BigDecimal quantity;

    /**
     * 奖励积分（获胜: 每份100积分 × 份数，失败: 0）
     */
    private Long rewardAmount;

    /**
     * 状态: 0-待领取 1-已领取 2-失败
     */
    private Integer status;

    /**
     * 领取时间
     */
    private LocalDateTime claimedAt;

}
