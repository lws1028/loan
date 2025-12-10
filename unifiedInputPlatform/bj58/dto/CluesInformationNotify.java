package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.bj58.dto;

import lombok.Getter;

import java.sql.Timestamp;

/**
 * 线索回调参数
 */
@Getter
public class CluesInformationNotify {
    //订单编号
    private Long orderId;
    //线索状态
    private Integer status;
    //状态变更时间
    private Timestamp changeTime;
    //状态说明
    private String remark;
    //贷款产品名称
    private String productName;
    //放款金额
    private Integer amount;
    //借款期数
    private Integer term;
    //利率
    private String rate;
    //放款时间
    private Long loanTime;
    //客服姓名
    private String staffName;
    //客服工号
    private String staffNumber;

    private CluesInformationNotify( Long orderId, Integer status, Timestamp changeTime, String remark, String productName, Integer amount, Integer term, String rate, Long loanTime, String staffName, String staffNumber) {
        this.orderId = orderId;
        this.status = status;
        this.changeTime = changeTime;
        this.remark = remark;
        this.productName = productName;
        this.amount = amount;
        this.term = term;
        this.rate = rate;
        this.loanTime = loanTime;
        this.staffName = staffName;
        this.staffNumber = staffNumber;
    }

    public static CluesInformationNotify createNotify( Long orderId, Integer status, Timestamp changeTime, String remark, String productName, Integer amount, Integer term, String rate, Long loanTime, String staffName, String staffNumber) {
        return new CluesInformationNotify(orderId, status, changeTime, remark, productName, amount, term, rate, loanTime, staffName, staffNumber);
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
