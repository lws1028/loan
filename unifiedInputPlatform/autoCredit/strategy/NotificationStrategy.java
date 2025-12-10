package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.autoCredit.strategy;

import com.zlhj.unifiedInputPlatform.autoCredit.context.PreReviewContext;

public interface NotificationStrategy {
    /**
     * 预审通过通知
     */
    void onPrePass(PreReviewContext context);

    /**
     * 预审拒绝通知
     */
    void onPreReject(PreReviewContext context);

    /**
     * 流程异常通知
     */
    void onError(PreReviewContext context, Exception e);
}