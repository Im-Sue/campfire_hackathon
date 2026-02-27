package cn.iocoder.yudao.module.treasure.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 夺宝事件同步 DO
 *
 * @author Sue
 */
@TableName(value = "treasure_event_sync", autoResultMap = true)
@KeySequence("treasure_event_sync_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TreasureEventSyncDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 合约地址
     */
    private String contractAddress;

    /**
     * 链ID
     */
    private Integer chainId;

    /**
     * 事件类型: PoolCreated/TicketPurchased/DrawStarted/DrawCompleted/PrizeClaimed
     */
    private String eventType;

    // ========== 事件信息 ==========

    /**
     * 交易哈希
     */
    private String txHash;

    /**
     * 区块高度
     */
    private Long blockNumber;

    /**
     * 日志索引
     */
    private Integer logIndex;

    /**
     * 事件数据(JSON)
     */
    private String eventData;

    // ========== 处理状态 ==========

    /**
     * 同步状态: 0-待处理 1-处理中 2-已完成 3-失败
     */
    private Integer syncStatus;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 处理时间
     */
    private LocalDateTime processedTime;
}
