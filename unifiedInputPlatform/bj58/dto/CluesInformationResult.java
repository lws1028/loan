package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.bj58.dto;

import lombok.ToString;

/**
 *
 */
@ToString
public class CluesInformationResult {

    public static final CluesInformationResult success = new CluesInformationResult(true, null);


    /**
     * 结果
     */
    private final Boolean result;

    /**
     * 错误内容
     */
    private final String msg;

    private CluesInformationResult(Boolean result, String msg) {
        this.result = result;
        this.msg = msg;
    }

    public boolean success() {
        return this.result;
    }

    public String getMsg() {
        return this.msg;
    }

    public static CluesInformationResult createCluesInformationError(String error) {
        return new CluesInformationResult(false, error);
    }
}
