package com.zlhj.unifiedInputPlatform.autoCredit.exceptions;

import com.zlhj.commonLoan.business.common.exception.BusinessException;

public class CarAgeNotMatchException extends BusinessException {
	public CarAgeNotMatchException() {
		super();
	}

	public CarAgeNotMatchException(String s) {
		super(s);
	}
}
