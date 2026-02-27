package cn.iocoder.yudao.module.point.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.point.enums.PointBizTypeEnum;
import cn.iocoder.yudao.module.point.enums.PointTransactionTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.util.Map;

/**
 * 积分流水 DO
 */
@TableName(value = "point_transaction", autoResultMap = true)
@KeySequence("point_transaction_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointTransactionDO extends BaseDO {

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
     * 类型
     *
     * 枚举 {@link PointTransactionTypeEnum}
     */
    private Integer type;

    /**
     * 变动金额（正为增加，负为减少）
     */
    private Long amount;

    /**
     * 变动前余额
     */
    private Long beforeBalance;

    /**
     * 变动后余额
     */
    private Long afterBalance;

    /**
     * 业务类型
     *
     * 枚举 {@link PointBizTypeEnum}
     */
    private String bizType;

    /**
     * 业务 ID
     */
    private Long bizId;

    /**
     * 扩展信息（盈亏、成交价等）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extension;

    /**
     * 备注
     */
    private String remark;

}
