package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.mi.service.impl;

import com.zlhj.externalChannel.vo.SplBussinessBasicObject;
import com.zlhj.loan.SendEmailMessage;
import com.zlhj.unifiedInputPlatform.mi.service.MiSendEmailService;
import org.springframework.stereotype.Service;

@Service
public class MiSendEmailServiceImpl implements MiSendEmailService {
    private final SendEmailMessage sendEmailMessage;

    public MiSendEmailServiceImpl(SendEmailMessage sendEmailMessage) {
        this.sendEmailMessage = sendEmailMessage;
    }

    @Override
    public void send(SplBussinessBasicObject splBussinessBasic, String pullState) {
        String newLine = "\n";
        String emailContent = "各位好：" + newLine +
                "以下贷款发送小米线索审批通过状态推送异常，请及时处理：" + newLine +
                "申请编号：" + splBussinessBasic.getApplyNum() + "；" +
                "创建日期：" + splBussinessBasic.getSubmitDate() + "；" +
                "推送状态：" + pullState + "；" +
                "关联贷款：" + splBussinessBasic.getLoanId();

        sendEmailMessage.sendEmail("小米线索审批通过状态推送异常", emailContent, "小米线索审批通过状态推送异常邮件推送");
    }
}
