package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.ant.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AntCreditSwitchConfig {
    /**
     * 是否开启新流程
     */
    private boolean enabled;

    /**
     * 走新流程的比例（0~100）
     */
    private int percentage;
}
