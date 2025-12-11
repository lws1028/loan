package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.autoCredit.core.strategy;

import com.zlhj.commonLoan.business.appCommon.enums.ClueChanelCode;
import com.zlhj.commonLoan.domain.creditBusiness.CreditOrderId;
import com.zlhj.unifiedInputPlatform.autoCredit.core.context.BoId;
import com.zlhj.unifiedInputPlatform.autoCredit.core.context.PreReviewContext;

public interface NotificationStrategy {
    /**
     * 预审通过通知
     */
    void onPrePass(BoId boId, ClueChanelCode clueChanelCode, CreditOrderId creditOrderId);

    /**
     * 预审拒绝通知
     */
    void onPreReject(BoId boId, ClueChanelCode clueChanelCode);

    /**
     * 流程异常通知
     */
    void onError(PreReviewContext context, Exception e);
}