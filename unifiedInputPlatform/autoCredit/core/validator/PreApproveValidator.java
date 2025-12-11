package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.autoCredit.core.validator;

import com.zlhj.commonLoan.business.appCommon.enums.ClueChanelCode;
import com.zlhj.infrastructure.po.CreditAuthorization;

public interface PreApproveValidator<T> {

	void validate(CreditAuthorization auth, T data);

	default boolean supports(ClueChanelCode channelCode) {
		return false;
	}
}
