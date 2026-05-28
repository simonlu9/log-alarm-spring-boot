package com.ljw.logalarm.test;

import com.ljw.logalarm.core.service.TraceIdThreadPoolScheduleTaskExecutor;
import com.ljw.logalarm.core.service.TraceIdThreadPoolTaskExecutor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.Executor;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class TestApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }

    @Bean
    @Primary
    public ThreadPoolTaskExecutor customTaskExecutor(@Value("${log-alarm.name:${spring.application.name:}}") String applicationName) {
        TraceIdThreadPoolTaskExecutor executor =  new TraceIdThreadPoolTaskExecutor(applicationName);
        executor.setThreadNamePrefix("trace-id-task-executor-");
        executor.setCorePoolSize(4);
        return executor;
    }
    @Bean
    public ThreadPoolTaskScheduler taskScheduler(@Value("${log-alarm.name:${spring.application.name:}}") String applicationName) {
        ThreadPoolTaskScheduler scheduler = new TraceIdThreadPoolScheduleTaskExecutor(applicationName);
        scheduler.setPoolSize(5); // 配置线程池大小
        scheduler.setThreadNamePrefix("Scheduler-");
        return scheduler;
    }


}
