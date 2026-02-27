package cn.iocoder.yudao.module.market.dal.dataobject.settlement;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 市场结算记录 DO
 */
@TableName("pm_settlement")
@KeySequence("pm_settlement_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PmSettlementDO extends BaseDO {

    /**
     * 结算编号
     */
    @TableId
    private Long id;

    /**
     * 市场 ID
     */
    private Long marketId;

    /**
     * Polymarket 市场 ID
     */
    private String polymarketId;

    /**
     * 获胜选项
     */
    private String winnerOutcome;

    /**
     * 结算数据来源
     */
    private String source;

    /**
     * 状态: 0-待确认 1-已确认 2-已完成
     */
    private Integer status;

    /**
     * 确认管理员 ID
     */
    private Long confirmedBy;

    /**
     * 确认时间
     */
    private LocalDateTime confirmedAt;

    /**
     * 完成时间
     */
    private LocalDateTime completedAt;

    /**
     * 总持仓数
     */
    private Integer totalPositions;

    /**
     * 获胜持仓数
     */
    private Integer winningPositions;

    /**
     * 失败持仓数
     */
    private Integer losingPositions;

    /**
     * 总奖励积分
     */
    private Long totalReward;

}
