package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.autoCredit.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CreditChannelEnum {

    JDJR(23, "jdJrCreditPreApproveHandle", "京东金融");

    private final Integer code;
    private final String beanName;
    private final String desc;

    /**
     * 根据code获取枚举
     */
    public static CreditChannelEnum getByCode(Integer code) {
        for (CreditChannelEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}