package com.ljw.logalarm.core.service;

import com.ljw.logalarm.core.context.LogAlarmContext;
import com.ljw.logalarm.core.dto.AlarmMessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author lujianwen9@gmail.com
 * @since 2024-08-07 15:32
 */

public class Sender implements DisposableBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(Sender.class);

    private String alarmMode;
    private String webhook;
    private volatile boolean running = true;

    private static final Map<String, AlarmService> STRATEGIES = new HashMap<>();

    public Sender(String alarmMode,String webhook) {
        this.alarmMode = alarmMode;
        this.webhook = webhook;
    }

    private final ExecutorService executor =  Executors.newSingleThreadExecutor();

    public void init() {
        STRATEGIES.put("workWechat", new WorkWechatAlarmService(webhook));
        STRATEGIES.put("dingding",new DingdingAlarmService(webhook));
        executor.execute(new Runnable() {
            @Override
            public void run() {
                while (running && !Thread.currentThread().isInterrupted()){
                    try {
                        AlarmMessageDTO dto = LogAlarmContext.logBlockingQueue.take();
                        AlarmService alarmService = STRATEGIES.get(alarmMode);
                        if (alarmService == null) {
                            LOGGER.warn("Unsupported log alarm mode [{}], discard alarm message.", alarmMode);
                            continue;
                        }
                        alarmService.doAlarm(dto.getMessage());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        LOGGER.warn("Failed to send log alarm message.", e);
                    }
                }
            }
        });

    }

    @Override
    public void destroy() {
        running = false;
        executor.shutdownNow();
    }
}
