package cn.iocoder.yudao.module.market.dal.dataobject.event;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 预测市场事件 DO
 */
@TableName(value = "pm_event", autoResultMap = true)
@KeySequence("pm_event_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PmEventDO extends BaseDO {

    /**
     * 事件编号
     */
    @TableId
    private Long id;

    /**
     * Polymarket Event ID
     */
    private String polymarketEventId;

    /**
     * Polymarket Ticker
     */
    private String ticker;

    /**
     * URL Slug
     */
    private String slug;

    /**
     * 事件标题
     */
    private String title;

    /**
     * 封面图
     */
    private String imageUrl;

    /**
     * 主分类: politics, sports, crypto, culture, business
     */
    private String category;

    /**
     * 标签数组
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> tags;

    /**
     * 包含的 Market 数量
     */
    private Integer marketCount;

    /**
     * 状态: 0-待上架 1-已上架 2-已下架
     */
    private Integer status;

    /**
     * 是否互斥选项
     */
    private Boolean negRisk;

    /**
     * 开始时间
     */
    private LocalDateTime startDate;

    /**
     * 结束时间
     */
    private LocalDateTime endDate;

    // ========== 联赛关联（体育） ==========

    /**
     * Series ID
     */
    private String seriesId;

    /**
     * Series Slug
     */
    private String seriesSlug;

    // ========== 体育静态信息 ==========

    /**
     * 比赛唯一 ID
     */
    private Integer gameId;

    /**
     * 主队
     */
    private String homeTeamName;

    /**
     * 客队
     */
    private String awayTeamName;

    /**
     * 比赛日期
     */
    private LocalDate eventDate;

    /**
     * 赛季周数
     */
    private Integer eventWeek;

}
