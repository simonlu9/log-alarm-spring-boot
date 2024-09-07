package com.ljw.logalarm.autoconfigure;

import lombok.Data;

import java.util.Set;

/**
 * @author lujianwen9@gmail.com
 * @since 2024-08-16 11:36
 */
@Data
public class ErrorExcludeProperties {
    private Set<String> throwable;
    private Set<String> keyword;
}
