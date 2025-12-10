package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.universal.service;

import com.zlhj.commonLoan.domain.creditBusiness.CreditOrderId;
import com.zlhj.jd.vo.ClueApproveInfoDto;

public interface ClueApproveInfoService {

    void saveClueApproveInfo(ClueApproveInfoDto clueApproveInfoDto);

    ClueApproveInfoDto getClueApproveInfo(String boId);

    void updateIncomeProofFinished(String boId,String incomeProofMessage);

    void updateIncomeProofFinished(ClueApproveInfoDto clueApproveInfoDto);

    ClueApproveInfoDto getClueApproveInfoByCreditOrderId(CreditOrderId creditOrderId);
}
