package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.autoCredit.core.processor;

import com.zlhj.commonLoan.business.appCommon.domain.credit.TelemarketingCreditDomainService;
import com.zlhj.commonLoan.business.appCommon.domain.credit.ZQHTCredit;
import com.zlhj.commonLoan.business.appCommon.domain.credit.ZQHTCreditType;
import com.zlhj.commonLoan.business.appCommon.exception.PreRejectException;
import com.zlhj.unifiedInputPlatform.autoCredit.core.context.PreReviewContext;
import com.zlhj.unifiedInputPlatform.autoCredit.core.CreditServiceSupport;
import com.zlhj.unifiedInputPlatform.autoCredit.core.service.CreditRiskService;
import com.zlhj.unifiedInputPlatform.autoCredit.core.strategy.NotificationStrategy;

public class JdJrFinanceCreditProcessor extends AbstractPreReviewProcessor {
	public JdJrFinanceCreditProcessor(CreditServiceSupport creditServiceSupport, PreReviewContext context, CreditRiskService creditRiskService) {
		super(creditServiceSupport, context,creditRiskService);
	}

	@Override
	protected NotificationStrategy chooseNotificationStrategy() {
		return creditServiceSupport.getNotificationStrategy("jdJrFinanceNotificationStrategy");
	}

	@Override
	protected String channelName() {
		return "京东金融";
	}

	@Override
	protected void prepareData() throws PreRejectException {
		try {
			ZQHTCredit credit;
			if (context.isCallbackFlow()) {
				credit = creditServiceSupport.findByHCDCreditBusiness(
						context.hcdCreditBusiness(), ZQHTCreditType.JD_JR_AUTO);
			} else {
				TelemarketingCreditDomainService domainService = creditServiceSupport.getDomainService("JdJrCreditDomainService");

				if (domainService == null) {
					throw new PreRejectException("未找到京东金融DomainService: " + "JdJrCreditDomainService");
				}

				credit = domainService.process(context.caId(), context.bankOrgNameType());
			}
			context.setZqhtCredit(credit);
		} catch (Exception e) {
			throw new PreRejectException(e.getMessage());
		}
	}

	@Override
	protected void executeRules() {
		runStandardCreditScreening(context);
		runStandardPreApproval(context);
	}
}
