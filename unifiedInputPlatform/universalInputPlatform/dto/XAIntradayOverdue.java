package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.universalInputPlatform.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * 当日逾期BI
 */
@Setter
@Getter
@ToString(includeFieldNames=false)
@Builder
public class XAIntradayOverdue implements IntradayOverdue {
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
     * 借据号
     */
    private String duebillNo;
    /**
     * 放款日期
     */
    private String loanDate;
    /**
     * 逾期期次
     */
    private String overdueTerm;
    /**
     * 逾期日期
     */
    private String overdueDate;
    /**
     * 逾期本金
     */
    private BigDecimal overduePrin;
    /**
     * 逾期利息
     */
    private BigDecimal overdueInt;
    /**
     * 逾期罚息
     */
    private BigDecimal overduePenalty;
    /**
     * 逾期复利
     */
    private BigDecimal overdueCompound;
    /**
     * 逾期手续费
     */
    private BigDecimal overdueFee;
}
