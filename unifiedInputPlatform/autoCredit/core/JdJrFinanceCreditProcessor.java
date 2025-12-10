package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.autoCredit.core;

import com.zlhj.commonLoan.business.appCommon.domain.credit.TelemarketingCreditDomainService;
import com.zlhj.commonLoan.business.appCommon.domain.credit.ZQHTCredit;
import com.zlhj.commonLoan.business.appCommon.domain.credit.ZQHTCreditType;
import com.zlhj.commonLoan.business.appCommon.exception.PreRejectException;
import com.zlhj.unifiedInputPlatform.autoCredit.context.PreReviewContext;
import com.zlhj.unifiedInputPlatform.autoCredit.strategy.NotificationStrategy;

public class JdJrFinanceCreditProcessor extends AbstractPreReviewProcessor {
	private static final String STRATEGY_BEAN_NAME = "jdJrFinanceNotificationStrategy";

	public JdJrFinanceCreditProcessor(CreditServiceSupport creditServiceSupport, PreReviewContext context) {
		super(creditServiceSupport, context);
	}

	@Override
	protected NotificationStrategy chooseNotificationStrategy() {
		return creditServiceSupport.getNotificationStrategy(STRATEGY_BEAN_NAME);
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
