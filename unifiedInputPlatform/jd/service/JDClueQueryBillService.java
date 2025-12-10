package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.jd.service;

import com.zlhj.commonLoan.domain.mainLoan.LoanId;
import com.zlhj.unifiedInputPlatform.jd.vo.JDClueQueryBillVO;

public interface JDClueQueryBillService {
    JDClueQueryBillVO create(LoanId loanId);
    JDClueQueryBillVO createForJdJt(LoanId loanId);
}
