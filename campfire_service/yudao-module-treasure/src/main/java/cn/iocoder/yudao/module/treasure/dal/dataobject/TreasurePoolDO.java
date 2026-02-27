package cn.iocoder.yudao.module.treasure.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 夺宝奖池 DO
 *
 * @author Sue
 */
@TableName(value = "treasure_pool", autoResultMap = true)
@KeySequence("treasure_pool_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TreasurePoolDO extends BaseDO {

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
     * 合约地址
     */
    private String contractAddress;

    /**
     * 链ID
     */
    private Integer chainId;

    // ========== 创建交易信息 ==========

    /**
     * 创建交易哈希
     */
    private String createTxHash;

    /**
     * 创建区块高度
     */
    private Long createBlockNumber;

    // ========== 奖池基本信息 ==========

    /**
     * 单价(wei字符串)
     */
    private String price;

    /**
     * 总份数
     */
    private Integer totalShares;

    /**
     * 已售份数
     */
    private Integer soldShares;

    /**
     * 中奖名额
     */
    private Integer winnerCount;

    /**
     * 截止时间
     */
    private LocalDateTime endTime;

    // ========== 奖池状态 ==========

    /**
     * 奖池状态: 0-进行中 1-已锁定 2-开奖中 3-已结算
     */
    private Integer status;

    /**
     * VRF请求ID
     */
    private String randomnessRequestId;

    /**
     * 每人奖金(wei字符串)
     */
    private String prizePerWinner;

    /**
     * 创建者注入的初始奖金(wei字符串)
     */
    private String initialPrize;

    // ========== 开奖信息 ==========

    /**
     * 开奖交易哈希
     */
    private String drawTxHash;

    /**
     * 开奖区块高度
     */
    private Long drawBlockNumber;

    /**
     * 开奖时间
     */
    private LocalDateTime drawTime;
}
