package cn.iocoder.yudao.module.market.service.chain;

/**
 * 下注链上记账服务接口
 *
 * @author Sue
 */
public interface BetChainService {

    /**
     * 批量同步待上链的已成交订单到链上
     * <p>
     * 1. 查询待上链订单（chain_status=0 或 =3 且 retry<max）
     * 2. 计算 betHash
     * 3. 调用合约 recordBets
     * 4. 更新订单和批次状态
     */
    void syncPendingOrders();

}
