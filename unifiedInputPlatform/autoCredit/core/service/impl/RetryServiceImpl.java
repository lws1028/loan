package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.autoCredit.core.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.zlhj.common.core.utils.StringUtils;
import com.zlhj.mq.dto.PreApproveMessage;
import com.zlhj.unifiedInputPlatform.autoCredit.core.handler.CreditPreApproveHandleFactory;
import com.zlhj.unifiedInputPlatform.autoCredit.core.service.AutoPreApproveRetryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RetryServiceImpl implements AutoPreApproveRetryService {

	@Autowired
	protected RedisTemplate<String, String> redisTemplate;
	@Autowired
	private CreditPreApproveHandleFactory handleFactory;
	@Override
	public void handle() {
		String listRedisKey = "auto:credit:approve:error";
		Long size = this.redisTemplate.opsForList().size(listRedisKey);
		if (size != null && size > 0) {
			for (int i = 0; i < size; i++) {
				String messageStr = this.redisTemplate.opsForList().rightPop(listRedisKey);
				if (StringUtils.isBlank(messageStr)) {
					continue;
				}
				try {
					PreApproveMessage msg = JSONObject.parseObject(messageStr, PreApproveMessage.class);
					handleFactory.create(msg).executePreApprove(msg);
				} catch (Exception e) {
					log.error("[重试处理自动预审异常], 原因={}", e.getMessage(), e);
					// 重新入队
					this.redisTemplate.opsForList().leftPush(listRedisKey, messageStr);
				}
			}
		}
	}
}
