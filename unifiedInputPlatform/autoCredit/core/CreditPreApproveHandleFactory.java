package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.autoCredit.core;

import com.zlhj.commonLoan.business.common.exception.BusinessException;
import com.zlhj.mq.dto.PreApproveMessage;
import com.zlhj.unifiedInputPlatform.autoCredit.enums.CreditChannelEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
@Slf4j
public class CreditPreApproveHandleFactory {

	@Autowired
	private Map<String, BaseCreditPreApproveHandle<Object>> handlerMap = new ConcurrentHashMap<>();

	public BaseCreditPreApproveHandle<Object> create(PreApproveMessage message) {
		CreditChannelEnum channelEnum = CreditChannelEnum.getByCode(message.getChannelCode());
		if (channelEnum == null) {
			log.error("未找到渠道编码 [{}] 对应的枚举配置", message.getChannelCode());
			throw new BusinessException("不支持的渠道编码: " + message.getChannelCode());
		}
		BaseCreditPreApproveHandle<Object> handler = handlerMap.get(channelEnum.getBeanName());
		if (handler == null) {
			log.error("未找到 BeanName=[{}] 对应的处理器实现", channelEnum.getBeanName());
			throw new BusinessException("系统未实现该渠道的预审逻辑: " + channelEnum.getDesc());
		}

		return handler;
	}
}