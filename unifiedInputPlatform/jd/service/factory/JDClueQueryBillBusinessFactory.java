package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.jd.service.factory;

import com.zlhj.Interface.batchVo.LoanRepayDetailObject;
import com.zlhj.Interface.batchVo.LoanRepayDetailRepository;
import com.zlhj.commonLoan.domain.mainLoan.LoanId;
import com.zlhj.commonLoan.interfaces.ksyh.pojo.AdditionalFeeInformation;
import com.zlhj.commonLoan.interfaces.ksyh.pojo.AdditionalFeeInformationRepository;
import com.zlhj.commonLoan.interfaces.ksyh.pojo.AdditionalFeeRepayPlan;
import com.zlhj.commonLoan.interfaces.ksyh.pojo.AdditionalFeeRepayPlanRepository;
import com.zlhj.loan.entity.Sapdcslas;
import com.zlhj.loan.entity.SapdcslasRepository;
import com.zlhj.unifiedInputPlatform.jd.pojo.JDClueQueryBillBusiness;
import com.zlhj.user.vo.MultipleLoanObject;
import com.zlhj.user.vo.MultipleLoanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JDClueQueryBillBusinessFactory {
    private static final String MAIN_LOAN = "2";
    private static final String ADDITIONAL_SECURITY_DEPOSIT = "1";//附加担保费
    private static final String GPS_SECURITY_DEPOSIT = "2";//gps担保费

    private final SapdcslasRepository sapdcslasRepository;
    private final MultipleLoanRepository multipleLoanRepository;
    private final LoanRepayDetailRepository loanRepayDetailRepository;
    private final AdditionalFeeRepayPlanRepository additionalFeeRepayPlanRepository;
    private final AdditionalFeeInformationRepository additionalFeeInformationRepository;
    public JDClueQueryBillBusiness create(LoanId loanId){
        Sapdcslas sapdcslas = getSapdcslas(loanId.getValue());//贷款节点信息
        MultipleLoanObject mainLoan = getLoanObject(loanId.get(), MAIN_LOAN);//主贷款信息

        List<LoanRepayDetailObject> mainLoanRepayment =getLoanRepaymentObject(mainLoan);//主业务还款信息

        AdditionalFeeInformation additionalFeeInformation = getYuanEuroguaranteeInFormation(loanId.get(),ADDITIONAL_SECURITY_DEPOSIT);//元欧附加担保费信息
        AdditionalFeeInformation gpsFeeInformation = getYuanEuroguaranteeInFormation(loanId.get(),GPS_SECURITY_DEPOSIT);//元欧GPS担保费信息

        List<AdditionalFeeRepayPlan> additionalFeeRepayPlans = getAdditionalPremiumRepaymentObject(additionalFeeInformation);//附加担保费还款信息
        List<AdditionalFeeRepayPlan> gpsFeeRepayPlans = getGPSPremiumRepaymentObject(gpsFeeInformation);//gps担保费还款信息

        JDClueQueryBillBusiness jdClueQueryBillBusiness = new JDClueQueryBillBusiness();
        jdClueQueryBillBusiness.setLoanId(loanId.getValue());
        jdClueQueryBillBusiness.setSapdcslas(sapdcslas);
        jdClueQueryBillBusiness.setMultipleLoanObject(mainLoan);
        jdClueQueryBillBusiness.setGpsFeeRepayPlans(gpsFeeRepayPlans);
        jdClueQueryBillBusiness.setAdditionalFeeRepayPlans(additionalFeeRepayPlans);
        jdClueQueryBillBusiness.setLoanRepayDetailObjects(mainLoanRepayment);
        return jdClueQueryBillBusiness;
    }

    private Sapdcslas getSapdcslas(Integer loanId){
        return sapdcslasRepository.getSapdcslasByLoanId(loanId);
    }

    /**
     * 主或附加贷款信息
     */
    private MultipleLoanObject getLoanObject(Integer loanId, String loanType) {
        MultipleLoanObject multipleLoanObject = new MultipleLoanObject();
        multipleLoanObject.setMMainLoanID(loanId);
        multipleLoanObject.setMLoanType(loanType);
        return multipleLoanRepository.getObject(multipleLoanObject);
    }

    /**
     * 元欧担保费信息
     */
    private AdditionalFeeInformation getYuanEuroguaranteeInFormation(Integer loanId,String expenseType){
        return additionalFeeInformationRepository.getAdditionalFeeInformationByLoanIdAndType(loanId,expenseType);
    }

    /**
     * 还款计划信息
     */
    private List<LoanRepayDetailObject> getLoanRepaymentObject(MultipleLoanObject multipleLoanObject){
        if (multipleLoanObject == null){
            return new ArrayList<>();
        }
        return loanRepayDetailRepository.getListOrderByTerm(multipleLoanObject.getM_loanid());
    }

    /**
     * 附加担保费还款计划
     */

    private List<AdditionalFeeRepayPlan> getAdditionalPremiumRepaymentObject(AdditionalFeeInformation additionalFeeInformation){
        if (additionalFeeInformation == null){
            return new ArrayList<>();
        }
        return additionalFeeRepayPlanRepository.getByAdditionalFeeId(additionalFeeInformation.getId());
    }

    /**
     * gps担保费还款计划
     */
    private List<AdditionalFeeRepayPlan> getGPSPremiumRepaymentObject(AdditionalFeeInformation gpsFeeInformation){
        if (gpsFeeInformation == null){
            return new ArrayList<>();
        }
        return additionalFeeRepayPlanRepository.getByAdditionalFeeId(gpsFeeInformation.getId());
    }
}
