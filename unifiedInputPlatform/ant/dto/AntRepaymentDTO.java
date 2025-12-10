package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.ant.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AntRepaymentDTO {

	/**
	 * 产品类型
	 */
	private String product;

	/**
	 * 利率（费率）
	 */
	private BigDecimal resultNum;

	/**
	 * 贷款金额
	 */
	private BigDecimal applyMoney;

	/**
	 * 贷款期限
	 */
	private Integer term;

	/**
	 * 贷款期限
	 */
	private String lastTermDate;

	/**
	 * 放款日期
	 */
	private String grantMoneyDate;

	/**
	 * 逾期信息
	 */
	private String overdueSituation;

	/**
	 * 贷款id
	 */
	private Integer loanId;

}
