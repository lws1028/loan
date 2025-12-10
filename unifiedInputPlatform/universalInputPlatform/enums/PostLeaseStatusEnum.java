package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.universalInputPlatform.enums;

import lombok.Getter;

/**
 * 放款状态枚举
 */
@Getter
public enum PostLeaseStatusEnum {
    SUCCESS('U',"放款成功"),
    FAILED('O',"放款失败"),
    OTHER('P',"其他");

    private final Character code;
    private final String desc;

    PostLeaseStatusEnum(Character code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
