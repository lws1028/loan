package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.autoCredit.core;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.apollo.alds.util.ConvertionUtil;
import com.zlhj.Interface.batchVo.LoanRepayDetailObject;
import com.zlhj.commonLoan.business.common.exception.BusinessException;
import com.zlhj.commonLoan.interfaces.ksyh.pojo.AdditionalFeeRepayPlan;
import com.zlhj.unifiedInputPlatform.autoCredit.dto.QueryClueBillDTO;
import com.zlhj.unifiedInputPlatform.jd.pojo.ClueQueryBillBusiness;
import com.zlhj.user.vo.MultipleLoanObject;
import com.zlhj.util.ToolsUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class QueryBillConvert {

    private final static String REPAYMENT_TYPE = "EQUAL_PRINCIPAL_INTEREST";

    public static QueryClueBillDTO convert(ClueQueryBillBusiness business) {
        // 校验基本对象
        MultipleLoanObject loanObject = Optional.ofNullable(business.getMultipleLoanObject())
                .orElseThrow(() -> new BusinessException("线索没有匹配的贷款数据"));
        List<LoanRepayDetailObject> loanRepayDetailObjects = Optional.ofNullable(business.getLoanRepayDetailObjects())
                .orElseThrow(() -> new BusinessException("还款计划表为空"));

        if (!business.judgeLoanStatus()) {
            throw new BusinessException("仅支持已放款后的贷款");
        }

        // 费用计划转Map
        Map<Integer, AdditionalFeeRepayPlan> additionalFeeRepayPlanMap = Optional.ofNullable(business.getAdditionalFeeRepayPlans())
                .orElseGet(Collections::emptyList)
                .stream()
                .collect(Collectors.toMap(AdditionalFeeRepayPlan::getApTerm, r -> r));

        Map<Integer, AdditionalFeeRepayPlan> gpsFeeRepayPlanMap = Optional.ofNullable(business.getGpsFeeRepayPlans())
                .orElseGet(Collections::emptyList)
                .stream()
                .collect(Collectors.toMap(AdditionalFeeRepayPlan::getApTerm, r -> r));

        // 基础字段（必填校验）
        String mhtIouNo = getRequiredField(loanObject.getM_loanNumber(), "借据单号为空");
        BigDecimal cashAmount = BigDecimal.valueOf(getRequiredField(loanObject.getM_applyMoney(), "放款金额为空")).multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP);
        Integer totalPeriod = getRequiredField(loanObject.getM_term(), "总期数为空");
        Timestamp cashCompleteDate = DateUtil.parse(getRequiredField(loanObject.getM_grantMoneyDate(), "放款日期为空")).toTimestamp();
        // 位数为6位
        BigDecimal normalRate = BigDecimal.valueOf(getRequiredField(loanObject.getM_commonRate(), "贷款年化利率为空")).setScale(6, RoundingMode.HALF_UP);

        // 待还本金计算
        BigDecimal pendingAmount = loanRepayDetailObjects.stream()
                .map(r -> ToolsUtil.getBigDecimalWithNull(r.getM_thisCapital())
                        .subtract(ToolsUtil.getBigDecimalWithNull(r.getM_capital())))
                .reduce(BigDecimal.ZERO, BigDecimal::add).multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP);

        // 借据状态和逾期状态
        String status = loanRepayDetailObjects.stream()
                .allMatch(r -> "CLEAR".equals(getOverdue(r, additionalFeeRepayPlanMap, gpsFeeRepayPlanMap))) ? "SETTLE" : "LOANED";
        String overdueStatus = loanRepayDetailObjects.stream()
                .anyMatch(r -> "OVD".equals(getOverdue(r, additionalFeeRepayPlanMap, gpsFeeRepayPlanMap))) ? "1" : "0";
        //若逾期，取最早逾期期数的应还日期
        Date repaymentPendingDate = null;
        if ("1".equals(overdueStatus)){
            LoanRepayDetailObject repayDetailObject = loanRepayDetailObjects.stream()
                    .filter(r -> "1".equals(r.getM_repaymentState()))
                    .min(Comparator.comparing(LoanRepayDetailObject::getM_repaymentDate)).orElse(null);
            repaymentPendingDate = repayDetailObject != null ? parseDate(repayDetailObject.getM_repaymentDate()): null;
        }else {
            LoanRepayDetailObject repayDetailObject = loanRepayDetailObjects.stream()
                    .filter(r -> !"1".equals(r.getM_repaymentState()))
                    .filter(r -> !"0".equals(r.getM_repaymentState()))
                    .min(Comparator.comparing(LoanRepayDetailObject::getM_repaymentDate)).orElse(null);
            if (repayDetailObject == null){
                repayDetailObject = loanRepayDetailObjects.stream().max(Comparator.comparing(LoanRepayDetailObject::getM_thisTerm)).orElse(null);
            }
            repaymentPendingDate = repayDetailObject != null ? parseDate(repayDetailObject.getM_repaymentDate()): null;
        }

        // 还款计划列表
        List<QueryClueBillDTO.RepaymentPlanDTO> repaymentPlanList = loanRepayDetailObjects.stream()
                .map(r -> LoanRepayDetailObject.create(r, additionalFeeRepayPlanMap, gpsFeeRepayPlanMap))
                .collect(Collectors.toList());

        // 还款计划 - 补充计息开始时间
        repaymentPlanList.forEach(plan -> {
            String paymentPeriod = plan.getPaymentPeriod();
            if ("1".equals(paymentPeriod)) {
                plan.setInterestStartTime(cashCompleteDate);
            } else {
                BigDecimal needPeriod = BigDecimal.valueOf(Integer.parseInt(paymentPeriod) - 1);
                Optional<LoanRepayDetailObject> previousTerm = loanRepayDetailObjects.stream()
                        .filter(q -> q.getM_thisTerm().compareTo(needPeriod) == 0)
                        .findFirst();
                previousTerm.ifPresent(prev -> plan.setInterestStartTime(
                        DateUtil.offsetDay(DateUtil.parse(prev.getM_repaymentDate()), 1).toTimestamp()));
            }
        });
        LoanRepayDetailObject repayDetailObject = loanRepayDetailObjects.stream().max(Comparator.comparing(LoanRepayDetailObject::getM_thisTerm)).orElse(null);
        Timestamp loanEndDate = repayDetailObject != null ? DateUtil.parse(repayDetailObject.getM_repaymentDate()).toTimestamp() : null;
        // 封装结果
        QueryClueBillDTO queryClueBillDTO = new QueryClueBillDTO();
        queryClueBillDTO.setMhtIouNo(mhtIouNo);
        queryClueBillDTO.setCashAmount(ConvertionUtil.getSimpleStringWithNull(cashAmount));
        queryClueBillDTO.setPendingAmount(ConvertionUtil.getSimpleStringWithNull(pendingAmount));
        queryClueBillDTO.setTotalPeriod(ConvertionUtil.getSimpleStringWithNull(totalPeriod));
        queryClueBillDTO.setRepaymentType(REPAYMENT_TYPE);
        queryClueBillDTO.setStatus(status);
        queryClueBillDTO.setOverdueStatus(overdueStatus);
        queryClueBillDTO.setCashCompleteDate(cashCompleteDate);
        queryClueBillDTO.setLoanEndDate(loanEndDate);
        queryClueBillDTO.setNormalRate(ConvertionUtil.getSimpleStringWithNull(normalRate));
        queryClueBillDTO.setRepaymentPlanList(repaymentPlanList);
        queryClueBillDTO.setRepaymentPendingDate(repaymentPendingDate);
        return queryClueBillDTO;
    }
    /**
     * 逾期 状态
     */
    public static String getOverdue(LoanRepayDetailObject loanRepayDetailObject,
                                     Map<Integer, AdditionalFeeRepayPlan> additionalFeeRepayPlanMap,
                                     Map<Integer, AdditionalFeeRepayPlan> gpsFeeRepayPlanMap) {

        if (loanRepayDetailObject == null || loanRepayDetailObject.getM_repaymentDate() == null){
            return "NORMAL";
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        ParsePosition pos = new ParsePosition(0);
        if (new Date().compareTo(dateFormat.parse(loanRepayDetailObject.getM_repaymentDate(), pos)) > 0 && "1".equals(loanRepayDetailObject.getM_repaymentState())) {
            return "OVD";
        }

        AdditionalFeeRepayPlan additionalFeeRepayPlan = additionalFeeRepayPlanMap.get(loanRepayDetailObject.getM_thisTerm().intValue());
        if (additionalFeeRepayPlan != null){

            if (additionalFeeRepayPlan.getApOverdueStatus() != null && additionalFeeRepayPlan.getApOverdueStatus()){
                return "OVD";
            }
        }

        AdditionalFeeRepayPlan gpsAdditionalFeeRepayPlan = gpsFeeRepayPlanMap.get(loanRepayDetailObject.getM_thisTerm().intValue());
        if (gpsAdditionalFeeRepayPlan != null){
            if (gpsAdditionalFeeRepayPlan.getApOverdueStatus() != null && gpsAdditionalFeeRepayPlan.getApOverdueStatus()){
                return "OVD";
            }
        }

        BigDecimal overdueMoney = getOverdueMoney(loanRepayDetailObject);

        BigDecimal additionalFee = actualPremiumOverdueAmount(additionalFeeRepayPlan);
        BigDecimal gpsAdditionalFee = actualPremiumOverdueAmount(gpsAdditionalFeeRepayPlan);

        if (overdueMoney
                .add(additionalFee)
                .add(gpsAdditionalFee)
                .compareTo(BigDecimal.ZERO) == 0 ){

            return "CLEAR";
        }

        return "NORMAL";
	}

    /**
     * 逾期天数
     */
    public static Integer getOverdueDay(
            LoanRepayDetailObject loanRepayDetailObject,
            Map<Integer, AdditionalFeeRepayPlan> additionalFeeRepayPlanMap,
            Map<Integer, AdditionalFeeRepayPlan> gpsFeeRepayPlanMap) {

        if (loanRepayDetailObject == null || loanRepayDetailObject.getM_repaymentDate() == null) {
            return 0;
        }

        long repaymentOverdueDay = 0;
        long additionalOverdueDay = 0;
        long gpsOverdueDay = 0;

        // 计算贷款逾期天数
        if (new Date().after(parseDate(loanRepayDetailObject.getM_repaymentDate()))
                && "1".equals(loanRepayDetailObject.getM_repaymentState())) {
            DateTime repaymentDate = DateUtil.parse(loanRepayDetailObject.getM_repaymentDate());
            repaymentOverdueDay = DateUtil.betweenDay(repaymentDate, DateUtil.date(), false);
        }

        int currentTerm = loanRepayDetailObject.getM_thisTerm().intValue();

        // 计算附加费逾期天数
        AdditionalFeeRepayPlan additionalPlan = additionalFeeRepayPlanMap.get(currentTerm);
        if (Boolean.TRUE.equals(Optional.ofNullable(additionalPlan).map(AdditionalFeeRepayPlan::getApOverdueStatus).orElse(false))) {
            Date apRepayDate = Optional.ofNullable(additionalPlan.getApRepayDate())
                    .orElseThrow(() -> new BusinessException("附加费应还日期为空"));
            additionalOverdueDay = DateUtil.betweenDay(apRepayDate, DateUtil.date(), false);
        }

        // 计算 GPS 附加费逾期天数
        AdditionalFeeRepayPlan gpsPlan = gpsFeeRepayPlanMap.get(currentTerm);
        if (Boolean.TRUE.equals(Optional.ofNullable(gpsPlan).map(AdditionalFeeRepayPlan::getApOverdueStatus).orElse(false))) {
            Date gpsRepayDate = Optional.ofNullable(gpsPlan.getApRepayDate())
                    .orElseThrow(() -> new BusinessException("GPS附加费应还日期为空"));
            gpsOverdueDay = DateUtil.betweenDay(gpsRepayDate, DateUtil.date(), false);
        }

        // 所有逾期金额为 0，则不算逾期
        BigDecimal totalOverdue = getOverdueMoney(loanRepayDetailObject)
                .add(actualPremiumOverdueAmount(additionalPlan))
                .add(actualPremiumOverdueAmount(gpsPlan));

        if (totalOverdue.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }

        return (int) Math.max(repaymentOverdueDay, Math.max(additionalOverdueDay, gpsOverdueDay));
    }

    /**
     * 剩余应还金额
     */
    public static BigDecimal getOverdueMoney(LoanRepayDetailObject loanRepayDetailObject) {
        if (loanRepayDetailObject == null || loanRepayDetailObject.getM_repaymentDate() == null){
            return BigDecimal.ZERO;
        }

        BigDecimal mThisInterest = loanRepayDetailObject.getM_thisInterest() == null ? BigDecimal.ZERO : loanRepayDetailObject.getM_thisInterest();
        BigDecimal mThisCapital = loanRepayDetailObject.getM_thisCapital() == null ? BigDecimal.ZERO : loanRepayDetailObject.getM_thisCapital();
        BigDecimal mThisPremium = loanRepayDetailObject.getM_thisPremium() == null ? BigDecimal.ZERO : loanRepayDetailObject.getM_thisPremium();
        BigDecimal mThisPenalty = loanRepayDetailObject.getM_thisPenalty() == null ? BigDecimal.ZERO : loanRepayDetailObject.getM_thisPenalty();
        BigDecimal mThisCompound = loanRepayDetailObject.getM_thisCompound() == null ? BigDecimal.ZERO : loanRepayDetailObject.getM_thisCompound();
        BigDecimal mInterest = loanRepayDetailObject.getM_interest() == null ? BigDecimal.ZERO : loanRepayDetailObject.getM_interest();
        BigDecimal mPenalty = loanRepayDetailObject.getM_penalty() == null ? BigDecimal.ZERO : loanRepayDetailObject.getM_penalty();
        BigDecimal mCompound = loanRepayDetailObject.getM_compound() == null ? BigDecimal.ZERO : loanRepayDetailObject.getM_compound();
        BigDecimal mCapital = loanRepayDetailObject.getM_capital() == null ? BigDecimal.ZERO : loanRepayDetailObject.getM_capital();
        return mThisInterest
                .add(mThisCapital)
                .add(mThisPremium)
                .add(mThisPenalty)
                .add(mThisCompound)
                .subtract(mInterest)
                .subtract(mPenalty)
                .subtract(mCompound)
                .subtract(mCapital);
    }

    /**
     * 逾期金额（附加担保费)
     */
    private static BigDecimal actualPremiumOverdueAmount(AdditionalFeeRepayPlan gpsAdditionalFeeRepayPlan){
        if (gpsAdditionalFeeRepayPlan == null){
            return BigDecimal.ZERO;
        }

        if (gpsAdditionalFeeRepayPlan.getApOverdueStatus() != null && !gpsAdditionalFeeRepayPlan.getApOverdueStatus()){
            return BigDecimal.ZERO;
        }
        return QueryBillConvert.shouldPremiumRepayAmount(gpsAdditionalFeeRepayPlan).subtract(QueryBillConvert.actualPremiumRepaymentAmount(gpsAdditionalFeeRepayPlan));
    }

    /**
     * 应还总额(附加担保费)
     */
    private static BigDecimal shouldPremiumRepayAmount(AdditionalFeeRepayPlan gpsAdditionalFeeRepayPlan){
        if (gpsAdditionalFeeRepayPlan == null){
            return BigDecimal.ZERO;
        }

        return (gpsAdditionalFeeRepayPlan.getApRepayAmount() == null ? BigDecimal.ZERO :gpsAdditionalFeeRepayPlan.getApRepayAmount())
                .add(
                        (gpsAdditionalFeeRepayPlan.getApPenalSum() == null ? BigDecimal.ZERO : gpsAdditionalFeeRepayPlan.getApPenalSum())
                );
    }
    /**
     * 当期实还总额(附加担保费用)
     */
    private static BigDecimal actualPremiumRepaymentAmount(AdditionalFeeRepayPlan gpsAdditionalFeeRepayPlan){
        if (gpsAdditionalFeeRepayPlan == null){
            return BigDecimal.ZERO;
        }

        return (gpsAdditionalFeeRepayPlan.getApActualRepayAmount() == null ? BigDecimal.ZERO :gpsAdditionalFeeRepayPlan.getApActualRepayAmount())
                .add(
                        (gpsAdditionalFeeRepayPlan.getApRealPenalSum() == null ? BigDecimal.ZERO : gpsAdditionalFeeRepayPlan.getApRealPenalSum())
                );
    }

    /**
     * 通用字段必填校验。
     */
    private static <T> T getRequiredField(T value, String message) {
        return Optional.ofNullable(value)
                .orElseThrow(() -> new BusinessException(message));
    }

    private static Date parseDate(String dateStr) {
        return new SimpleDateFormat("yyyy-MM-dd").parse(dateStr, new ParsePosition(0));
    }

}
