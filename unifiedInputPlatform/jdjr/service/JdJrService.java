package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.jdjr.service;

import com.zlhj.mq.dto.PreApproveMessage;

public interface JdJrService {
	/**
	 * 自动预审
	 */
	void autoPreApprove(PreApproveMessage message);

	/**
	 * 机构初审异常重试
	 */
	void preApproveRetry();

}
