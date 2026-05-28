package com.ljw.logalarm.test.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AsyncService {
    @Async
    public void test() {
        log.error("async");
    }

    @Scheduled(fixedRate = 5000) // 每隔 5 秒执行一次
    public void runTask() {
        log.info("Fixed rate task executed at: " + System.currentTimeMillis());
        throw new RuntimeException("throw error3434");
    }
}
