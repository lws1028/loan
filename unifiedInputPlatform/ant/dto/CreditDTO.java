package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.ant.dto;

import com.zlhj.unifiedInputPlatform.ant.dto.CreditPricingDTO;
import lombok.Data;

import java.util.List;
@Data
public class CreditDTO {
	private String productCode;//产品代码
	private Long creditAmt;//授信金额
	private String expireTime;//授信到期时间
	private List<CreditPricingDTO> creditPricingList;//定价列表
	private String loanType;//贷款类型
}
