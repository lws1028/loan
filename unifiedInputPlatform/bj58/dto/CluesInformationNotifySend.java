package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.bj58.dto;

import lombok.Getter;

@Getter
public class CluesInformationNotifySend {

    private Long orderId;

    private Integer loanId;

    private Integer status;

    //状态 (0未受理  1接受近件  2拒绝近件  3待业务申请 9系统取消）
    private Integer loanState;

    private Integer branchLoanId;

    private CluesInformationNotify notify;

    private String errorMsg;

    private CluesInformationNotifySend(Long orderId, Integer loanId, CluesInformationNotify notify,Integer status,Integer loanState,Integer branchLoanId, String errorMsg) {
        this.orderId = orderId;
        this.loanId = loanId;
        this.notify = notify;
        this.status = status;
        this.loanState = loanState;
        this.branchLoanId = branchLoanId;
        this.errorMsg = errorMsg;
    }

    public static CluesInformationNotifySend createSend(Long orderId,Integer loanId, CluesInformationNotify notify,Integer status,Integer loanState,Integer branchLoanId, String errorMsg) {
        return new CluesInformationNotifySend(orderId,loanId, notify, status,loanState,branchLoanId, errorMsg);
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
