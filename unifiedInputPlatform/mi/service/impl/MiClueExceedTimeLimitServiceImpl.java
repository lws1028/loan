package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.mi.service.impl;

import com.apollo.alds.util.ConvertionUtil;
import com.apollo.util.DateUtil;
import com.zlhj.commonLoan.domain.mainLoan.LoanId;
import com.zlhj.entity.BusinessBasicEntity;
import com.zlhj.mapper.SplBussinessbasicMapper;
import com.zlhj.unifiedInputPlatform.mi.service.MiClueExceedTimeLimitService;
import com.zlhj.unifiedInputPlatform.mi.service.MiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class MiClueExceedTimeLimitServiceImpl implements MiClueExceedTimeLimitService {
    private final SplBussinessbasicMapper splBussinessbasicMapper;
    private final MiService miService;
    public MiClueExceedTimeLimitServiceImpl(SplBussinessbasicMapper splBussinessbasicMapper, MiService miService) {
        this.splBussinessbasicMapper = splBussinessbasicMapper;
        this.miService = miService;
    }

    @Override
    public void handle() {
        log.info("合作渠道=15-小米贷款超市-30天超期定时任务开始：[{}]", DateUtil.formatToYYYYMMDDHHMMSS2(new Date()));
        //获取30天前的日期
        String date = DateUtil.getNowBeforeDate(30);
        log.info("合作渠道=15-小米贷款超市-30天超期定时任务，实际执行的创建时间[{}]", date);

        //筛选线索进件时间超过30天、没有进件提交线索
        List<String> applyNums = splBussinessbasicMapper.selectMiNotSubmitList(date);
        log.info("筛选线索进件时间超过30天、没有进件提交线索:[{}]", applyNums);
        if (applyNums.size() > 0) {
            for (String applyNum : applyNums) {
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
//                        basicEntity.forEach(r -> {
                            miService.pushCancelClueState(applyNum);
//                        });
                    }
                }
            }
        } else {
            log.info("合作渠道=15-小米贷款超市的线索-30天超期定时任务,查询无数据，不需要处理");
        }
    }
}
