package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.autoCredit.strategy;

import com.zlhj.commonLoan.business.clue.dto.LoanStatePushToClueDTO;
import com.zlhj.commonLoan.business.clue.enums.LoanStatusChangeEnum;
import com.zlhj.unifiedInputPlatform.autoCredit.context.PreReviewContext;
import com.zlhj.unifiedInputPlatform.autoCredit.core.CreditServiceSupport;
import com.zlhj.unifiedInputPlatform.jdjt.dto.VehicleEvaluationNotifyDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("jdJrFinanceNotificationStrategy")
@Slf4j
public class JdJrFinanceNotificationStrategy implements NotificationStrategy {
	private static final String NOTIFY_BIZ_TYPE = "JD_NOTICE_AUTO_FINANCE";

	@Autowired
	private CreditServiceSupport creditServiceSupport;

	@Override
	public void onError(PreReviewContext context, Exception e) {
		log.error("[京东金融] 策略执行：预审异常通知, boId={}", context.boId(), e);
		String emailContent =
				"处理京东金融电子征信流程异常，原因: " + e.getMessage() +
						"，boId=：" + context.boId() +
						"，caId=：" + context.caId().getValue() +
						",请及时跟进处理！";
		creditServiceSupport.sendWechatEmail("京东金融电子征信流程异常", emailContent, "京东金融电子征信流程异常");
	}

	@Override
	public void onPrePass(PreReviewContext context) {
		log.info("[京东金融] 策略执行：预审通过通知, boId={}", context.boId());

		VehicleEvaluationNotifyDTO evaluationNotifyDTO = creditServiceSupport.queryVehicleEvaluation(context.boId());

		LoanStatusChangeEnum statusChangeEnum = "SUCCESS".equals(evaluationNotifyDTO.getCheckStatus()) ? LoanStatusChangeEnum.AUTO_PRE_PASS : LoanStatusChangeEnum.AUTO_PRE_REJECT;

		doPush(context, statusChangeEnum);

		creditServiceSupport.pushWeChatMessagePrePass(context.creditOrderId().get());
	}

	@Override
	public void onPreReject(PreReviewContext context) {
		log.info("[京东金融] 策略执行：预审拒绝通知, boId={}", context.boId());
		doPush(context, LoanStatusChangeEnum.AUTO_PRE_REJECT);
	}


	private void doPush(PreReviewContext context, LoanStatusChangeEnum status) {
		LoanStatePushToClueDTO dto = new LoanStatePushToClueDTO(
				context.boId(), null, status.getValue(),
				context.channelCode(), NOTIFY_BIZ_TYPE);

		creditServiceSupport.notifyCluePlatform(dto);
		creditServiceSupport.notifyTelemarketing(dto);
	}
}
