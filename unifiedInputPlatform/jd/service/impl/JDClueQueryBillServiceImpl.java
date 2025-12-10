package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.jd.service.impl;

import com.zlhj.commonLoan.domain.mainLoan.LoanId;
import com.zlhj.unifiedInputPlatform.jd.convert.JDQueryBillConvert;
import com.zlhj.unifiedInputPlatform.jd.pojo.JDClueQueryBillBusiness;
import com.zlhj.unifiedInputPlatform.jd.service.JDClueQueryBillService;
import com.zlhj.unifiedInputPlatform.jd.service.factory.JDClueQueryBillBusinessFactory;
import com.zlhj.unifiedInputPlatform.jd.vo.JDClueQueryBillVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JDClueQueryBillServiceImpl implements JDClueQueryBillService {

    private final JDClueQueryBillBusinessFactory jdClueQueryBillBusinessFactory;

    @Override
    public JDClueQueryBillVO create(LoanId loanId) {
        JDClueQueryBillBusiness business = jdClueQueryBillBusinessFactory.create(loanId);
        return JDQueryBillConvert.convert(business);

    }

    @Override
    public JDClueQueryBillVO createForJdJt(LoanId loanId) {
        JDClueQueryBillBusiness business = jdClueQueryBillBusinessFactory.create(loanId);
        return JDQueryBillConvert.convertForJdJt(business);
    }
}
