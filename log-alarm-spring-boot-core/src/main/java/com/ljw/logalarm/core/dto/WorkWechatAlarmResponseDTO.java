package com.ljw.logalarm.core.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

/**
 * @author lujianwen@wsyxmall.com
 * @since 2023-12-20 15:42
 */
@Setter
@Getter
public class WorkWechatAlarmResponseDTO {
    @JSONField(name = "errcode")
    private Integer errCode;

    @JSONField(name = "errmsg")
    private String errMsg;
}
