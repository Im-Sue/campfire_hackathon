package cn.iocoder.yudao.module.treasure.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 夺宝票号 DO
 *
 * @author Sue
 */
@TableName(value = "treasure_ticket", autoResultMap = true)
@KeySequence("treasure_ticket_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TreasureTicketDO extends BaseDO {

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
     * 票号索引
     */
    private Integer ticketIndex;

    /**
     * 合约地址
     */
    private String contractAddress;

    /**
     * 链ID
     */
    private Integer chainId;

    // ========== 票号信息 ==========

    /**
     * 票号所有者地址
     */
    private String ownerAddress;

    /**
     * 用户ID（关联系统用户表）
     */
    private Long userId;

    /**
     * 展示码
     */
    private String displayCode;

    // ========== 中奖信息 ==========

    /**
     * 是否中奖
     */
    private Boolean isWinner;

    /**
     * 奖金金额(wei字符串)
     */
    private String prizeAmount;

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

    // ========== 购买信息 ==========

    /**
     * 购买交易哈希
     */
    private String purchaseTxHash;

    /**
     * 购买区块高度
     */
    private Long purchaseBlockNumber;

    /**
     * 购买时间
     */
    private LocalDateTime purchaseTime;
}
