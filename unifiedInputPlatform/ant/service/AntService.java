package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.ant.service;

import com.zlhj.commonLoan.business.common.exception.AreaInadmissibleException;
import com.zlhj.commonLoan.business.common.exception.ClueResubmissionException;
import com.zlhj.commonLoan.domain.cule.ClueNumber;
import com.zlhj.commonLoan.domain.cule.CuleApplyCommand;
import com.zlhj.commonLoan.domain.cule.CuleApplyResult;
import com.zlhj.electronicCredit.pojo.CreditLoan;
import com.zlhj.mq.dto.AntPreApproveMessage;
import com.zlhj.mq.dto.ClueShowPushMessage;
import com.zlhj.unifiedInputPlatform.ant.dto.*;
import com.zlhj.unifiedInputPlatform.smy.entity.EncryptionPhoneCheck;

import java.util.List;
import java.util.Optional;

public interface AntService {

    /**
     * 匹配SHA256手机号
     */
    Optional<EncryptionPhoneCheck> matchSha256PhoneNumber(String mobileSHA256);

    /**
     * 线索进件
     */
    CuleApplyResult culeApply(CuleApplyCommand command) throws ClueResubmissionException, AreaInadmissibleException;

    /**
     * 线索状态查询
     */
    ClueStatusNotifyDTO queryClue(AntQueryCluePreDTO applyNo);

    /**
     * 针对蚂蚁机构初审回传信息查询
     */
    ClueStatusNotifyDTO queryClueForOrgApprove(AntQueryCluePreDTO preDTO);

    void productNameStore(String decisionRules, CreditLoan creditLoan);

    void antNotSubmitSchedule();

    void handleSHA256();

    List<AntRepaymentPlanDTO> queryRepayPlan(ClueNumber clueNumber);

    /**
     * 还款计划变更
     */
    void repaymentPlanChangeNotify();

    /**
     * 蚂蚁03接口预审通知
     */
    void antPreApprove(AntPreApproveMessage message);

    /**
     * 蚂蚁法大大签约完成
     */
    void fddSignComplete(FddRASignVO fddRASignVO);

    /**
     * 蚂蚁机构初审异常重试
     */
    void antPreApproveRetry();

    ClueAdditionalResult queryIncomeProof(Integer loanId);

    ClueAdditionalResult queryIncomeProof(String applyNum);

    void incomeProofFinished(ClueShowPushMessage message);

    void fddSignCompleteRetry();
}
