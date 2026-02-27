package cn.iocoder.yudao.module.treasure.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 夺宝中奖记录 DO
 *
 * @author Sue
 */
@TableName(value = "treasure_winner", autoResultMap = true)
@KeySequence("treasure_winner_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TreasureWinnerDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 链上奖池ID
     */
    private Long poolId;

    /**
     * 票号ID
     */
    private Long ticketId;

    /**
     * 合约地址
     */
    private String contractAddress;

    /**
     * 链ID
     */
    private Integer chainId;

    // ========== 中奖信息 ==========

    /**
     * 中奖者地址
     */
    private String winnerAddress;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 中奖票号
     */
    private Integer ticketIndex;

    /**
     * 奖金金额(wei字符串)
     */
    private String prizeAmount;

    // ========== 领奖状态 ==========

    /**
     * 是否已领奖
     */
    private Boolean isClaimed;

    /**
     * 领奖交易哈希
     */
    private String claimTxHash;

    /**
     * 领奖时间
     */
    private LocalDateTime claimTime;
}
