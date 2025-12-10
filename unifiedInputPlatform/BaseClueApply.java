package com.zlhj.unifiedInputPlatform.unifiedInputPlatform;

import lombok.Data;

@Data
public class BaseClueApply {

    /**
     * 姓名
     */
    private String name;

    /**
     * 证件号
     */
    private String idNum;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 线索编号（由线索方提供）
     */
    private String applyNo;
}
