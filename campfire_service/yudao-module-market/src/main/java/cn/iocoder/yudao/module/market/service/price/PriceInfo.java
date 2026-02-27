package cn.iocoder.yudao.module.market.service.price;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 价格信息 DTO
 */
@Data
@Accessors(chain = true)
public class PriceInfo {

    /**
     * Token ID (Polymarket asset_id)
     */
    private String tokenId;

    /**
     * 选项名称 (Yes/No/Lakers...)
     */
    private String outcomeName;

    /**
     * 选项索引 (0, 1, 2...)
     */
    private Integer outcomeIndex;

    /**
     * 最佳买价 (best_bid) - 有人愿意以此价格买入
     */
    private BigDecimal bestBid;

    /**
     * 最佳卖价 (best_ask) - 有人愿意以此价格卖出
     */
    private BigDecimal bestAsk;

    /**
     * 中间价 (bid + ask) / 2
     */
    private BigDecimal midPrice;

    /**
     * 价差 (ask - bid)
     */
    private BigDecimal spread;

    /**
     * 更新时间戳 (毫秒)
     */
    private Long updateTime;

    /**
     * 计算中间价和价差
     */
    public PriceInfo calculate() {
        if (bestBid != null && bestAsk != null) {
            this.midPrice = bestBid.add(bestAsk)
                    .divide(BigDecimal.valueOf(2), 4, java.math.RoundingMode.HALF_UP);
            this.spread = bestAsk.subtract(bestBid);
        }
        return this;
    }

    /**
     * 是否有有效价格
     */
    public boolean isValid() {
        return bestBid != null || bestAsk != null;
    }

}
