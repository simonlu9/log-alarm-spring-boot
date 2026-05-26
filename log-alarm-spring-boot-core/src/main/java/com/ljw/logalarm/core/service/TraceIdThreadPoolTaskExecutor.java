package com.ljw.logalarm.core.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static com.ljw.logalarm.core.filter.TraceIdFilter.TRACE_ID;
import static com.ljw.logalarm.core.filter.TraceIdFilter.genTraceId;

/**
 * @author lujianwen9@gmail.com
 * @since 2024-08-15 17:13
 */
@Slf4j
public class TraceIdThreadPoolTaskExecutor extends ThreadPoolTaskExecutor {
    @Override
    public void execute(Runnable runnable) {
        Map<String, String> context = prepareContext(MDC.getCopyOfContextMap());
        super.execute(() -> runWithMdc(context, runnable));
    }



    @Override
    public Future<?> submit(Runnable task) {
        Map<String, String> context = prepareContext(MDC.getCopyOfContextMap());
        return super.submit(() -> runWithMdc(context, task));
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        Map<String, String> context = prepareContext(MDC.getCopyOfContextMap());
        return super.submit(wrap(context, task));
    }

    private Map<String, String> prepareContext(Map<String, String> context) {
        Map<String, String> resolved = context == null ? new HashMap<>() : new HashMap<>(context);
        if (!StringUtils.hasText(resolved.get(TRACE_ID))) {
            resolved.put(TRACE_ID, genTraceId());
        }
        return resolved;
    }

    private void runWithMdc(Map<String, String> context, Runnable task) {
        Map<String, String> previous = MDC.getCopyOfContextMap();
        try {
            MDC.setContextMap(context);
            task.run();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            restoreMdc(previous);
        }
    }

    private <T> Callable<T> wrap(Map<String, String> context, Callable<T> task) {
        return () -> {
            Map<String, String> previous = MDC.getCopyOfContextMap();
            try {
                MDC.setContextMap(context);
                return task.call();
            } finally {
                restoreMdc(previous);
            }
        };
    }

    private void restoreMdc(Map<String, String> previous) {
        if (previous == null || previous.isEmpty()) {
            MDC.clear();
        } else {
            MDC.setContextMap(previous);
        }
    }

}
