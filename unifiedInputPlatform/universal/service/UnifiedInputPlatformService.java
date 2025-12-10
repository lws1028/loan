package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.universal.service;

import com.zlhj.commonLoan.business.clue.dto.LoanStatePushToClueDTO;
import com.zlhj.commonLoan.business.clue.enums.LoanStatusChangeEnum;
import com.zlhj.commonLoan.domain.cule.ClueNumber;
import com.zlhj.commonLoan.domain.mainLoan.LoanId;

public interface UnifiedInputPlatformService {

    LoanId getLoanIdByClueSystem(ClueNumber clueNumber, Integer splChannelPattern);

    int preApproveRealTimeInteraction(LoanId loanId);

    void preApproveRefuseRealTimeInteraction(LoanId loanId);

    void LendingApprovePassRealTimeInteraction(LoanId loanId);

    void channelStatusRePush(String param);

    void realTimeInteraction(LoanStatePushToClueDTO loanStatePushToClueDTO);

    void prePassPushTelemarketing(String boId, LoanStatusChangeEnum loanStatusChangeEnum);
}
