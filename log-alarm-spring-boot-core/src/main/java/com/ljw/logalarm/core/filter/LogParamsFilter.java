package com.ljw.logalarm.core.filter;

import com.alibaba.fastjson2.JSONObject;
import com.ljw.logalarm.core.wrapper.RequestWrapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lujianwen@wsyxmall.com
 * @since 2024-09-05 11:50
 */
@Slf4j
public class LogParamsFilter extends OncePerRequestFilter {
    public static final String REQUEST_PARAMS = "requestParams";
    public static final String REQUEST_BODY = "requestBody";
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        RequestWrapper requestWrapper = new RequestWrapper((HttpServletRequest) request);
        MDC.put(REQUEST_BODY,requestWrapper.getBody());
        Map<String, String> paramMap = buildParametersMap(request);
        MDC.put(REQUEST_PARAMS, JSONObject.from(paramMap).toString());
        log.info("Request Body: " + requestWrapper.getBody());
        chain.doFilter(requestWrapper, response);

    }
    private Map<String, String> buildParametersMap(HttpServletRequest httpServletRequest) {
        Map<String, String> resultMap = new HashMap<>();
        Enumeration<String> parameterNames = httpServletRequest.getParameterNames();

        while (parameterNames.hasMoreElements()) {
            String key = parameterNames.nextElement();
            String value = httpServletRequest.getParameter(key);
            resultMap.put(key, value);
        }

        return resultMap;
    }

}
