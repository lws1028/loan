package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.universalInputPlatform.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * 当日还款
 */
@Setter
@Getter
@ToString(includeFieldNames=false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XAIntradayPayment implements IntradayPayment {
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
     * 还款渠道
     */
    private String repayChannel;
    /**
     * 云闪付用户号
     */
    private String custId;
    /**
     * 还款交易流水号
     */
    private String txnNo;
    /**
     * 借据号
     */
    private String duebillNo;
    /**
     * 还款日期
     */
    private String repayDate;
    /**
     * 还款期数
     */
    private String repayTerm;
    /**
     * 还款总额
     */
    private BigDecimal repayAmt;
    /**
     * 还款本金
     */
    private BigDecimal repayPrin;
    /**
     * 还款利息
     */
    private BigDecimal repayInt;
    /**
     * 还款罚息
     */
    private BigDecimal repayPenalty;
    /**
     * 还款复利
     */
    private BigDecimal repayCompound;
    /**
     * 还款手续费
     */
    private BigDecimal repayFee;
    /**
     * 还款保费
     */
    private BigDecimal repayPremium;
    /**
     * 还款类型
     */
    private String repayType;
    /**
     * 借据状态
     */
    private String loanStatus;
    /**
     * 还款状态
     */
    private String repayStatus;
    /**
     * 放款日期
     */
    private String putoutDate;
    /**
     * 还款平台费用
     */
    private BigDecimal guaranteeFee;
    /**
     * 还款平台罚费
     */
    private BigDecimal guaranteePenaltyFee;
}
