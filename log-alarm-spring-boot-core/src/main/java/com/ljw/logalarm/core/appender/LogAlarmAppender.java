package com.ljw.logalarm.core.appender;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.ljw.logalarm.core.context.LogAlarmContext;
import com.ljw.logalarm.core.dto.AlarmMessageDTO;
import net.logstash.logback.stacktrace.ShortenedThrowableConverter;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ljw.logalarm.core.filter.LogParamsFilter.*;
import static com.ljw.logalarm.core.filter.TraceIdFilter.TRACE_ID;

/**
 * @author lujianwen9@gmail.com
 * @since 2024-08-07 10:45
 */
public class LogAlarmAppender extends AppenderBase<LoggingEvent> {
    private static final Pattern EXCEPTION_PATTERN= Pattern.compile("(.+?): ");
    @Override
    protected void append(LoggingEvent eventObject) {
        try {
            Level level = eventObject.getLevel();
            if (Level.ERROR!=level) {
                //只处理error级别的报错
                return;
            }
            //构造消息通知内容AlarmService
            String message = initMessage(eventObject);
            //添加消息到队列，队列满时丢弃，避免告警系统反向影响业务日志线程
            LogAlarmContext.logBlockingQueue.offer(AlarmMessageDTO.builder().message(message).build());

        } catch (Exception e) {
            addError("Failed to append log alarm event.", e);
        }
    }
    private String initMessage(LoggingEvent eventObject) {
        //获取异常堆栈信息
        IThrowableProxy proxy = eventObject.getThrowableProxy();
        String track = "";
        String trackMessage = "";
        String stackTrace = "";
        if (proxy!=null){
            ShortenedThrowableConverter throwableConverter = new ShortenedThrowableConverter();
            throwableConverter.setMaxDepthPerThrowable(5);
            throwableConverter.setExcludes(Arrays.asList("sun\\.reflect\\..*\\.invoke.*","net\\.sf\\.cglib\\.proxy\\.MethodProxy\\.invoke"));
            throwableConverter.start();
            stackTrace =  throwableConverter.convert(eventObject);
            throwableConverter.stop();
            Matcher matcher = EXCEPTION_PATTERN.matcher(stackTrace);
            if(matcher.find()){
                track = matcher.group(1);
            }
            trackMessage = stackTrace;
        }
        String template = "链路追踪: %s\n应用名: %s\n线程名称: %s\n用户编号: %s\n请求信息: %s\n请求参数: %s\n请求body: %s\n异常来源: %s\n日志内容: %s\n异常时间: %s\n异常描述: %s\n详细信息:\n%s";
        LocalDateTime date = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDate = date.format(formatter);
        String requestInfo = !StringUtils.isEmpty(MDC.get(REQUEST_METHOD))?MDC.get(REQUEST_METHOD)+" "+MDC.get(REQUEST_URL):"";
        return String.format(template,
                MDC.get(TRACE_ID),
                MDC.get(APP_NAME),
                eventObject.getThreadName(),
                MDC.get(USER_ID),
                requestInfo,
                MDC.get(REQUEST_PARAMS),
                MDC.get(REQUEST_BODY),
                //LoggerName表示生成该日志记录器的名字，即打印日志的类的完整类路径
                eventObject.getLoggerName(),
                //日志内容
                eventObject.getFormattedMessage(),
                //异常时间
                formattedDate,
                //异常描述(异常类型)
                track,
                //异常堆栈信息
                trackMessage);
    }
}
