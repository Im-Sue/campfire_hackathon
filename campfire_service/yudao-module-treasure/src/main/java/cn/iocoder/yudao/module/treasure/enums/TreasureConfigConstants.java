package cn.iocoder.yudao.module.treasure.enums;

/**
 * 夺宝模块配置键常量
 *
 * @author Sue
 */
public interface TreasureConfigConstants {

    // ========== 合约相关 ==========
    String CONTRACT_ADDRESS = "contract.address";
    String SWITCHBOARD_VRF_ADDRESS = "contract.switchboard_vrf_address";
    String MOCK_VRF_ADDRESS = "contract.mock_vrf_address";
    String VRF_FEE = "contract.vrf_fee";

    // ========== 区块链相关 ==========
    String CHAIN_ID = "blockchain.chain_id";
    String GAS_LIMIT = "blockchain.gas_limit";

    // ========== 事件同步相关 ==========
    String EVENT_SYNC_ENABLED = "event_sync.enabled";
    String EVENT_SYNC_INTERVAL = "event_sync.interval";
    String EVENT_SYNC_BLOCK_BATCH_SIZE = "event_sync.block_batch_size";
}
