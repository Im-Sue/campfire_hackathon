package cn.iocoder.yudao.module.treasure.event.handler;

import org.web3j.protocol.core.methods.response.Log;

/**
 * 事件处理器接口
 *
 * @author Sue
 */
public interface EventHandler {

    /**
     * 处理事件
     *
     * @param log 事件日志
     */
    void handle(Log log) throws Exception;
}
