package com.zlhj.unifiedInputPlatform.ant.dto;

import lombok.Data;

@Data
public class CreditPricingDTO {
	private String intRate;//利率
	private Long loanTerm;//贷款期限
	private String loanTermUnit;//贷款期限单位Y、M、D分别代表年⽉⽇
	private String repayType;//还款⽅式
}
