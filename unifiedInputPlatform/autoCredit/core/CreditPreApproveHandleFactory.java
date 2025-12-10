package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.autoCredit.core;

import com.zlhj.mq.dto.PreApproveMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class CreditPreApproveHandleFactory {

	@Autowired
	private CreditServiceSupport creditServiceSupport;

	public BaseCreditPreApproveHandle<Object> create(PreApproveMessage message) {
		if (message.getChannelCode() == null) {
			throw new IllegalArgumentException("channelCode 不能为空");
		}
		if ("23".equals(message.getChannelCode().toString())) {
			return new JdJrCreditPreApproveHandle();
		}

		throw new IllegalArgumentException("未知的征信渠道类型: " + zqhtCreditType);
	}
}