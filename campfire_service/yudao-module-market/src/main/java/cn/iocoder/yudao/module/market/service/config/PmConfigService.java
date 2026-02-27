package cn.iocoder.yudao.module.market.service.config;

import java.math.BigDecimal;

/**
 * 预测市场配置 Service 接口
 */
public interface PmConfigService {

    /**
     * 获取字符串配置值
     *
     * @param key 配置键
     * @return 配置值
     */
    String getString(String key);

    /**
     * 获取字符串配置值，带默认值
     *
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    String getString(String key, String defaultValue);

    /**
     * 获取整数配置值
     *
     * @param key 配置键
     * @return 配置值
     */
    Integer getInteger(String key);

    /**
     * 获取整数配置值，带默认值
     *
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    Integer getInteger(String key, Integer defaultValue);

    /**
     * 获取 BigDecimal 配置值
     *
     * @param key 配置键
     * @return 配置值
     */
    BigDecimal getDecimal(String key);

    /**
     * 获取 BigDecimal 配置值，带默认值
     *
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    BigDecimal getDecimal(String key, BigDecimal defaultValue);

    /**
     * 获取布尔配置值
     *
     * @param key 配置键
     * @return 配置值
     */
    Boolean getBoolean(String key);

    /**
     * 更新配置值
     *
     * @param key 配置键
     * @param value 配置值
     */
    void updateConfig(String key, String value);

}
