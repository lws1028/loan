package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.autoCredit.core.strategy;

import com.zlhj.commonLoan.business.appCommon.enums.ClueChanelCode;
import com.zlhj.commonLoan.business.clue.dto.LoanStatePushToClueDTO;
import com.zlhj.commonLoan.business.clue.enums.LoanStatusChangeEnum;
import com.zlhj.commonLoan.domain.creditBusiness.CreditOrderId;
import com.zlhj.unifiedInputPlatform.autoCredit.core.context.BoId;
import com.zlhj.unifiedInputPlatform.autoCredit.core.context.PreReviewContext;
import com.zlhj.unifiedInputPlatform.autoCredit.core.CreditServiceSupport;
import com.zlhj.unifiedInputPlatform.autoCredit.core.service.ClueQueryService;
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
	@Autowired
	private ClueQueryService clueQueryService;

	@Override
	public void onPrePass(BoId boId, ClueChanelCode clueChanelCode, CreditOrderId creditOrderId) {
		log.info("[京东金融] 策略执行：预审通过通知, boId={}", boId.getValue());

		VehicleEvaluationNotifyDTO evaluationNotifyDTO = clueQueryService.queryVehicleEvaluation(boId.getValue());

		LoanStatusChangeEnum statusChangeEnum = "SUCCESS".equals(evaluationNotifyDTO.getCheckStatus()) ?
				LoanStatusChangeEnum.AUTO_PRE_PASS : LoanStatusChangeEnum.AUTO_PRE_REJECT;

		doPush(boId, clueChanelCode, statusChangeEnum);

		creditServiceSupport.pushWeChatMessagePrePass(creditOrderId.get());
	}

	@Override
	public void onPreReject(BoId boId, ClueChanelCode clueChanelCode) {
		log.info("[京东金融] 策略执行：预审拒绝通知, boId={}", boId.getValue());
		doPush(boId, clueChanelCode, LoanStatusChangeEnum.AUTO_PRE_REJECT);
	}

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

	private void doPush(BoId boId, ClueChanelCode clueChanelCode, LoanStatusChangeEnum status) {
		LoanStatePushToClueDTO dto = new LoanStatePushToClueDTO(
				boId.getValue(), status.getValue(),
				clueChanelCode.code(), NOTIFY_BIZ_TYPE);

		creditServiceSupport.notifyCluePlatform(dto);
		creditServiceSupport.notifyPrResult(dto);
	}
}
