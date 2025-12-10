package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.jd.service;

import com.zlhj.commonLoan.domain.mainLoan.LoanId;
import com.zlhj.unifiedInputPlatform.autoCredit.dto.QueryClueBillDTO;

public interface JDClueQueryBillService {
    QueryClueBillDTO create(LoanId loanId);
    QueryClueBillDTO createForJdJt(LoanId loanId);
}
