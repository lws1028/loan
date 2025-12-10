package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.bj58.dto;

import lombok.Data;

/**
 * 线索回调 返回
 */
@Data
public class CluesInformationNotifyResponse {

    private Integer code;

    private String msg;

    private Object date;

    public Boolean success() {
        return 0 == code;
    }
}
