package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.universalInputPlatform.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * 单日授信
 */
@Setter
@Getter
@ToString(includeFieldNames=false)
@Builder
public class XAIntradayCredit implements IntradayCredit {
    /**
     * 合作伙伴ID
     */
    private String partnerId;
    /**
     * 对账日期
     */
    private String batchDate;
    /**
     * 产品编号
     */
    private String productNo;
    /**
     * 云闪付用户号
     */
    private String custId;
    /**
     * 授信额度
     */
    private BigDecimal creditLimit;
    /**
     * 执行利率
     */
    private BigDecimal intRate;
    /**
     * 授信日期
     */
    private String creditDate;
    /**
     * 额度状态
     */
    private String limitStatus;
}
