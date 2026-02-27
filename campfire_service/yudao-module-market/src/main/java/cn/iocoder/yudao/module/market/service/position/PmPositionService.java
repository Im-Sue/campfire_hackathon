package cn.iocoder.yudao.module.market.service.position;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.market.dal.dataobject.position.PmPositionDO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户持仓 Service 接口
 */
public interface PmPositionService {

    /**
     * 获取持仓
     *
     * @param id 持仓编号
     * @return 持仓
     */
    PmPositionDO getPosition(Long id);

    /**
     * 获取用户在指定市场的指定选项的持仓
     *
     * @param userId   用户 ID
     * @param marketId 市场 ID
     * @param outcome  选项
     * @return 持仓
     */
    PmPositionDO getPosition(Long userId, Long marketId, String outcome);

    /**
     * 获取用户的所有持仓
     *
     * @param userId 用户 ID
     * @return 持仓列表
     */
    List<PmPositionDO> getPositionsByUserId(Long userId);

    /**
     * 分页获取用户持仓
     *
     * @param userId    用户 ID
     * @param pageParam 分页参数
     * @return 分页结果
     */
    PageResult<PmPositionDO> getPositionPageByUserId(Long userId, PageParam pageParam);

    /**
     * 获取市场的所有持仓
     *
     * @param marketId 市场 ID
     * @return 持仓列表
     */
    List<PmPositionDO> getPositionsByMarketId(Long marketId);

    /**
     * 增加持仓（买入时调用）
     *
     * @param userId        用户 ID
     * @param walletAddress 钱包地址
     * @param marketId      市场 ID
     * @param outcome       选项
     * @param quantity      份数
     * @param price         价格
     * @param cost          成本（积分）
     */
    void addPosition(Long userId, String walletAddress, Long marketId, String outcome,
            BigDecimal quantity, BigDecimal price, Long cost);

    /**
     * 减少持仓（卖出时调用）
     *
     * @param userId   用户 ID
     * @param marketId 市场 ID
     * @param outcome  选项
     * @param quantity 份数
     * @param price    价格
     * @return 实现盈亏（积分）
     */
    Long reducePosition(Long userId, Long marketId, String outcome, BigDecimal quantity, BigDecimal price);

    /**
     * 清空持仓（结算时调用）
     *
     * @param positionId 持仓编号
     */
    void clearPosition(Long positionId);

    /**
     * 标记持仓为已结算（结算时调用，保留持仓数据）
     *
     * @param positionId 持仓编号
     */
    void markAsSettled(Long positionId);

    /**
     * 获取市场的净敞口（用于风控检查）
     *
     * @param marketId 市场 ID
     * @return 净敞口 = |Yes总份数 - No总份数|
     */
    BigDecimal getNetExposure(Long marketId);

}
