package com.zlhj.unifiedInputPlatform.jdjt.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class JdJtClueStatusNotifyDTO {
	/**
	 * 线索编号
	 */
	private String boId;

	/**
	 * 状态
	 */
	private String checkStatus;

	/**
	 * 审核额度
	 */
	private String checkAmount;

	/**
	 * 车牌号
	 * 终审成功、提现成功状态不可为空
	 */
	private String carLicense;

	/**
	 * 支持的期数
	 * 终审成功、提现成功状态不可为空
	 */
	private Integer period;

	/**
	 * 年化利率
	 */
	private BigDecimal annualRate;

	/**
	 * 终审成功时间
	 */
	private Long applySuccTime;

	/**
	 * 资方借据单号
	 */
	private String mhtIouNo;

	/**
	 * 提现成功时间
	 */
	private Long paymentTime;

	/**
	 * 提现金额
	 */
	private String cashAmount;

	/**
	 * 实际出资方信息
	 * 提现成功状态不可为空
	 */
	private String realLenderId;

	/**
	 * 额度实现时间
	 * 终审成功、提现成功状态不可为空
	 */
	private Long expireTime;
}
