package com.zlhj.unifiedInputPlatform.ant.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 渠道统一进件状态查询请求体
 * @author : wangwenhao
 * @since : 2025/9/2 17:28
 */
@Data
public class UnifiedInputPlatformBillReq {
    /**
     * 申请编号
     */
    @NotBlank(message = "申请编号不能为空")
    private String applyNo;
    /**
     * 状态
     */
    private String status;
    private String source;
}
