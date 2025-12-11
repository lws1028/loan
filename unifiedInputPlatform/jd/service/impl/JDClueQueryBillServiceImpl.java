package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.jd.service.impl;

import com.zlhj.commonLoan.domain.mainLoan.LoanId;
import com.zlhj.unifiedInputPlatform.autoCredit.core.ClueQueryBillBusinessFactory;
import com.zlhj.unifiedInputPlatform.autoCredit.core.QueryBillConvert;
import com.zlhj.unifiedInputPlatform.autoCredit.dto.QueryClueBillDTO;
import com.zlhj.unifiedInputPlatform.jd.pojo.ClueQueryBillBusiness;
import com.zlhj.unifiedInputPlatform.jd.service.JDClueQueryBillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JDClueQueryBillServiceImpl implements JDClueQueryBillService {

    private final ClueQueryBillBusinessFactory clueQueryBillBusinessFactory;

    @Override
    public QueryClueBillDTO create(LoanId loanId) {
        ClueQueryBillBusiness business = clueQueryBillBusinessFactory.create(loanId);
        return QueryBillConvert.convert(business);

    }

    @Override
    public QueryClueBillDTO createForJdJt(LoanId loanId) {
        ClueQueryBillBusiness business = clueQueryBillBusinessFactory.create(loanId);
        return QueryBillConvert.convert(business);
    }
}
