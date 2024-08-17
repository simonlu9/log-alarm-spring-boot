package com.ljw.logalarm.test;

import com.ljw.logalarm.core.filter.TraceIdFilter;
import com.ljw.logalarm.core.service.TraceIdThreadPoolTaskExecutor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;

@SpringBootApplication
@EnableAsync
public class TestApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }

    @Bean
    public Executor customTaskExecutor() {
        TraceIdThreadPoolTaskExecutor executor =  new TraceIdThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("trace-id-task-executor-");
        executor.setCorePoolSize(4);
        return executor;
    }
}