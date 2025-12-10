package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.ant.service;

import com.zlhj.commonLoan.business.common.exception.AreaInadmissibleException;
import com.zlhj.commonLoan.business.common.exception.ClueResubmissionException;
import com.zlhj.unifiedInputPlatform.ant.dto.*;

import java.util.List;
import java.util.Map;

public interface AntServiceFacade {

    /**
     * 线索进件
     */
    ClueApplyOutput culeApply(ClueApplyInput input) throws ClueResubmissionException, AreaInadmissibleException;

    /**
     * 线索状态查询
     */
    ClueStatusNotifyDTO queryClue(AntQueryCluePreDTO queryDTO);

    /**
     * 还款计划信息查询
     */
    List<AntRepaymentPlanDTO> queryRepayPlan(String applyNo);

    /**
     * 授信信息查询
     */
    /**
     * 针对蚂蚁机构初审回传信息查询
     */
    ClueStatusNotifyDTO queryClueForOrgApprove(AntQueryCluePreDTO preDTO);

    ClueAdditionalResult queryIncomeProof(Integer loanId);

    ClueAdditionalResult queryIncomeProof(String boId);
}
