package com.ljw.logalarm.core.appender;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.ljw.logalarm.core.context.LogAlarmContext;
import com.ljw.logalarm.core.dto.AlarmMessageDTO;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.stacktrace.ShortenedThrowableConverter;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ljw.logalarm.core.filter.LogParamsFilter.REQUEST_BODY;
import static com.ljw.logalarm.core.filter.LogParamsFilter.REQUEST_PARAMS;
import static com.ljw.logalarm.core.filter.TraceIdFilter.TRACE_ID;

/**
 * @author lujianwen9@gmail.com
 * @since 2024-08-07 10:45
 */
@Slf4j
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
            //添加消息到队列
            LogAlarmContext.logBlockingQueue.add(AlarmMessageDTO.builder().message(message).build());

        } catch (Exception e) {
           log.error(e.getMessage(),e);
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
        String template = "TraceId: %s\n请求参数: %s\n请求BODY: %s\n异常来源: %s\n日志内容: %s\n异常时间: %s\n异常描述: %s\n详细信息:\n%s";
        LocalDateTime date = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDate = date.format(formatter);
        return String.format(template,
                MDC.get(TRACE_ID),
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
