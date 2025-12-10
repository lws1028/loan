package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.universalInputPlatform.enums;

import lombok.Getter;

/**
 * 还款类型枚举
 */
@Getter
public enum RepayTypeEnum {
    ON_TIME('T',"按期还款"),
    EARLY_REPAYMENT('A',"提前结清"),
    PARTIAL_REPAYMENT('S',"部分还款"),
    OVER_DUE('O',"逾期还款"),
    AUTO_DEDUCTION('D',"自动扣款"),
    OTHER('C',"其他");

    private final Character code;
    private final String desc;

    RepayTypeEnum(Character code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
