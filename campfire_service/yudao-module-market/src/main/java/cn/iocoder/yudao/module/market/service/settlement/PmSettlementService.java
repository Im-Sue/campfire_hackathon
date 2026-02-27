package cn.iocoder.yudao.module.market.service.settlement;

import cn.iocoder.yudao.module.market.dal.dataobject.settlement.PmSettlementDO;

import java.util.List;

/**
 * 市场结算 Service 接口
 */
public interface PmSettlementService {

    /**
     * 创建结算记录
     *
     * @param marketId      市场 ID
     * @param polymarketId  Polymarket ID
     * @param winnerOutcome 获胜选项
     * @param source        数据来源
     * @return 结算记录编号
     */
    Long createSettlement(Long marketId, String polymarketId, String winnerOutcome, String source);

    /**
     * 获取结算记录
     *
     * @param id 结算记录编号
     * @return 结算记录
     */
    PmSettlementDO getSettlement(Long id);

    /**
     * 根据市场 ID 获取结算记录
     *
     * @param marketId 市场 ID
     * @return 结算记录
     */
    PmSettlementDO getSettlementByMarketId(Long marketId);

    /**
     * 获取待确认的结算列表
     *
     * @return 结算列表
     */
    List<PmSettlementDO> getPendingSettlements();

    /**
     * 分页查询结算记录
     *
     * @param pageReqVO 分页请求
     * @return 分页结果
     */
    cn.iocoder.yudao.framework.common.pojo.PageResult<PmSettlementDO> getSettlementPage(
            cn.iocoder.yudao.module.market.controller.admin.settlement.vo.SettlementPageReqVO pageReqVO);

    /**
     * 管理员确认结算
     *
     * @param id      结算记录编号
     * @param adminId 管理员 ID
     */
    void confirmSettlement(Long id, Long adminId);

    /**
     * 执行结算（确认后调用，生成奖励记录）
     *
     * @param id 结算记录编号
     */
    void executeSettlement(Long id);

    /**
     * 获取已确认待执行的结算列表
     *
     * @return 结算列表
     */
    List<PmSettlementDO> getConfirmedSettlements();

}

