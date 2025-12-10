package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.jd.pojo;

import com.zlhj.Interface.batchVo.LoanRepayDetailObject;
import com.zlhj.commonLoan.interfaces.ksyh.pojo.AdditionalFeeRepayPlan;
import com.zlhj.loan.entity.Sapdcslas;
import com.zlhj.user.vo.MultipleLoanObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClueQueryBillBusiness {
    // 汇总贷款ID
    private Integer loanId;
    // 贷款状态
    private Sapdcslas sapdcslas;
    private MultipleLoanObject multipleLoanObject;
    // 还款计划表
    private List<LoanRepayDetailObject> loanRepayDetailObjects;
    // gps担保费还款信息
    private List<AdditionalFeeRepayPlan> gpsFeeRepayPlans;
    // 附加担保费还款信息
    private List<AdditionalFeeRepayPlan> additionalFeeRepayPlans;

    public boolean judgeLoanStatus() {
        if(this.sapdcslas == null){
            return false;
        }
        //只拿节点在245的贷款数据
        return sapdcslas.getSdlasStatus() >= 245;
    }
}
