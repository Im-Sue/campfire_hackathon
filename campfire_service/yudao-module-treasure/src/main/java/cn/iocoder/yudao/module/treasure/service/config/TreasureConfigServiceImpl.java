package cn.iocoder.yudao.module.treasure.service.config;

import cn.iocoder.yudao.module.treasure.config.TreasureProperties;
import cn.iocoder.yudao.module.treasure.dal.dataobject.TreasureConfigDO;
import cn.iocoder.yudao.module.treasure.dal.mysql.TreasureConfigMapper;
import cn.iocoder.yudao.module.treasure.enums.TreasureConfigConstants;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 夺宝模块配置 Service 实现
 *
 * @author Sue
 */
@Slf4j
@Service
public class TreasureConfigServiceImpl implements TreasureConfigService {

    @Resource
    private TreasureConfigMapper treasureConfigMapper;

    @Resource
    private TreasureProperties treasureProperties;

    /**
     * 本地缓存，60 秒过期
     */
    private final Cache<String, String> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(60, TimeUnit.SECONDS)
            .build();

    /**
     * 默认配置定义：key -> {value, description}
     */
    private Map<String, String[]> getDefaultConfigs() {
        Map<String, String[]> defaults = new LinkedHashMap<>();
        defaults.put(TreasureConfigConstants.CONTRACT_ADDRESS,
                new String[]{treasureProperties.getContract().getAddress(), "TreasurePool 合约地址"});
        defaults.put(TreasureConfigConstants.SWITCHBOARD_VRF_ADDRESS,
                new String[]{treasureProperties.getContract().getSwitchboardVrfAddress(), "Switchboard VRF 合约地址"});
        defaults.put(TreasureConfigConstants.VRF_FEE,
                new String[]{treasureProperties.getContract().getVrfFee(), "VRF 请求费用 (wei)"});
        defaults.put(TreasureConfigConstants.CHAIN_ID,
                new String[]{String.valueOf(treasureProperties.getBlockchain().getChainId()), "链 ID"});
        defaults.put(TreasureConfigConstants.GAS_LIMIT,
                new String[]{String.valueOf(treasureProperties.getBlockchain().getGasLimit()), "Gas 限制"});
        defaults.put(TreasureConfigConstants.EVENT_SYNC_ENABLED,
                new String[]{String.valueOf(treasureProperties.getEventSync().getEnabled()), "是否启用事件同步"});
        defaults.put(TreasureConfigConstants.EVENT_SYNC_INTERVAL,
                new String[]{String.valueOf(treasureProperties.getEventSync().getInterval()), "事件同步间隔 (毫秒)"});
        defaults.put(TreasureConfigConstants.EVENT_SYNC_BLOCK_BATCH_SIZE,
                new String[]{String.valueOf(treasureProperties.getEventSync().getBlockBatchSize()), "每批扫描区块数"});
        return defaults;
    }

    /**
     * 启动时初始化默认配置（只插入不存在的 key）
     */
    @PostConstruct
    public void initDefaultConfigs() {
        log.info("[TreasureConfig] 初始化默认配置...");
        Map<String, String[]> defaults = getDefaultConfigs();
        int inserted = 0;
        for (Map.Entry<String, String[]> entry : defaults.entrySet()) {
            String key = entry.getKey();
            String[] valuePair = entry.getValue();
            TreasureConfigDO existing = treasureConfigMapper.selectByConfigKey(key);
            if (existing == null) {
                TreasureConfigDO config = TreasureConfigDO.builder()
                        .configKey(key)
                        .configValue(valuePair[0])
                        .description(valuePair[1])
                        .build();
                treasureConfigMapper.insert(config);
                inserted++;
            }
        }
        log.info("[TreasureConfig] 初始化完成，新增 {} 条配置", inserted);
    }

    // ========== 基础读取方法 ==========

    @Override
    public String getString(String key) {
        String cached = cache.getIfPresent(key);
        if (cached != null) {
            return cached;
        }
        TreasureConfigDO config = treasureConfigMapper.selectByConfigKey(key);
        if (config != null) {
            cache.put(key, config.getConfigValue());
            return config.getConfigValue();
        }
        log.warn("[TreasureConfig] 未找到配置: {}", key);
        return null;
    }

    @Override
    public Long getLong(String key) {
        String value = getString(key);
        return value != null ? Long.parseLong(value) : null;
    }

    @Override
    public Boolean getBoolean(String key) {
        String value = getString(key);
        return value != null ? Boolean.parseBoolean(value) : null;
    }

    @Override
    public Integer getInteger(String key) {
        String value = getString(key);
        return value != null ? Integer.parseInt(value) : null;
    }

    // ========== 写入方法 ==========

    @Override
    public void updateConfig(String key, String value) {
        TreasureConfigDO config = treasureConfigMapper.selectByConfigKey(key);
        if (config == null) {
            throw new IllegalArgumentException("配置项不存在: " + key);
        }
        config.setConfigValue(value);
        treasureConfigMapper.updateById(config);
        cache.put(key, value);
        log.info("[TreasureConfig] 更新配置: {} = {}", key, value);
    }

    @Override
    public List<TreasureConfigDO> listConfigs() {
        return treasureConfigMapper.selectAllConfigs();
    }

    @Override
    public void refreshCache() {
        cache.invalidateAll();
        log.info("[TreasureConfig] 缓存已刷新");
    }

    // ========== 便捷方法 ==========

    @Override
    public String getContractAddress() {
        return getString(TreasureConfigConstants.CONTRACT_ADDRESS);
    }

    @Override
    public Integer getChainId() {
        return getInteger(TreasureConfigConstants.CHAIN_ID);
    }

    @Override
    public String getSwitchboardVrfAddress() {
        return getString(TreasureConfigConstants.SWITCHBOARD_VRF_ADDRESS);
    }

    @Override
    public String getVrfFee() {
        return getString(TreasureConfigConstants.VRF_FEE);
    }

    @Override
    public Long getGasLimit() {
        return getLong(TreasureConfigConstants.GAS_LIMIT);
    }

    @Override
    public Boolean isEventSyncEnabled() {
        return getBoolean(TreasureConfigConstants.EVENT_SYNC_ENABLED);
    }

    @Override
    public Long getEventSyncInterval() {
        return getLong(TreasureConfigConstants.EVENT_SYNC_INTERVAL);
    }

    @Override
    public Integer getBlockBatchSize() {
        return getInteger(TreasureConfigConstants.EVENT_SYNC_BLOCK_BATCH_SIZE);
    }
}
