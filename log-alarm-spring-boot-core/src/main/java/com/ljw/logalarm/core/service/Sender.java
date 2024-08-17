package com.ljw.logalarm.core.service;

import com.ljw.logalarm.core.context.LogAlarmContext;
import com.ljw.logalarm.core.dto.AlarmMessageDTO;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author lujianwen@wsyxmall.com
 * @since 2024-08-07 15:32
 */

public class Sender {
    private String alarmMode;
    private String webhook;

    private static final Map<String, AlarmService> STRATEGIES = new HashMap<>();

    static {
        //STRATEGIES.put("dingding", new DingDingAlarmService());
        STRATEGIES.put("workWechat", new WorkWechatAlarmService());
    }

    public Sender(String alarmMode,String webhook) {
        this.alarmMode = alarmMode;
        this.webhook = webhook;
    }

    private final ExecutorService executor =  Executors.newSingleThreadExecutor();

    public void init() {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        AlarmMessageDTO dto = LogAlarmContext.logBlockingQueue.take();
                        STRATEGIES.get(alarmMode).doAlarm(dto.getMessage());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

    }
}
