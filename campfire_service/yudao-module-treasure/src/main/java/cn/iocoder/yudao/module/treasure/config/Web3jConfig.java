package cn.iocoder.yudao.module.treasure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.math.BigInteger;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.treasure.enums.ErrorCodeConstants.CONTRACT_NOT_CONFIGURED;

/**
 * Web3j 配置类
 *
 * @author Sue
 */
@Slf4j
@Configuration
public class Web3jConfig {

    @Resource
    private TreasureProperties treasureProperties;

    /**
     * 创建 Web3j 实例
     */
    @Bean
    public Web3j web3j() {
        String rpcUrl = treasureProperties.getBlockchain().getRpcUrl();
        log.info("初始化 Web3j，RPC URL: {}", rpcUrl);
        return Web3j.build(new HttpService(rpcUrl));
    }

    /**
     * 创建 Credentials（管理员私钥）
     */
    @Bean
    public Credentials credentials() {
        String privateKey = treasureProperties.getBlockchain().getPrivateKey();
        if (!StringUtils.hasText(privateKey)) {
            log.error("未配置管理员私钥，无法初始化合约签名账户");
            throw exception(CONTRACT_NOT_CONFIGURED);
        }
        Credentials credentials = Credentials.create(privateKey);
        log.info("加载管理员账户: {}", credentials.getAddress());
        return credentials;
    }

    /**
     * 创建动态 Gas Provider
     * 每次交易时从网络查询当前 gasPrice，避免 Monad 等链因 gas 价格波动导致 "Transaction fee too low"
     */
    @Bean
    public ContractGasProvider gasProvider(Web3j web3j) {
        // 动态 gasPrice + fallback 50 Gwei；gasLimit 从 YAML 读取启动默认值
        BigInteger fallbackGasPrice = BigInteger.valueOf(50_000_000_000L); // 50 Gwei
        BigInteger gasLimit = BigInteger.valueOf(treasureProperties.getBlockchain().getGasLimit());
        log.info("Gas 配置 - Fallback Price: {} wei, Limit: {}", fallbackGasPrice, gasLimit);

        return new ContractGasProvider() {
            @Override
            public BigInteger getGasPrice(String contractFunc) {
                return fetchGasPrice();
            }

            @Override
            public BigInteger getGasPrice() {
                return fetchGasPrice();
            }

            @Override
            public BigInteger getGasLimit(String contractFunc) {
                return gasLimit;
            }

            @Override
            public BigInteger getGasLimit() {
                return gasLimit;
            }

            private BigInteger fetchGasPrice() {
                try {
                    BigInteger networkGasPrice = web3j.ethGasPrice().send().getGasPrice();
                    // 在网络 gasPrice 基础上增加 10% 缓冲，提高成功率
                    BigInteger buffered = networkGasPrice.multiply(BigInteger.valueOf(110)).divide(BigInteger.valueOf(100));
                    log.debug("动态 gasPrice: network={}, buffered={}", networkGasPrice, buffered);
                    return buffered;
                } catch (IOException e) {
                    log.warn("获取网络 gasPrice 失败，使用 fallback: {}", fallbackGasPrice, e);
                    return fallbackGasPrice;
                }
            }
        };
    }
}

