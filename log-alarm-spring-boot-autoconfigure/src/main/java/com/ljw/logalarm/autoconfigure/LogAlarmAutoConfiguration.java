package com.ljw.logalarm.autoconfigure;

import com.ljw.logalarm.core.filter.AlarmFilter;
import com.ljw.logalarm.core.filter.LogParamsFilter;
import com.ljw.logalarm.core.filter.TraceIdFilter;
import com.ljw.logalarm.core.service.Sender;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author lujianwen9@gmail.com
 * @since 2024-08-15 14:53
 */
@Configuration
@EnableConfigurationProperties(LogAlarmProperties.class)
public class LogAlarmAutoConfiguration implements WebMvcConfigurer {
    private final LogAlarmProperties logAlarmProperties;

    public LogAlarmAutoConfiguration(LogAlarmProperties logAlarmProperties) {
        this.logAlarmProperties = logAlarmProperties;
        if(logAlarmProperties.getExclude()!=null){
            AlarmFilter.exclusionThrowableSet.addAll(logAlarmProperties.getExclude().getThrowable());
            AlarmFilter.exclusionString.addAll(logAlarmProperties.getExclude().getKeyword());
        }
    }


    @Bean(initMethod = "init")
    @ConditionalOnProperty(name = {"log-alarm.mode","log-alarm.webhook"}, matchIfMissing = false)
    public Sender sender(){
        return new Sender(logAlarmProperties.getMode(),logAlarmProperties.getWebhook());
    }
    @ConditionalOnProperty(name = "log-alarm.enableTraceId", havingValue = "true")
    @Bean
    public FilterRegistrationBean<TraceIdFilter> traceIdFilter() {
        FilterRegistrationBean<TraceIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TraceIdFilter());
        registration.addUrlPatterns("/*");
        registration.setName("traceIdFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    @ConditionalOnProperty(name = "log-alarm.enableTraceId", havingValue = "true")
    @Bean
    public FilterRegistrationBean<LogParamsFilter> logParamsFilter() {
        FilterRegistrationBean<LogParamsFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new LogParamsFilter());
        registration.addUrlPatterns("/*");
        registration.setName("logParamsFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
