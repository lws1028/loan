package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.autoCredit.core;

import com.zlhj.commonLoan.business.appCommon.domain.credit.ZQHTCreditType;
import com.zlhj.unifiedInputPlatform.autoCredit.context.PreReviewContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class PreReviewProcessorFactory {

	@Autowired
	private CreditServiceSupport creditServiceSupport;

	public AbstractPreReviewProcessor createProcessor(ZQHTCreditType zqhtCreditType, PreReviewContext context) {
		if (zqhtCreditType == null) {
			throw new IllegalArgumentException("zqhtCreditType 不能为空");
		}
		if (zqhtCreditType.equals(ZQHTCreditType.JD_JR_AUTO)) {
			return new JdJrFinanceCreditProcessor(creditServiceSupport, context);
		}

		throw new IllegalArgumentException("未知的征信渠道类型: " + zqhtCreditType);
	}
}