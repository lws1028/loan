package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.ant.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AntRepaymentPlanDTO {
	private Integer repaymentNum;//还款期次

	private String repayDate;//应还款⽇期 yyyy/mm/dd

	private String actRepayDate;//实际还款⽇期 yyyy/mm/dd

	private BigDecimal payOverdueCorp;//应还本金

	private BigDecimal actualOverdueCorp;//实还本金

	private BigDecimal payInte;//应还利息

	private BigDecimal actualInte;//实还利息

	private BigDecimal payOverdueCorpInte;//应还本⾦罚息

	private BigDecimal actualOverdueCorpInte;//实还本⾦罚息

	private BigDecimal payIntefine;//利息罚息  即应还复利

	private BigDecimal actualIntefine;//实还利息罚息 即实还复利

	private BigDecimal payPoundageInte;//应还违约罚息

	private BigDecimal actualPoundageInte;//实还违约罚息

	private BigDecimal deservePoundage;//提前结清应还违约⾦

	private BigDecimal poundage;//提前结清实还违约⾦

	private BigDecimal serviceFee;//应还服务费

	private BigDecimal actualServiceFee;//实还服务费⾦

	private String payOffFlag;//结清标志   1代表结清，0代表未结清

	private String loanStatus;//期次级的贷款状态。NORMAL：正常OVD：逾期 CLEAR：结清

	public AntRepaymentPlanDTO(String actRepayDate, BigDecimal actualInte, BigDecimal actualIntefine, BigDecimal actualOverdueCorp, BigDecimal actualOverdueCorpInte,
							   BigDecimal actualPoundageInte, BigDecimal actualServiceFee, BigDecimal deservePoundage, BigDecimal payInte,
							   BigDecimal payIntefine, String payOffFlag, BigDecimal payOverdueCorp, BigDecimal payOverdueCorpInte, BigDecimal payPoundageInte,
							   BigDecimal poundage, String repayDate, Integer repaymentNum, BigDecimal serviceFee,String loanStatus) {
		this.actRepayDate = actRepayDate;
		this.actualInte = actualInte;
		this.actualIntefine = actualIntefine;
		this.actualOverdueCorp = actualOverdueCorp;
		this.actualOverdueCorpInte = actualOverdueCorpInte;
		this.actualPoundageInte = actualPoundageInte;
		this.actualServiceFee = actualServiceFee;
		this.deservePoundage = deservePoundage;
		this.payInte = payInte;
		this.payIntefine = payIntefine;
		this.payOffFlag = payOffFlag;
		this.payOverdueCorp = payOverdueCorp;
		this.payOverdueCorpInte = payOverdueCorpInte;
		this.payPoundageInte = payPoundageInte;
		this.poundage = poundage;
		this.repayDate = repayDate;
		this.repaymentNum = repaymentNum;
		this.serviceFee = serviceFee;
		this.loanStatus = loanStatus;
	}
}
