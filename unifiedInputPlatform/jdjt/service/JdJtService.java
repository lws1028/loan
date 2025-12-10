package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.jdjt.service;

import com.zlhj.mq.dto.JdJtPreApproveMessage;
import com.zlhj.unifiedInputPlatform.jd.vo.JDClueQueryBillVO;
import com.zlhj.unifiedInputPlatform.jdjt.dto.JdJtClueStatusNotifyDTO;
import com.zlhj.unifiedInputPlatform.jdjt.dto.JdJtQueryClueInfoDTO;
import com.zlhj.unifiedInputPlatform.jdjt.dto.VehicleEvaluationNotifyDTO;

public interface JdJtService {
	/**
	 * 自动预审
	 */
	void autoPreApprove(JdJtPreApproveMessage message);

	/**
	 * 线索信息查询
	 */
	JdJtClueStatusNotifyDTO queryClueInfo(JdJtQueryClueInfoDTO queryClueInfoDTO);

	/**
	 * 车架评估查询
	 */
	VehicleEvaluationNotifyDTO queryVehicleEvaluation(String applyNo);

	/**
	 * 还款计划查询
	 */
	JDClueQueryBillVO clueQueryBill(String applyNo);

	/**
	 * 推送放款成功通知
	 */
	void loanSuccessNotify();

	/**
	 * 还款变更通知
	 */
	void repayChangeNotify();

	/**
	 * 机构初审异常重试
	 */
	void preApproveRetry();

}
