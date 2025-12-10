package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.universalInputPlatform.enums;

import lombok.Getter;

/**
 * 借据状态枚举
 */
@Getter
public enum LoanStatusEnum {
    IN_USE('U',"使用中"),
    APPROVING('A',"审核中"),
    OVER_DUE('O',"已逾期"),
    REPAY('P',"已结清");

    private final Character code;
    private final String desc;

    LoanStatusEnum(Character code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
