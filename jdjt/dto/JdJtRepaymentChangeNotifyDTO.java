package com.zlhj.unifiedInputPlatform.jdjt.dto;

import lombok.Data;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Data
public class JdJtRepaymentChangeNotifyDTO {
	private Integer loanId;
	//京东线索编号
	private String outApplyNo;
	//我司
	private String orgApplyNo;
	//机构侧支用号
	private String orgDrawdownNo;
	//还款金额
	private String repaymentAmount;
	//资方还款单号
	private Integer finDrawdownNo;
	//期数
	private String paymentPeriod;
	private String repaymentDate;
	private BigDecimal principalAmount;
	private BigDecimal punishAmount;
	private BigDecimal interestAmount;
	private BigDecimal guaranteeAmount;
	private BigDecimal carDisplsalAmount;

	public String orgApplyNo() {
		if (StringUtils.hasLength(outApplyNo) && outApplyNo.contains("ZQHT")){
			return outApplyNo;
		}
		return orgApplyNo;
	}

	public String outApplyNo() {
		if (StringUtils.hasLength(outApplyNo) && outApplyNo.contains("ZQHT")){
			return "";
		}
		return outApplyNo;
	}
}
