package com.ljw.logalarm.core.service;

import com.alibaba.fastjson2.JSONObject;
import com.ljw.logalarm.core.dto.WorkWechatAlarmResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

/**
 * @author lujianwen9@gmail.com
 * @since 2023-12-20 15:12
 */
@Slf4j
public class DingdingAlarmService implements AlarmService {
    private static final Integer CONNECTIONTIMEOUT = 10000;
    private static final Integer READTIMEOUT = 10000;
    private  String webhook ;
    private final RestTemplate restTemplate;
    private final static String BODY = "{\n" +
            "    \t\"msgtype\": \"text\",\n" +
            "    \t\"text\": {\n" +
            "        \t\"content\": \"%s\"\n" +
            "    \t}\n" +
            "   }";



    public DingdingAlarmService(String webhook, RestTemplate restTemplate) {
        this.webhook = webhook;
        this.restTemplate = restTemplate;
    }

    public DingdingAlarmService(String webhook) {
        this.webhook = webhook;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECTIONTIMEOUT);
        factory.setReadTimeout(READTIMEOUT);
        restTemplate = new RestTemplate(factory);
        restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
    }


    @Override
    public void doAlarm(String msg) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject jsonObject = JSONObject.parseObject(BODY);
        jsonObject.getJSONObject("text").put("content",msg);
        HttpEntity<String> formEntity = new HttpEntity<String>(jsonObject.toString(), headers);
        String result = restTemplate.postForObject(webhook, formEntity, String.class);
        log.debug("[WorkWechatAlarmService] response info [{}]", result);
        WorkWechatAlarmResponseDTO wechatAlarmResponseDTO = JSONObject.parseObject(result,WorkWechatAlarmResponseDTO.class);
        Integer errCode = wechatAlarmResponseDTO.getErrCode();
        if (errCode != 0 && errCode != 45009) {
            log.warn("alarm call wechat roobat error [{}]", result);
        }
    }



}
