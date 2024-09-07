package com.ljw.logalarm.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author lujianwen9@gmail.com
 * @since 2024-08-15 14:51
 */
@ConfigurationProperties(prefix = LogAlarmProperties.LOG_ALARM_PREFIX)
@Data
public class LogAlarmProperties {
    public static final String LOG_ALARM_PREFIX = "log-alarm";
    private String webhook;
    private String mode;
    private ErrorExcludeProperties exclude;

}
