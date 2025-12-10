package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.universalInputPlatform.enums;

import lombok.Getter;

/**
 * 还款状态枚举
 */
@Getter
public enum RepayStatusEnum {
    SUCCESS('U',"还款成功"),
    FAILED('O',"还款失败"),
    REPAYING('P',"处理中");

    private final Character code;
    private final String desc;

    RepayStatusEnum(Character code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
