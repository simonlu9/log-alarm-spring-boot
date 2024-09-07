package com.ljw.logalarm.core.context;

import com.ljw.logalarm.core.dto.AlarmMessageDTO;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author lujianwen9@gmail.com
 * @since 2024-08-15 16:01
 */
public class LogAlarmContext {
    public final static BlockingQueue<AlarmMessageDTO> logBlockingQueue = new ArrayBlockingQueue<>(1000);
}
