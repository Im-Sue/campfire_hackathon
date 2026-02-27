package cn.iocoder.yudao.module.point.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 积分账户 DO
 */
@TableName("point_account")
@KeySequence("point_account_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointAccountDO extends BaseDO {

    /**
     * 主键 ID
     */
    @TableId
    private Long id;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 钱包地址（冗余存储，方便按地址查询）
     */
    private String walletAddress;

    /**
     * 可用积分
     */
    private Long availablePoints;

    /**
     * 累计获得
     */
    private Long totalEarned;

    /**
     * 累计消费
     */
    private Long totalSpent;

    /**
     * 乐观锁版本（预留，注：未配置 OptimisticLockerInnerInterceptor，暂不使用 @Version）
     */
    private Integer version;

}
