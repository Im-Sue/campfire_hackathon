package cn.iocoder.yudao.module.treasure.service.pool;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.treasure.controller.admin.pool.vo.TreasurePoolCreateRespVO;
import cn.iocoder.yudao.module.treasure.controller.admin.pool.vo.TreasurePoolPageReqVO;
import cn.iocoder.yudao.module.treasure.dal.dataobject.TreasurePoolDO;

import java.math.BigInteger;

/**
 * 奖池管理 Service 接口
 *
 * @author Sue
 */
public interface TreasurePoolService {

    /**
     * 创建奖池
     *
     * @param price       单价（wei 字符串）
     * @param totalShares 总份数
     * @param duration    持续时长（秒）
     * @param winnerCount 中奖名额
     * @return 创建结果
     */
    TreasurePoolCreateRespVO createPool(String price, Integer totalShares, Integer duration, Integer winnerCount, String initialPrize) throws Exception;

    /**
     * 获取奖池分页列表
     *
     * @param pageReqVO 分页查询条件
     * @return 奖池分页列表
     */
    PageResult<TreasurePoolDO> getPoolPage(TreasurePoolPageReqVO pageReqVO);

    /**
     * 根据 ID 获取奖池详情
     *
     * @param id 奖池 ID
     * @return 奖池详情
     */
    TreasurePoolDO getPool(Long id);

    /**
     * 根据 poolId 获取奖池详情
     *
     * @param poolId 链上奖池 ID
     * @return 奖池详情
     */
    TreasurePoolDO getPoolByPoolId(Long poolId);

    /**
     * 同步链上奖池数据
     *
     * @param poolId 链上奖池 ID
     * @return 同步后的奖池数据
     */
    TreasurePoolDO syncPoolFromChain(BigInteger poolId) throws Exception;

    /**
     * 同步链上奖池数据（带创建交易哈希）
     *
     * @param poolId 链上奖池 ID
     * @param createTxHash 创建交易哈希
     * @return 同步后的奖池数据
     */
    TreasurePoolDO syncPoolFromChain(BigInteger poolId, String createTxHash) throws Exception;

    /**
     * 执行开奖
     *
     * @param poolId 链上奖池 ID
     * @return 交易哈希
     */
    String executeDraw(Long poolId) throws Exception;

    /**
     * 获取活跃奖池列表（用户端）
     *
     * @return 活跃奖池列表
     */
    PageResult<TreasurePoolDO> getActivePoolPage(TreasurePoolPageReqVO pageReqVO);

    /**
     * 获取当前唯一活跃奖池
     *
     * @return 活跃奖池，没有时返回 null
     */
    TreasurePoolDO getFirstActivePool();

    /**
     * 获取历史奖池分页（status≠0）
     *
     * @param pageParam 分页参数
     * @return 历史奖池分页
     */
    PageResult<TreasurePoolDO> getHistoryPoolPage(PageParam pageParam);
}
