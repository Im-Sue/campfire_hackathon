package cn.iocoder.yudao.module.market.dal.dataobject.chain;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 链上批次记录 DO
 */
@TableName("pm_chain_batch")
@KeySequence("pm_chain_batch_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PmChainBatchDO extends BaseDO {

    /**
     * 批次 ID
     */
    @TableId
    private Long id;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 包含订单数
     */
    private Integer orderCount;

    /**
     * 链上交易哈希
     */
    private String txHash;

    /**
     * 区块号
     */
    private Long blockNumber;

    /**
     * 状态: 0-待提交 1-已提交 2-已确认 3-失败
     */
    private Integer status;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 提交时间
     */
    private LocalDateTime submittedAt;

    /**
     * 确认时间
     */
    private LocalDateTime confirmedAt;

}
