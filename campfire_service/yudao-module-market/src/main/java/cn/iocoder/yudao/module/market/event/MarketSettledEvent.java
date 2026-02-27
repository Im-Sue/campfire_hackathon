package cn.iocoder.yudao.module.market.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 市场结算完成事件
 * 当管理员执行结算操作完成后触发,用于通知Agent模块进行结算
 *
 * @author Sue
 */
@Getter
public class MarketSettledEvent extends ApplicationEvent {

    /**
     * 市场ID
     */
    private final Long marketId;

    /**
     * 事件ID
     */
    private final Long eventId;

    /**
     * 获胜选项
     */
    private final String winnerOutcome;

    /**
     * 结算ID
     */
    private final Long settlementId;

    public MarketSettledEvent(Object source, Long marketId, Long eventId,
                              String winnerOutcome, Long settlementId) {
        super(source);
        this.marketId = marketId;
        this.eventId = eventId;
        this.winnerOutcome = winnerOutcome;
        this.settlementId = settlementId;
    }
}
