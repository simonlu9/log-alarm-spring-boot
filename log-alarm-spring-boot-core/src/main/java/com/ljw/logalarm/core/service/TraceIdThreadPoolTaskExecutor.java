package com.ljw.logalarm.core.service;

import org.slf4j.MDC;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * @author lujianwen@wsyxmall.com
 * @since 2024-08-15 17:13
 */
public class TraceIdThreadPoolTaskExecutor extends ThreadPoolTaskExecutor {
    /**
     * 所有线程都会委托给这个execute方法，在这个方法中我们把父线程的MDC内容赋值给子线程
     * https://logback.qos.ch/manual/mdc.html#managedThreads
     *
     * @param runnable runnable
     */
    @Override
    public void execute(Runnable runnable) {
        // 获取父线程MDC中的内容，必须在run方法之前，否则等异步线程执行的时候有可能MDC里面的值已经被清空了，这个时候就会返回null
        Map<String, String> context = MDC.getCopyOfContextMap();
        super.execute(() -> {
            // 将父线程的MDC内容传给子线程
            if (context != null) {
                MDC.setContextMap(context);
            }
            try {
                // 执行异步操作
                runnable.run();
            } finally {
                // 清空MDC内容
                MDC.clear();
            }
        });
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        // 获取父线程MDC中的内容，必须在run方法之前，否则等异步线程执行的时候有可能MDC里面的值已经被清空了，这个时候就会返回null
        Map<String, String> context = MDC.getCopyOfContextMap();
        return super.submit(() -> {
            // 将父线程的MDC内容传给子线程
            if (context != null) {
                MDC.setContextMap(context);
            }
            try {
                // 执行异步操作
                return task.call();
            } finally {
                // 清空MDC内容
                MDC.clear();
            }
        });
    }
}
