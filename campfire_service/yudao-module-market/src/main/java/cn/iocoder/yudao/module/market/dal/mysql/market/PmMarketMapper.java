package cn.iocoder.yudao.module.market.dal.mysql.market;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.market.dal.dataobject.market.PmMarketDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 预测市场信息 Mapper
 */
@Mapper
public interface PmMarketMapper extends BaseMapperX<PmMarketDO> {

    default PmMarketDO selectByPolymarketId(String polymarketId) {
        return selectOne(PmMarketDO::getPolymarketId, polymarketId);
    }

    /**
     * 根据事件 ID 查询市场列表，带多级排序
     * 排序规则：
     * 1. 状态：交易中(1) > 封盘(2) > 待结算(3) > 已结算(4)
     * 2. 盘口类型：moneyline > spreads > totals（通过 FIELD 函数）
     * 3. 盘口线值：升序
     * 4. 结束时间：升序（即将结束的优先）
     * 5. ID：升序（保证稳定性）
     */
    default List<PmMarketDO> selectByEventId(Long eventId) {
        return selectList(new LambdaQueryWrapperX<PmMarketDO>()
                .eq(PmMarketDO::getEventId, eventId)
                .orderByAsc(PmMarketDO::getStatus)
                .orderByAsc(PmMarketDO::getLine)
                .orderByAsc(PmMarketDO::getEndDate)
                .orderByAsc(PmMarketDO::getId));
    }

    default List<PmMarketDO> selectByStatus(Integer status) {
        return selectList(PmMarketDO::getStatus, status);
    }

    default List<PmMarketDO> selectTradingMarkets() {
        return selectList(new LambdaQueryWrapperX<PmMarketDO>()
                .eq(PmMarketDO::getStatus, 1)); // 交易中
    }

    default List<PmMarketDO> selectPendingSettlementMarkets() {
        return selectList(new LambdaQueryWrapperX<PmMarketDO>()
                .eq(PmMarketDO::getStatus, 3)); // 待结算
    }

    default List<PmMarketDO> selectSuspendedMarkets() {
        return selectList(new LambdaQueryWrapperX<PmMarketDO>()
                .eq(PmMarketDO::getStatus, 2)); // 已封盘
    }

    /**
     * 根据 clobTokenId 查询市场
     * clobTokenIds 是 JSON 数组格式存储，使用 LIKE 模糊匹配
     */
    default PmMarketDO selectByTokenId(String tokenId) {
        if (tokenId == null || tokenId.isEmpty()) {
            return null;
        }
        return selectOne(new LambdaQueryWrapperX<PmMarketDO>()
                .like(PmMarketDO::getClobTokenIds, tokenId));
    }

    /**
     * 根据市场问题模糊搜索，返回关联的事件 ID 列表
     *
     * @param question 搜索关键词
     * @return 事件 ID 列表（去重）
     */
    default List<Long> selectEventIdsByQuestionLike(String question) {
        return selectList(new LambdaQueryWrapperX<PmMarketDO>()
                .likeIfPresent(PmMarketDO::getQuestion, question))
                .stream()
                .map(PmMarketDO::getEventId)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 查询有活跃市场（TRADING 状态）的事件 ID 列表
     *
     * @return 事件 ID 列表（去重）
     */
    default List<Long> selectActiveEventIds() {
        return selectList(new LambdaQueryWrapperX<PmMarketDO>()
                .eq(PmMarketDO::getStatus, 1)) // TRADING
                .stream()
                .map(PmMarketDO::getEventId)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
    }

}
