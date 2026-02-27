package cn.iocoder.yudao.module.market.service.price;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 价格管理 Service 接口
 * 
 * 统一管理所有价格相关操作，包括：
 * - 从 Redis 缓存读取价格
 * - 从 CLOB API 回退获取价格
 * - 更新价格到缓存（WS 调用）
 */
public interface PmPriceService {

    /**
     * 核心方法：按 tokenId 获取价格
     *
     * @param tokenId Polymarket Token ID (asset_id)
     * @return 价格信息
     */
    PriceInfo getPriceByTokenId(String tokenId);

    /**
     * 按市场ID和选项索引获取价格
     *
     * @param marketId     市场 ID
     * @param outcomeIndex 选项索引 (0=第一个选项, 1=第二个选项...)
     * @return 价格信息
     */
    PriceInfo getPriceByOutcomeIndex(Long marketId, int outcomeIndex);

    /**
     * 按市场ID和选项名称获取价格
     *
     * @param marketId    市场 ID
     * @param outcomeName 选项名称 (Yes/No/Lakers...)
     * @return 价格信息
     */
    PriceInfo getPriceByOutcomeName(Long marketId, String outcomeName);

    /**
     * 获取市场所有选项的价格
     *
     * @param marketId 市场 ID
     * @return 价格映射 key=outcomeIndex, value=PriceInfo
     */
    Map<Integer, PriceInfo> getAllPrices(Long marketId);

    /**
     * 更新价格（由 WsMessageHandler 调用）
     *
     * @param tokenId Token ID
     * @param bestBid 最佳买价
     * @param bestAsk 最佳卖价
     */
    void updatePrice(String tokenId, BigDecimal bestBid, BigDecimal bestAsk);

    /**
     * 更新价格（字符串版本，方便 WS 直接调用）
     *
     * @param tokenId Token ID
     * @param bestBid 最佳买价
     * @param bestAsk 最佳卖价
     */
    void updatePrice(String tokenId, String bestBid, String bestAsk);

}
