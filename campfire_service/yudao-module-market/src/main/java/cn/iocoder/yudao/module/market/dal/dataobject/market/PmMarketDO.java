package cn.iocoder.yudao.module.market.dal.dataobject.market;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 预测市场信息 DO
 */
@TableName(value = "pm_market", autoResultMap = true)
@KeySequence("pm_market_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PmMarketDO extends BaseDO {

    /**
     * 市场编号
     */
    @TableId
    private Long id;

    /**
     * 关联 Event ID
     */
    private Long eventId;

    /**
     * Polymarket 市场 ID
     */
    private String polymarketId;

    /**
     * Condition ID (WS 订阅用)
     */
    private String conditionId;

    /**
     * Token IDs (WS 订阅用)
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> clobTokenIds;

    /**
     * 市场问题
     */
    private String question;

    /**
     * 选项列表 ["Yes","No"] 或 ["队伍名"]
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> outcomes;

    /**
     * 分组标题
     */
    private String groupItemTitle;

    /**
     * 状态: 0-待上架 1-交易中 2-封盘 3-待结算 4-已结算
     */
    private Integer status;

    /**
     * 获胜选项
     */
    private String winnerOutcome;

    /**
     * 结算时间
     */
    private LocalDateTime settledAt;

    /**
     * 开始时间
     */
    private LocalDateTime startDate;

    /**
     * 结束时间
     */
    private LocalDateTime endDate;

    // ========== 体育盘口 ==========

    /**
     * 盘口类型: moneyline, spreads, totals
     */
    private String sportsMarketType;

    /**
     * 盘口线值
     */
    private BigDecimal line;

    /**
     * 比赛开始时间
     */
    private LocalDateTime gameStartTime;

    /**
     * 是否互斥
     */
    private Boolean negRisk;

}
