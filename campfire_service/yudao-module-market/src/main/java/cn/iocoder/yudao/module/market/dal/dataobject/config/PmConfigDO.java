package cn.iocoder.yudao.module.market.dal.dataobject.config;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 预测市场配置 DO
 */
@TableName("pm_config")
@KeySequence("pm_config_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PmConfigDO extends BaseDO {

    /**
     * 配置编号
     */
    @TableId
    private Long id;

    /**
     * 配置键
     */
    private String configKey;

    /**
     * 配置值
     */
    private String configValue;

    /**
     * 类型: STRING, INTEGER, DECIMAL, JSON, BOOLEAN
     */
    private String configType;

    /**
     * 配置说明
     */
    private String description;

}
