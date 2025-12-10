package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.mi.service.impl;

import com.apollo.alds.util.ConvertionUtil;
import com.zlhj.commonLoan.domain.mainLoan.LoanId;
import com.zlhj.entity.BusinessBasicEntity;
import com.zlhj.mapper.SplBussinessbasicMapper;
import com.zlhj.unifiedInputPlatform.mi.service.MiOMService;
import com.zlhj.unifiedInputPlatform.mi.service.MiService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MiOMServiceImpl implements MiOMService {
    private final MiService miService;
    private final SplBussinessbasicMapper splBussinessbasicMapper;
    public MiOMServiceImpl(MiService miService,
                           SplBussinessbasicMapper splBussinessbasicMapper) {
        this.miService = miService;
        this.splBussinessbasicMapper = splBussinessbasicMapper;
    }


    @Override
    public void operationSendMiStatusInterface(String applyNum) {
        //查询该线索进件业务的当前贷款状态,若有多笔贷款取贷款状态最大的该笔
        List<Map<String, Object>> maxLoanStatus = splBussinessbasicMapper.getMaxLoanStatus(applyNum, 15);
        if (maxLoanStatus != null && maxLoanStatus.size() > 0) {
            //如果有多条，则取第一条
            Map<String, Object> maxStatus = maxLoanStatus.get(0);
            //sapdcslas表中汇总贷款id
            Integer loanId = ConvertionUtil.getSimpleIntegerWithNull(maxStatus.get("loanId"));
            //sapdcslas表中状态
            Integer loanStates = ConvertionUtil.getSimpleIntegerWithNull(maxStatus.get("status"));
            if (loanStates == 245){
                miService.pushApproveClueState(new LoanId(loanId));
            } else if (loanStates < -70){
                miService.pushApprovalRejectionClueState(new LoanId(loanId),loanStates);
            }else {
                miService.pushCancelClueState(new LoanId(loanId),loanStates);
            }

        } else {
            List<BusinessBasicEntity> basicEntity = splBussinessbasicMapper.getBusinessMaxLoanStatusByApplyNum(applyNum, 15);
            if (basicEntity != null && basicEntity.size() > 0) {
                basicEntity.forEach(r -> {
                    miService.pushCancelClueState(applyNum);
                });
            }
        }
    }
}
