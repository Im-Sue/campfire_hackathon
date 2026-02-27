package cn.iocoder.yudao.module.treasure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Treasure 模块配置属性
 *
 * @author Sue
 */
@Data
@Component
@ConfigurationProperties(prefix = "treasure")
public class TreasureProperties {

    /**
     * 区块链配置
     */
    private BlockchainConfig blockchain = new BlockchainConfig();

    /**
     * 合约配置
     */
    private ContractConfig contract = new ContractConfig();

    /**
     * 事件同步配置
     */
    private EventSyncConfig eventSync = new EventSyncConfig();

    @Data
    public static class BlockchainConfig {
        /**
         * RPC 节点地址
         */
        private String rpcUrl = "https://testnet-rpc.monad.xyz";

        /**
         * 链 ID
         */
        private Integer chainId = 10143;

        /**
         * 私钥（用于管理员操作）
         */
        private String privateKey;

        /**
         * Gas 限制
         */
        private Long gasLimit = 500000L;
    }

    @Data
    public static class ContractConfig {
        /**
         * TreasurePool 合约地址
         */
        private String address = "0x873F006fe119246F27cE68c3b9A7ec59bB2390F9";

        /**
         * Switchboard VRF 合约地址
         */
        private String switchboardVrfAddress = "0x36825bf3Fbdf5a29E2d5148bfe7Dcf7B5639e320";

        /**
         * VRF 请求费用（wei）
         */
        private String vrfFee = "100000000000000000"; // 0.1 MON
    }

    @Data
    public static class EventSyncConfig {
        /**
         * 是否启用事件同步
         */
        private Boolean enabled = true;

        /**
         * 同步间隔（毫秒）
         */
        private Long interval = 10000L; // 10秒

        /**
         * 每次同步的区块数量
         */
        private Integer blockBatchSize = 100;

        /**
         * 起始区块高度（0表示从最新区块开始）
         */
        private Long startBlock = 0L;

        /**
         * 最大重试次数
         */
        private Integer maxRetryCount = 3;

        /**
         * 重试间隔（毫秒）
         */
        private Long retryInterval = 5000L;
    }
}
