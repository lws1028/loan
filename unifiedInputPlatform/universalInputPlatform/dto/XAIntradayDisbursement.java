package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.universalInputPlatform.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * 当日放款
 */
@Setter
@Getter
@ToString(includeFieldNames=false)
@Builder
public class XAIntradayDisbursement implements IntradayDisbursement {
    /**
     * 合作伙伴ID
     */
    private String partnerId;
    /**
     * 对账日期
     */
    private String batchDate;
    /**
     * 产品编码
     */
    private String productNo;
    /**
     * 云闪付用户号
     */
    private String custId;
    /**
     * 借据号
     */
    private String duebillNo;
    /**
     * 放款日期
     */
    private String loanDate;
    /**
     * 贷款总额
     */
    private BigDecimal loanAmt;
    /**
     * 执行利率
     */
    private BigDecimal intRate;
    /**
     * 还款日
     */
    private String repayDay;
    /**
     * 总期数
     */
    private String loanTerm;
    /**
     * 放款状态
     */
    private String putoutStatus;
}
