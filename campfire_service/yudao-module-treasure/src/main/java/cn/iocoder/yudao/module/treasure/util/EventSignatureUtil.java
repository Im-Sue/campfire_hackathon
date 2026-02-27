package cn.iocoder.yudao.module.treasure.util;

import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;

/**
 * 事件签名计算工具类
 *
 * @author Sue
 */
public class EventSignatureUtil {

    /**
     * 计算事件签名
     *
     * @param eventSignature 事件签名字符串，例如: "PoolCreated(uint256,uint256,uint256,uint256,uint256)"
     * @return 事件签名的 Keccak-256 哈希值（十六进制字符串）
     */
    public static String calculateEventSignature(String eventSignature) {
        byte[] hash = Hash.sha3(eventSignature.getBytes());
        return Numeric.toHexString(hash);
    }

    public static void main(String[] args) {
        // 计算各个事件的签名
        System.out.println("PoolCreated: " + calculateEventSignature("PoolCreated(uint256,uint256,uint256,uint256,uint256)"));
        System.out.println("TicketPurchased: " + calculateEventSignature("TicketPurchased(uint256,address,uint256)"));
        System.out.println("DrawStarted: " + calculateEventSignature("DrawStarted(uint256,bytes32)"));
        System.out.println("DrawCompleted: " + calculateEventSignature("DrawCompleted(uint256,address[],uint256)"));
        System.out.println("PrizeClaimed: " + calculateEventSignature("PrizeClaimed(uint256,address,uint256)"));
    }
}
