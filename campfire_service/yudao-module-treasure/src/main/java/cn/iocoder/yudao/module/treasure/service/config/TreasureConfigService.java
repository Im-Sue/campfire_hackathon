package cn.iocoder.yudao.module.treasure.service.config;

import cn.iocoder.yudao.module.treasure.dal.dataobject.TreasureConfigDO;

import java.util.List;

/**
 * 夺宝模块配置 Service 接口
 *
 * @author Sue
 */
public interface TreasureConfigService {

    /**
     * 获取字符串配置值
     *
     * @param key 配置键
     * @return 配置值
     */
    String getString(String key);

    /**
     * 获取长整型配置值
     *
     * @param key 配置键
     * @return 配置值
     */
    Long getLong(String key);

    /**
     * 获取布尔配置值
     *
     * @param key 配置键
     * @return 配置值
     */
    Boolean getBoolean(String key);

    /**
     * 获取整型配置值
     *
     * @param key 配置键
     * @return 配置值
     */
    Integer getInteger(String key);

    /**
     * 更新配置
     *
     * @param key   配置键
     * @param value 配置值
     */
    void updateConfig(String key, String value);

    /**
     * 获取所有配置列表
     *
     * @return 配置列表
     */
    List<TreasureConfigDO> listConfigs();

    /**
     * 刷新缓存
     */
    void refreshCache();

    // ========== 便捷方法 ==========

    /** 获取合约地址 */
    String getContractAddress();

    /** 获取链 ID */
    Integer getChainId();

    /** 获取 VRF 合约地址 */
    String getSwitchboardVrfAddress();

    /** 获取 VRF 费用 */
    String getVrfFee();

    /** 获取 Gas 限制 */
    Long getGasLimit();

    /** 是否启用事件同步 */
    Boolean isEventSyncEnabled();

    /** 获取事件同步间隔 */
    Long getEventSyncInterval();

    /** 获取区块批次大小 */
    Integer getBlockBatchSize();
}
