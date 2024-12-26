package net.media.emma.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;


@Configuration
@EnableAsync
public class BrowserPoolConfig {

    @Value("${corePoolSizeForSmokeTest}")
    private int corePoolSize;

    @Value("${maxPoolSizeForSmokeTest}")
    private int maxPoolSize;

    @Value("${queueCapacity}")
    private int queueCapacity;


    @Bean(name = "browserPool")
    public Executor asyncExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("BrowserRemote-AsynchThread-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "taskExecutor")
    public Executor yamlTestSuitePool() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("YamlTestSuite-AsynchThread-");
        executor.initialize();
        return executor;
    }
    
    @Bean(name = "schedulerThreadPool")
    public Executor schedulerThreadPool() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("schedulerThreadPool");
        executor.initialize();
        return executor;
    }

}