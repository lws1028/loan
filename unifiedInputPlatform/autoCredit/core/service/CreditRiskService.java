package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.autoCredit.core.service;

import com.zlhj.commonLoan.business.appCommon.domain.credit.ZQHTCredit;
import com.zlhj.commonLoan.business.appCommon.enums.ZQHTCreditRuleResult;
import com.zlhj.commonLoan.business.appCommon.enums.ZQHTPreApproveRuleResult;
import com.zlhj.commonLoan.domain.creditBusiness.CreditOrderId;
import com.zlhj.commonLoan.domain.creditBusiness.hcd.HCDCreditBusiness;

public interface CreditRiskService {
	HCDCreditBusiness investigation(CreditOrderId creditOrderId);

	ZQHTCreditRuleResult processZQHTCredit(ZQHTCredit zqhtCredit);

	ZQHTPreApproveRuleResult processFullPricePreApproval(ZQHTCredit zqhtCredit, ZQHTCreditRuleResult zqhtCreditRuleResult);
}
