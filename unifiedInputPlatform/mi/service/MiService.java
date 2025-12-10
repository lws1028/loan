package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.mi.service;

import com.zlhj.commonLoan.domain.mainLoan.LoanId;
import com.zlhj.unifiedInputPlatform.mi.dto.CarApplyDTO;

public interface MiService {
    /**
     * 车抵贷线索提交
     */
    void clueCarApply(CarApplyDTO carApplyDTO);
    /**
     * 线索状态通知(取消)
     */
    void pushCancelClueState(LoanId loanId,Integer loanStatus);
    /**
     * 线索状态通知(审批拒绝)
     */
    void pushApprovalRejectionClueState(LoanId loanId,Integer loanStatus);
    /**
     * 线索状态通知(审批通过)
     */
    void pushApproveClueState(LoanId loanId);

    /**
     * 线索状态通知(取消)
     */
    void pushCancelClueState(String applyNum);
}
