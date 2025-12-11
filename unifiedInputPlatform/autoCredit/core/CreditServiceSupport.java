package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.autoCredit.core;

import com.zlhj.commonLoan.business.appCommon.domain.credit.TelemarketingCreditDomainService;
import com.zlhj.commonLoan.business.appCommon.domain.credit.TelemarketingCreditRepository;
import com.zlhj.commonLoan.business.appCommon.domain.credit.ZQHTCredit;
import com.zlhj.commonLoan.business.appCommon.domain.credit.ZQHTCreditType;
import com.zlhj.commonLoan.business.basic.service.WeChatMessagePushService;
import com.zlhj.commonLoan.business.clue.dto.LoanStatePushToClueDTO;
import com.zlhj.commonLoan.domain.creditBusiness.hcd.HCDCreditBusiness;
import com.zlhj.loan.SendEmailMessage;
import com.zlhj.mq.provider.Sender;
import com.zlhj.unifiedInputPlatform.autoCredit.core.strategy.NotificationStrategy;
import com.zlhj.unifiedInputPlatform.universal.service.UnifiedInputPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CreditServiceSupport {
    @Autowired
    private TelemarketingCreditRepository telemarketingCreditRepository;
    @Autowired
    private Map<String, TelemarketingCreditDomainService> telemarketingCreditDomainServiceMap;
    @Autowired
    private Map<String, NotificationStrategy> notificationStrategyMap;
    @Autowired
    private UnifiedInputPlatformService unifiedInputPlatformService;
    @Autowired
    private Sender sender;
    @Autowired
    private WeChatMessagePushService weChatMessagePushService;
    @Autowired
    private SendEmailMessage sendEmailMessage;

    public NotificationStrategy getNotificationStrategy(String strategyBeanName) {
        return notificationStrategyMap.get(strategyBeanName);
    }

    public ZQHTCredit findByHCDCreditBusiness(HCDCreditBusiness hcdCreditBusiness, ZQHTCreditType zqhtCreditType) {
        return telemarketingCreditRepository.findByHCDCreditBusiness(hcdCreditBusiness, zqhtCreditType);
    }
    public TelemarketingCreditDomainService getDomainService(String domainService) {
        return telemarketingCreditDomainServiceMap.get(domainService);
    }

    public void sendWechatEmail(String subject, String emailContent, String interfaceType) {
        sendEmailMessage.sendEmail(subject, emailContent, interfaceType);
    }

    public void pushWeChatMessagePrePass(String creditOrderId) {
        weChatMessagePushService.pushWeChatMessagePrePass(creditOrderId);
    }

    public void notifyCluePlatform(LoanStatePushToClueDTO dto) {
        unifiedInputPlatformService.realTimeInteraction(dto);
    }

    public void notifyPrResult(LoanStatePushToClueDTO dto) {
        sender.preResultNotify(dto);
    }

    public void updateZQHTCredit(ZQHTCredit zqhtCredit) {
        telemarketingCreditRepository.update(zqhtCredit);
    }

}