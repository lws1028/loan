package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.universalInputPlatform.enums;

import lombok.Getter;

/**
 * 还款渠道
 */
@Getter
public enum RepayChannelEnum {
    ACTIVE_REPAY('A',"主动还款"),
    BATCH_AUTO_DEDUCTION('B',"批量自动扣款"),
    OTHER('C',"其他");

    private final Character code;
    private final String desc;

    RepayChannelEnum(Character code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
