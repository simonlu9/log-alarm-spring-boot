package com.ljw.logalarm.autoconfigure;

import com.ljw.logalarm.core.filter.*;
import com.ljw.logalarm.core.service.Sender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author lujianwen9@gmail.com
 * @since 2024-08-15 14:53
 */
@Configuration
@EnableConfigurationProperties({LogAlarmProperties.class,TimeoutProperties.class})
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
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE+1);
        return registration;
    }

    //@ConditionalOnProperty(name = "log-alarm.enableTraceId", havingValue = "true")
    @Bean
    public FilterRegistrationBean<CacheRequestBodyFilter> cacheBodyFilter() {
        FilterRegistrationBean<CacheRequestBodyFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new CacheRequestBodyFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.setName("cacheBodyFilter");
       // registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
    @Bean
    public FilterRegistrationBean<LogParamsFilter> logParamFilter(@Value("${spring.application.name:}") String applicationName) {
        FilterRegistrationBean<LogParamsFilter> registration = new FilterRegistrationBean<>();
        String logAlarmAppName = StringUtils.hasText(logAlarmProperties.getName())
                ? logAlarmProperties.getName()
                : applicationName;
        registration.setFilter(new LogParamsFilter(logAlarmAppName));
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE+2);
        registration.setName("logParamsFilter");
        // registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
    @Bean
    //@ConditionalOnBean({TimeoutProperties.class})
    //@ConditionalOnProperty(name = "log-alarm.timeout")
    public FilterRegistrationBean<ExecutionTimeFilter> executionTimeFilter( TimeoutProperties timeoutProperties) {
        FilterRegistrationBean<ExecutionTimeFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ExecutionTimeFilter(timeoutProperties.getSettings()));
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE+2);
        registration.setName("executionTimeFilter");
        // registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
