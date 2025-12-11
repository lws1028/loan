package com.zlhj.unifiedInputPlatform.autoCredit.core.service.impl;

import com.zlhj.commonLoan.business.appCommon.domain.credit.ZQHTCredit;
import com.zlhj.commonLoan.business.appCommon.enums.ZQHTCreditRuleResult;
import com.zlhj.commonLoan.business.appCommon.enums.ZQHTPreApproveRuleResult;
import com.zlhj.commonLoan.business.appCommon.service.CreditInvestigationService;
import com.zlhj.commonLoan.business.appCommon.service.RuleEngineService;
import com.zlhj.commonLoan.domain.creditBusiness.CreditOrderId;
import com.zlhj.commonLoan.domain.creditBusiness.hcd.HCDCreditBusiness;
import com.zlhj.unifiedInputPlatform.autoCredit.core.service.CreditRiskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CreditRiskServiceImpl implements CreditRiskService {

	@Autowired
	private CreditInvestigationService creditInvestigationService;

	@Autowired
	private RuleEngineService ruleEngineService;

	public HCDCreditBusiness investigation(CreditOrderId creditOrderId) {
		return creditInvestigationService.investigation(creditOrderId);
	}

	public ZQHTCreditRuleResult processZQHTCredit(ZQHTCredit zqhtCredit) {
		return ruleEngineService.processZQHTCredit(zqhtCredit);
	}

	public ZQHTPreApproveRuleResult processFullPricePreApproval(ZQHTCredit zqhtCredit, ZQHTCreditRuleResult zqhtCreditRuleResult) {
		return ruleEngineService.processFullPricePreApproval(zqhtCredit, zqhtCreditRuleResult);
	}
}
