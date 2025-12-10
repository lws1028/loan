package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.universalInputPlatform.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * 新安总账BI
 */
@Setter@Getter
@ToString(includeFieldNames=false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XAGeneralLedger implements GeneralLedger {
    /**
     * 合作伙伴ID
     */
    private String partnerId;
    /**
     * 对账日期
     */
    private String batchDate;
    /**
     * 贷款余额
     */
    private BigDecimal remainLoanAmt;
    /**
     * 总放款笔数
     */
    private Long loanSum;
    /**
     * 总放款金额
     */
    private BigDecimal loanAmt;
    /**
     * 总还款笔数
     */
    private Long repaySum;
    /**
     * 总还款金额
     */
    private BigDecimal repayAmt;
    /**
     * 总还本金
     */
    private BigDecimal repayPrin;
    /**
     * 总还利息
     */
    private BigDecimal repayInt;
    /**
     * 总还罚息
     */
    private BigDecimal repayPenelty;
    /**
     * 总还复利
     */
    private BigDecimal repayCompound;
    /**
     * 总还保费
     */
    private BigDecimal repayPremium;
    /**
     * 总还手续费
     */
    private BigDecimal repayFee;
    /**
     * 总授信申请人数
     */
    private Long applyCnt;
    /**
     * 总授信成功人数
     */
    private Long applySuccessCnt;
    /**
     * 总放款申请笔数
     */
    private Long putoutCnt;
    /**
     * 总放款成功笔数
     */
    private Integer putoutSuccessCnt;
}
