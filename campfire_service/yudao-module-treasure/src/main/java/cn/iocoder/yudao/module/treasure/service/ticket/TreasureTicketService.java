package cn.iocoder.yudao.module.treasure.service.ticket;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.treasure.controller.admin.ticket.vo.TreasureTicketPageReqVO;
import cn.iocoder.yudao.module.treasure.dal.dataobject.TreasureTicketDO;

import java.math.BigInteger;

/**
 * 票号管理 Service 接口
 *
 * @author Sue
 */
public interface TreasureTicketService {

    /**
     * 获取票号分页列表
     *
     * @param pageReqVO 分页查询条件
     * @return 票号分页列表
     */
    PageResult<TreasureTicketDO> getTicketPage(TreasureTicketPageReqVO pageReqVO);

    /**
     * 根据 ID 获取票号详情
     *
     * @param id 票号 ID
     * @return 票号详情
     */
    TreasureTicketDO getTicket(Long id);

    /**
     * 获取用户在指定奖池的票号
     *
     * @param userAddress 用户地址
     * @param poolId 奖池 ID
     * @return 票号信息
     */
    TreasureTicketDO getUserTicket(String userAddress, Long poolId);

    /**
     * 获取用户的票号列表
     *
     * @param userAddress 用户地址
     * @param pageReqVO 分页查询条件
     * @return 票号分页列表
     */
    PageResult<TreasureTicketDO> getUserTicketPage(String userAddress, TreasureTicketPageReqVO pageReqVO);
}
