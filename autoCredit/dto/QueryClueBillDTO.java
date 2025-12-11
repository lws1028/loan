package com.zlhj.unifiedInputPlatform.autoCredit.dto;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryClueBillDTO {
    /**
     * 借据单号
     */
    private String mhtIouNo;
    /**
     * 放款金额
     */
    private String cashAmount;
    /**
     * 待还本金
     */
    private String pendingAmount;
    /**
     * 总期数
     */
    private String totalPeriod;
    /**
     * 还款方式
     */
    private String repaymentType;
    /**
     * 借据状态
     */
    private String status;
    /**
     * 逾期状态
     */
    private String overdueStatus;
    /**
     * 放款日期
     */
    private Timestamp cashCompleteDate;
    /**
     * 借款到期日期
     */
    private Timestamp loanEndDate;
    /**
     * 贷款年化利率
     */
    private String normalRate;

    /**
     * 最新待还日期
     */
    private Date repaymentPendingDate;
    /**
     * 还款计划
     */
    private List<RepaymentPlanDTO> repaymentPlanList;

    @Data
    public static class RepaymentPlanDTO {
        /**
         * 当期还款期数
         */
        private String paymentPeriod;
        /**
         * 当期计息开始时间
         */
        private Timestamp interestStartTime;
        /**
         * 当期计息结束时间
         */
        private Timestamp interestEndTime;
        /**
         * 当期还款日期
         */
        private Timestamp repaymentDate;
        /**
         * 当期应还总金额
         */
        private String totalAmount;
        /**
         * 当期应还本金
         */
        private String principalAmount;
        /**
         * 当期应还利息
         */
        private String interestAmount;
        /**
         * 当期应还罚息
         */
        private String punishAmount;
        /**
         * 当期应还复息
         */
        private String repeatInterest;
        /**
         * 当期应还担保费
         */
        private String guaranteeAmt;
        /**
         * 当期还款状态
         */
        private String paymentStatus;
        /**
         * 当期实际还款时间
         */
        private Timestamp repaymentRealTime;
        /**
         * 当期实际还款总金额
         */
        private String repaymentRealAmount;
        /**
         * 当期实还本金
         */
        private String principalRealAmount;
        /**
         * 当期实还利息
         */
        private String interestRealAmount;
        /**
         * 当期实还罚息
         */
        private String punishRealAmount;
        /**
         * 当期实还复息
         */
        private String realRepeatInterest;
        /**
         * 当期实还担保费
         */
        private String realGuaranteeAmt;
        /**
         * 逾期天数
         */
        private String overdueDays;
        /**
         * 逾期金额
         */
        private String overdueAmount;

        /**
         * 应还总金额
         */
        public String getTotalAmount() {
            // 应还本金
            BigDecimal principalAmount = StrUtil.isEmpty(this.getPrincipalAmount()) ? BigDecimal.ZERO : new BigDecimal(this.getPrincipalAmount());
            // 应还利息
            BigDecimal interestAmount = StrUtil.isEmpty(this.getInterestAmount()) ? BigDecimal.ZERO : new BigDecimal(this.getInterestAmount());
            // 应还罚息
            BigDecimal punishAmount =  StrUtil.isEmpty(this.getPunishAmount()) ? BigDecimal.ZERO : new BigDecimal(this.getPunishAmount());
            // 应还复息
            BigDecimal repeatInterest = StrUtil.isEmpty(this.getRepeatInterest()) ? BigDecimal.ZERO : new BigDecimal(this.getRepeatInterest());
            // 应还担保费
            BigDecimal guaranteeFee = StrUtil.isEmpty(this.getGuaranteeAmt()) ? BigDecimal.ZERO : new BigDecimal(this.getGuaranteeAmt());
            return BigDecimal.ZERO.add(principalAmount).add(interestAmount).add(punishAmount).add(repeatInterest).add(guaranteeFee).toString();
        }

        /**
         * 实还总金额
         */
        @JsonIgnore
        public String getActualAmount() {
            // 实还本金
            BigDecimal principalRealAmount = StrUtil.isEmpty(this.getPrincipalRealAmount()) ? BigDecimal.ZERO : new BigDecimal(this.getPrincipalRealAmount());
            // 实还利息
            BigDecimal interestRealAmount = StrUtil.isEmpty(this.getInterestRealAmount()) ? BigDecimal.ZERO : new BigDecimal(this.getInterestRealAmount());
            // 实还罚息
            BigDecimal punishRealAmount = StrUtil.isEmpty(this.getPunishRealAmount()) ? BigDecimal.ZERO : new BigDecimal(this.getPunishRealAmount());
            // 实还复息
            BigDecimal realRepeatInterest = StrUtil.isEmpty(this.getRealRepeatInterest()) ? BigDecimal.ZERO : new BigDecimal(this.getRealRepeatInterest());
            // 实还担保费
            BigDecimal realGuaranteeFee = StrUtil.isEmpty(this.getRealGuaranteeAmt()) ? BigDecimal.ZERO : new BigDecimal(this.getRealGuaranteeAmt());
            return BigDecimal.ZERO.add(principalRealAmount).add(interestRealAmount).add(punishRealAmount).add(realRepeatInterest).add(realGuaranteeFee).toString();
        }

        @JsonIgnore
        public boolean isValid() {
            if ("PAID".equals(this.paymentStatus)) {
                return this.getRepaymentRealTime() != null && this.getRepaymentRealAmount() != null && this.getPrincipalAmount() != null
                        && this.getInterestRealAmount() != null && this.getRealGuaranteeAmt() != null;
            }
            return true;
        }
    }
}
