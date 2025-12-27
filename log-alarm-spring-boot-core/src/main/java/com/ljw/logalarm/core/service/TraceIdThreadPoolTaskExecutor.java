package com.ljw.logalarm.core.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StringUtils;

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
        if(context==null||StringUtils.isEmpty(context.get(TRACE_ID))){
            String traceId = genTraceId();
            MDC.put(TRACE_ID, traceId);
        }else{
            MDC.setContextMap(context);
        }
        super.execute(() -> {
            // 将父线程的MDC内容传给子线程
            try {
                // 执行异步操作
                runnable.run();
            }catch (Exception e){
                log.error(e.getMessage(),e);
            }finally {
                // 清空MDC内容
                MDC.clear();
            }
        });
    }



    @Override
    public Future<?> submit(Runnable task) {
        Runnable wrappedTask = () -> {
            // 执行前的逻辑
            Map<String, String> context = MDC.getCopyOfContextMap();
            if(context==null||StringUtils.isEmpty(context.get(TRACE_ID))){
                String traceId = genTraceId();
                MDC.put(TRACE_ID, traceId);
            }else{
                MDC.setContextMap(context);
            }
            try {
                task.run();
            } finally {
                // 执行后的逻辑
                MDC.clear();
            }
        };
        return super.submit(wrappedTask);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return super.submit(wrap(task));
    }
    private <T> Callable<T> wrap(Callable<T> task) {
        String parentTraceId = MDC.get(TRACE_ID);

        return () -> {
            boolean needClear = false;
            try {
                if (parentTraceId == null) {
                    MDC.put(TRACE_ID, genTraceId());
                    needClear = true;
                } else {
                    MDC.put(TRACE_ID, parentTraceId);
                }
                return task.call();
            } finally {
                if (needClear) {
                    MDC.remove(TRACE_ID);
                }
            }
        };
    }

}
