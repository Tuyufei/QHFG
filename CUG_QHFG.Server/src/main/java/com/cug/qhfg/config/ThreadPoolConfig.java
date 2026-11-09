package com.cug.qhfg.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * @Description TODO
 * @Author fjh
 * @Date 2025/4/4
 */
@Configuration
@EnableAsync
public class ThreadPoolConfig {

    @Bean
    public Executor executor(){
        return new ThreadPoolTaskExecutor() {{
            setCorePoolSize(10); // 设置核心线程数
            setMaxPoolSize(20);  // 设置最大线程数
            setQueueCapacity(500); // 设置队列容量
            setThreadNamePrefix("query-thread-"); // 设置线程前缀
            initialize();
        }};

    }
}
