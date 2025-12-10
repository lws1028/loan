package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.universalInputPlatform.dto;

import lombok.*;

import java.math.BigDecimal;

/**
 * 当日总账BI
 */
@Setter
@Getter
@ToString(includeFieldNames=false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XAIntradayGeneralLedger implements IntradayGeneralLedger {
    /**
     * 合作伙伴ID
     */
    private String partnerId;
    /**
     * 对账日期
     */
    private String batchDate;
    /**
     * 当日放款笔数
     */
    private Long dayLoanSum;
    /**
     * 当日放款金额
     */
    private BigDecimal dayLoanAmt;
    /**
     * 当日还款笔数
     */
    private Long dayRepaySum;
    /**
     * 当日还款金额
     */
    private BigDecimal dayRepayAmt;
    /**
     * 当日还本金
     */
    private BigDecimal dayRepayPrin;
    /**
     * 当日还利息
     */
    private BigDecimal dayRepayInt;
    /**
     * 当日还罚息
     */
    private BigDecimal dayRepayPenalty;
    /**
     * 当日还复利
     */
    private BigDecimal dayRepayCompound;
    /**
     * 当日还保费
     */
    private BigDecimal dayPremium;
    /**
     * 当日还手续费
     */
    private BigDecimal dayRepayFee;
    /**
     * 授信申请人数
     */
    private Long applyCnt;
    /**
     * 授信成功人数
     */
    private Long applySuccessCnt;
    /**
     * 放款申请笔数
     */
    private Long putoutCnt;
    /**
     * 放款成功笔数
     */
    private Integer putoutSuccessCnt;
}
