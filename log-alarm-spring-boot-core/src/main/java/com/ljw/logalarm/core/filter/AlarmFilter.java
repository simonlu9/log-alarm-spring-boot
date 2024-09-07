package com.ljw.logalarm.core.filter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

import java.util.HashSet;
import java.util.Set;

/**
 * @author lujianwen9@gmail.com
 * @since 2024-08-08 16:59
 */
public class AlarmFilter extends Filter<ILoggingEvent> {
    public static final Set<String> exclusionThrowableSet = new HashSet<>();
    public static final  Set<String> exclusionString= new HashSet<>();
    @Override
    public FilterReply decide(ILoggingEvent event) {
        if(event.getLevel().toInt()== Level.ERROR.toInt()){
            String exceptionClassName = event.getThrowableProxy() != null ? event.getThrowableProxy().getClassName() : null;
            if(exclusionThrowableSet.contains(exceptionClassName)){
                return FilterReply.DENY;
            }
            String message = event.getMessage();
            if(exclusionString.contains(message)){
                return FilterReply.DENY;
            }
            return FilterReply.ACCEPT;
        }
        return FilterReply.DENY;
    }
}
