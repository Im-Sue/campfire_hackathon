package cn.iocoder.yudao.module.ai.framework.agent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * AI Agent 线程池配置
 *
 * @author campfire
 */
@Configuration
public class AgentThreadPoolConfig {

    /**
     * 房间运行线程池
     * 用于房间主循环
     */
    @Bean("agentRoomExecutor")
    public ThreadPoolTaskExecutor agentRoomExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);       // 核心线程数，对应同时运行5个房间
        executor.setMaxPoolSize(15);       // 最大线程数，应对突发
        executor.setQueueCapacity(100);    // 队列容量
        executor.setThreadNamePrefix("agent-room-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    /**
     * LLM调用线程池
     * 用于并发调用LLM
     */
    @Bean("agentLlmExecutor")
    public ThreadPoolTaskExecutor agentLlmExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);      // LLM调用并发数
        executor.setMaxPoolSize(30);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("agent-llm-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        executor.initialize();
        return executor;
    }

}
