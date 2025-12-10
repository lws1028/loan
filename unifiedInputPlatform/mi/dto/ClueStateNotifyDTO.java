package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.mi.dto;

import lombok.ToString;

@ToString
 public class ClueStateNotifyDTO {
    private Long applyId;  //申请流⽔号
    private String openId;  //⽤⼾唯⼀标识

    private Long changeTime; //unix时间戳

    private int clueType;//线索类型：1⻋抵贷

    /**
     * 线索订单状态:
     * 1.处理中：线索订单已成功提交合作⽅，合作⽅尚未反馈处理进
     * 度
     * 2.已取消：合作⽅客服联系不上⽤⼾或⽆需求
     * 3.审批通过:资⽅审批通过并放款（结算节点）
     * 4.审批拒绝:合作⽅⽆意向
     */
    private int orderStatus;

    /**
     *  如果是已取消或审批拒绝，请写明原因
     */
    private String reason;

    /**
     * 合同编号（审批通过必填）
     */
    private String contractNumber;

    /**
     * 订单借款⾦额，以分为单位，⼀万写作1000000（审批通过必填）
     */
    private Long orderAmount;
    private int term;//实际借期（审批通过必填）
    private Long rate;//利率,百万分之⼀，⽰例：万3请填写300（审批通过必填）
    private int rateType;//利率类型:1.⽇利率；2.⽉利率；3.年利率（审批通过必填）
    private Long fee;//服务费，以分为单位

    public void setApplyId(Long applyId) {
        this.applyId = applyId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public void setChangeTime(Long changeTime) {
        this.changeTime = changeTime;
    }

    public void setClueType(int clueType) {
        this.clueType = clueType;
    }

    public void setOrderStatus(int orderStatus) {
        this.orderStatus = orderStatus;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }

    public void setOrderAmount(Long orderAmount) {
        this.orderAmount = orderAmount;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public void setRate(Long rate) {
        this.rate = rate;
    }

    public void setRateType(int rateType) {
        this.rateType = rateType;
    }

    public void setFee(Long fee) {
        this.fee = fee;
    }

    public Long getApplyId() {
        return applyId;
    }

    public String getOpenId() {
        return openId;
    }

    public Long getChangeTime() {
        return changeTime;
    }

    public int getClueType() {
        return clueType;
    }

    public int getOrderStatus() {
        return orderStatus;
    }

    public String getReason() {
        return reason;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public Long getOrderAmount() {
        return orderAmount;
    }

    public int getTerm() {
        return term;
    }

    public Long getRate() {
        return rate;
    }

    public int getRateType() {
        return rateType;
    }

    public Long getFee() {
        return fee;
    }
}
