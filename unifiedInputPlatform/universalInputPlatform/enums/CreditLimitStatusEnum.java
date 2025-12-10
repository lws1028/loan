package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.universalInputPlatform.enums;

import lombok.Getter;

/**
 * 授信额度状态枚举
 */
@Getter
public enum CreditLimitStatusEnum {
    NORMAL('A',"正常"),
    LONG_TERM_FREEZE('B',"长期冻结，该状态为额度长期不可用"),
    UP_TO_DATE('C',"到期"),
    SHORT_TERM_FREEZE('D',"短期冻结，该状态为额度短期内不可用"),
    NOT_ACTIVATED('E',"未激活"),
    CREDIT_REFUSE('F',"授信拒绝");

    private final Character code;
    private final String desc;

    CreditLimitStatusEnum(Character code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
