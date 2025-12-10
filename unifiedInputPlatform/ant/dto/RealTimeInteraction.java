package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.ant.dto;

import com.zlhj.commonLoan.business.clue.enums.LoanStatusChangeEnum;
import com.zlhj.commonLoan.domain.mainLoan.LoanId;
import lombok.Getter;

@Getter
public class RealTimeInteraction {

    private LoanId loanId;

    private Integer branchLoanId;

    private String applyNum;

    private LoanStatusChangeEnum antRealTimeStatusEnum;

    public RealTimeInteraction(LoanId loanId, Integer branchLoanId, String applyNum, LoanStatusChangeEnum antRealTimeStatusEnum) {
        this.loanId = loanId;
        this.branchLoanId = branchLoanId;
        this.applyNum = applyNum;
        this.antRealTimeStatusEnum = antRealTimeStatusEnum;
    }
}
