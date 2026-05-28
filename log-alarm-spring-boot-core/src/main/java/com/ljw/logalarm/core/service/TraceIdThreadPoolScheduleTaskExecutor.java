package com.ljw.logalarm.core.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.TaskUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import static com.ljw.logalarm.core.filter.LogParamsFilter.APP_NAME;
import static com.ljw.logalarm.core.filter.TraceIdFilter.TRACE_ID;
import static com.ljw.logalarm.core.filter.TraceIdFilter.genTraceId;

/**
 * @author lujianwen9@gmail.com
 * @since 2024-08-15 17:13
 */
@Slf4j
public class TraceIdThreadPoolScheduleTaskExecutor extends ThreadPoolTaskScheduler {
    private final String applicationName;

    public TraceIdThreadPoolScheduleTaskExecutor() {
        this(null);
    }

    public TraceIdThreadPoolScheduleTaskExecutor(String applicationName) {
        this.applicationName = applicationName;
    }

    private Runnable wrapTaskWithMDC(Runnable task) {
        Map<String, String> context = prepareContext(MDC.getCopyOfContextMap());
        return TaskUtils.decorateTaskWithErrorHandler(() -> runWithMdc(context, task), null, true);
    }

    private Map<String, String> prepareContext(Map<String, String> context) {
        Map<String, String> resolved = context == null ? new HashMap<>() : new HashMap<>(context);
        if (!StringUtils.hasText(resolved.get(TRACE_ID))) {
            resolved.put(TRACE_ID, genTraceId());
        }
        if (StringUtils.hasText(applicationName)) {
            resolved.put(APP_NAME, applicationName);
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
            if (previous == null || previous.isEmpty()) {
                MDC.clear();
            } else {
                MDC.setContextMap(previous);
            }
        }
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable task, Trigger trigger) {
        return super.schedule(wrapTaskWithMDC(task), trigger);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Duration period) {
        return super.scheduleAtFixedRate(wrapTaskWithMDC(task), period);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable task, Instant startTime) {
        return super.schedule(wrapTaskWithMDC(task), startTime);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Instant startTime, Duration period) {
        return super.scheduleAtFixedRate(wrapTaskWithMDC(task), startTime, period);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Duration delay) {
        return super.scheduleWithFixedDelay(wrapTaskWithMDC(task), delay);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Instant startTime, Duration delay) {
        return super.scheduleWithFixedDelay(wrapTaskWithMDC(task), startTime, delay);
    }

}
