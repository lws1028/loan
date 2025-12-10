package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.universalInputPlatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 线索统一进件数据分析请求体
 */
@Data
@AllArgsConstructor
public class UniversalInputPlatformDataAnalysisReq {
    /**
     * 资方来源
     */
    private Integer selectCode;
}
