package com.zlhj.unifiedInputPlatform.ant.exceptions;

import com.zlhj.common.exception.BizException;

/**
 * @author : wangwenhao
 * @since : 2025/9/3 11:49
 */
public class IllegalChannelPartnerException extends BizException {
    public IllegalChannelPartnerException(String enumName) {
        super("非法的渠道类型: " + enumName);
    }
}
