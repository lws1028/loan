package com.zlhj.unifiedInputPlatform.autoCredit.core.service;

import com.zlhj.unifiedInputPlatform.autoCredit.dto.ClueStatusInfoDTO;
import com.zlhj.unifiedInputPlatform.autoCredit.dto.QueryClueBillDTO;
import com.zlhj.unifiedInputPlatform.autoCredit.dto.QueryClueInfoDTO;
import com.zlhj.unifiedInputPlatform.jdjt.dto.VehicleEvaluationNotifyDTO;

public interface ClueQueryService {
	ClueStatusInfoDTO queryClueInfo(QueryClueInfoDTO queryDTO);

	QueryClueBillDTO clueQueryBill(String applyNo);

	VehicleEvaluationNotifyDTO queryVehicleEvaluation(String boId);
}
