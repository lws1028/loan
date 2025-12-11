package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.jd.service;

import com.zlhj.unifiedInputPlatform.ant.dto.ClueStatusNotifyDTO;
import com.zlhj.unifiedInputPlatform.autoCredit.dto.QueryClueBillDTO;
import com.zlhj.unifiedInputPlatform.jd.dto.JDQueryClueStatusCommand;
import com.zlhj.commonLoan.domain.cule.ClueNumber;

public interface JDService {
	/**
	 * 京东推送放款成功通知
	 */
	void loanSuccessNotify();
	ClueStatusNotifyDTO queryClueStatus(JDQueryClueStatusCommand clueNumber);
	void jdAutoFinanceNotSubmitSchedule();
	void jdCarLifeNotSubmitSchedule();
	void repaymentPlanChangeNotify();
	/**
	 * 查询账单详情
	 */
	QueryClueBillDTO clueQueryBill(ClueNumber clueNumber);

	ClueStatusNotifyDTO carLifeQueryClueStatus(JDQueryClueStatusCommand command);
}
