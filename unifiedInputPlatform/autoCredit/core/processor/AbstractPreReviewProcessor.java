package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.autoCredit.core.processor;

import com.zlhj.commonLoan.business.appCommon.enums.ClueChanelCode;
import com.zlhj.commonLoan.business.appCommon.enums.ZQHTCreditRuleResult;
import com.zlhj.commonLoan.business.appCommon.enums.ZQHTPreApproveRuleResult;
import com.zlhj.commonLoan.business.appCommon.exception.PreRejectException;
import com.zlhj.commonLoan.domain.creditBusiness.hcd.HCDCreditBusiness;
import com.zlhj.unifiedInputPlatform.autoCredit.core.context.BoId;
import com.zlhj.unifiedInputPlatform.autoCredit.core.context.PreReviewContext;
import com.zlhj.unifiedInputPlatform.autoCredit.core.CreditServiceSupport;
import com.zlhj.unifiedInputPlatform.autoCredit.core.service.CreditRiskService;
import com.zlhj.unifiedInputPlatform.autoCredit.core.strategy.NotificationStrategy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractPreReviewProcessor {

	protected final CreditServiceSupport creditServiceSupport;
	protected final PreReviewContext context;
	protected final NotificationStrategy notificationStrategy;
	private final CreditRiskService creditRiskService;
	public AbstractPreReviewProcessor(CreditServiceSupport creditServiceSupport, PreReviewContext context,
									  CreditRiskService creditRiskService1) {
		this.creditServiceSupport = creditServiceSupport;
		this.context = context;
		this.creditRiskService = creditRiskService1;
		this.notificationStrategy = chooseNotificationStrategy();
	}

	public final void execute() {
		log.info("[{}] 自动预审流程开始, boId={}", channelName(), context.boId());
		try {
			prepareData();

			investigate();

			executeRules();

			notification();
		} catch (PreRejectException e) {
			notificationStrategy.onPreReject(new BoId(context.boId()), ClueChanelCode.getByCode(context.channelCode()));
		} catch (Exception e) {
			notificationStrategy.onError(context, e);
		}
	}

	protected abstract String channelName();

	protected abstract void prepareData() throws PreRejectException;

	protected abstract void executeRules();

	protected abstract NotificationStrategy chooseNotificationStrategy();


	protected void investigate() {
		HCDCreditBusiness hcdCreditBusiness = creditRiskService.investigation(context.creditOrderId());
		context.changeHcdCreditBusiness(hcdCreditBusiness);
		context.changeZqhtCredit(hcdCreditBusiness);
	}

	protected void runStandardCreditScreening(PreReviewContext context) {
		ZQHTCreditRuleResult result = creditRiskService.processZQHTCredit(context.zqhtCredit());
		context.setCreditRuleResult(result);

		if (result == ZQHTCreditRuleResult.REJECT ||
				result == ZQHTCreditRuleResult.INTERFACE_EXCEPTION ||
				result == ZQHTCreditRuleResult.CREDIT_EXCEPTION) {

			context.zqhtCredit().changeCreditDateByCreditRuleResult(result);
			creditServiceSupport.updateZQHTCredit(context.zqhtCredit());
		}
	}

	protected void runStandardPreApproval(PreReviewContext context) {
		ZQHTPreApproveRuleResult preResult = creditRiskService.processFullPricePreApproval(context.zqhtCredit(), context.creditRuleResult());
		context.setPreApproveResult(preResult);

		if (preResult == ZQHTPreApproveRuleResult.REJECT ||
				preResult == ZQHTPreApproveRuleResult.AUTO_APPROVE ||
				preResult == ZQHTPreApproveRuleResult.PERSON_APPROVE ||
				preResult == ZQHTPreApproveRuleResult.STAY_PERSON_APPROVE ||
				preResult == ZQHTPreApproveRuleResult.AUTO_APPROVE_OR_PERSON_APPROVE ||
				preResult == ZQHTPreApproveRuleResult.CREDIT_REJECT) {

			context.zqhtCredit().changeCreditDateByPreApproveRuleResult(preResult);
			creditServiceSupport.updateZQHTCredit(context.zqhtCredit());
		}
	}

	private void notification() {
		if (isPass()) {
			notificationStrategy.onPrePass(new BoId(context.boId()), ClueChanelCode.getByCode(context.channelCode()), context.creditOrderId());
		}
		if (isReject()) {
			notificationStrategy.onPreReject(new BoId(context.boId()), ClueChanelCode.getByCode(context.channelCode()));
		}
	}

	private boolean isReject() {
		return context.preApproveResult() == ZQHTPreApproveRuleResult.REJECT ||
				context.preApproveResult() == ZQHTPreApproveRuleResult.CREDIT_REJECT ||
				context.preApproveResult() == ZQHTPreApproveRuleResult.STAY_PERSON_APPROVE;
	}

	private boolean isPass() {
		return context.preApproveResult() == ZQHTPreApproveRuleResult.AUTO_APPROVE ||
				context.preApproveResult() == ZQHTPreApproveRuleResult.PERSON_APPROVE ||
				context.preApproveResult() == ZQHTPreApproveRuleResult.AUTO_APPROVE_OR_PERSON_APPROVE;
	}
}