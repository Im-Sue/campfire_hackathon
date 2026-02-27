package cn.iocoder.yudao.module.market.service.config;

import cn.iocoder.yudao.module.market.dal.dataobject.config.PmConfigDO;
import cn.iocoder.yudao.module.market.dal.mysql.config.PmConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 预测市场配置 Service 实现类
 */
@Service
@Validated
@Slf4j
public class PmConfigServiceImpl implements PmConfigService {

    @Resource
    private PmConfigMapper pmConfigMapper;

    /**
     * 配置缓存
     */
    private final Map<String, String> configCache = new ConcurrentHashMap<>();

    @Override
    public String getString(String key) {
        return getString(key, null);
    }

    @Override
    public String getString(String key, String defaultValue) {
        // 先从缓存获取
        String cachedValue = configCache.get(key);
        if (cachedValue != null) {
            return cachedValue;
        }

        // 从数据库获取
        PmConfigDO config = pmConfigMapper.selectByKey(key);
        if (config == null) {
            return defaultValue;
        }

        // 缓存并返回
        configCache.put(key, config.getConfigValue());
        return config.getConfigValue();
    }

    @Override
    public Integer getInteger(String key) {
        return getInteger(key, null);
    }

    @Override
    public Integer getInteger(String key, Integer defaultValue) {
        String value = getString(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("[getInteger][配置 {} 的值 {} 无法转换为整数]", key, value);
            return defaultValue;
        }
    }

    @Override
    public BigDecimal getDecimal(String key) {
        return getDecimal(key, null);
    }

    @Override
    public BigDecimal getDecimal(String key, BigDecimal defaultValue) {
        String value = getString(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            log.warn("[getDecimal][配置 {} 的值 {} 无法转换为 BigDecimal]", key, value);
            return defaultValue;
        }
    }

    @Override
    public Boolean getBoolean(String key) {
        String value = getString(key);
        if (value == null) {
            return null;
        }
        return Boolean.parseBoolean(value);
    }

    @Override
    public void updateConfig(String key, String value) {
        PmConfigDO config = pmConfigMapper.selectByKey(key);
        if (config != null) {
            config.setConfigValue(value);
            pmConfigMapper.updateById(config);
            // 更新缓存
            configCache.put(key, value);
        }
    }

    /**
     * 清除缓存（用于配置更新时）
     */
    public void clearCache() {
        configCache.clear();
    }

    /**
     * 清除指定配置的缓存
     */
    public void clearCache(String key) {
        configCache.remove(key);
    }

}
