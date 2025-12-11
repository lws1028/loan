package com.zlhj.unifiedInputPlatform.autoCredit.core.context;

import com.zlhj.commonLoan.business.appCommon.domain.credit.ZQHTCredit;
import com.zlhj.commonLoan.business.appCommon.enums.ZQHTCreditRuleResult;
import com.zlhj.commonLoan.business.appCommon.enums.ZQHTPreApproveRuleResult;
import com.zlhj.commonLoan.domain.creditAuth.CreditAuthID;
import com.zlhj.commonLoan.domain.creditBusiness.CreditOrderId;
import com.zlhj.commonLoan.domain.creditBusiness.hcd.HCDCreditBusiness;
import com.zlhj.main.fumin.enums.BankOrgNameType;
import lombok.Builder;
import lombok.Setter;

@Setter
@Builder
public class PreReviewContext {
	private BoId boId;
	private CreditAuthID caId;
	private BankOrgNameType bankOrgNameType;
	private HCDCreditBusiness hcdCreditBusiness;
	private boolean isCallbackFlow;


	private ZQHTCredit zqhtCredit;
	private ZQHTCreditRuleResult creditRuleResult;
	private ZQHTPreApproveRuleResult preApproveResult;

	public String boId() {
		return boId.getValue();
	}

	public boolean isCallbackFlow() {
		return isCallbackFlow;
	}

	public HCDCreditBusiness hcdCreditBusiness() {
		return hcdCreditBusiness;
	}

	public BankOrgNameType bankOrgNameType() {
		return bankOrgNameType;
	}

	public CreditAuthID caId() {
		return caId;
	}

	public Integer channelCode() {
		return zqhtCredit.channelCode();
	}

	public ZQHTCreditRuleResult creditRuleResult() {
		return creditRuleResult;
	}

	public ZQHTPreApproveRuleResult preApproveResult() {
		return preApproveResult;
	}

	public ZQHTCredit zqhtCredit() {
		return zqhtCredit;
	}

	public CreditOrderId creditOrderId() {
		return zqhtCredit.creditOrderId();
	}

	public void changeHcdCreditBusiness(HCDCreditBusiness hcdCreditBusiness) {
		this.hcdCreditBusiness = hcdCreditBusiness;
	}

	public void changeZqhtCredit(HCDCreditBusiness hcdCreditBusiness) {
		this.zqhtCredit.changeHcdCreditBusiness(hcdCreditBusiness);
	}

	public boolean preApproveResultIsEmpty() {
		return preApproveResult == null;
	}
}
