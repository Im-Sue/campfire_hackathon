package cn.iocoder.yudao.module.treasure.service.winner;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.treasure.controller.admin.winner.vo.TreasureWinnerPageReqVO;
import cn.iocoder.yudao.module.treasure.dal.dataobject.TreasureWinnerDO;

/**
 * 中奖记录管理 Service 接口
 *
 * @author Sue
 */
public interface TreasureWinnerService {

    /**
     * 获取中奖记录分页列表
     *
     * @param pageReqVO 分页查询条件
     * @return 中奖记录分页列表
     */
    PageResult<TreasureWinnerDO> getWinnerPage(TreasureWinnerPageReqVO pageReqVO);

    /**
     * 根据 ID 获取中奖记录详情
     *
     * @param id 中奖记录 ID
     * @return 中奖记录详情
     */
    TreasureWinnerDO getWinner(Long id);

    /**
     * 获取指定奖池的中奖记录列表
     *
     * @param poolId 奖池 ID
     * @return 中奖记录列表
     */
    PageResult<TreasureWinnerDO> getPoolWinnerPage(Long poolId, TreasureWinnerPageReqVO pageReqVO);

    /**
     * 获取用户的中奖记录列表
     *
     * @param userAddress 用户地址
     * @param pageReqVO 分页查询条件
     * @return 中奖记录分页列表
     */
    PageResult<TreasureWinnerDO> getUserWinnerPage(String userAddress, TreasureWinnerPageReqVO pageReqVO);
}
