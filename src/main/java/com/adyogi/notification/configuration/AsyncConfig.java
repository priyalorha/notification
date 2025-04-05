package com.adyogi.notification.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

import static com.adyogi.notification.utils.constants.ConfigConstants.*;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = ALL_CLIENT_EMAIL)
    public Executor sendEmailAllClientTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.initialize();
        return executor;
    }

    @Bean(name = SEND_EMAIL_SINGLE_CLIENT)
    public Executor sendEmailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.initialize();
        return executor;
    }

    @Bean(name = BULK_SAVE_METRICS_TASK)
    public Executor bulkSaveMetricsTaskExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.initialize();
        return executor;
    }


    @Bean(name = PROCESS_BULK_INCIDENT_TASK)
    public Executor processIncidentsForClientTaskExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.initialize();
        return executor;
    }


}

