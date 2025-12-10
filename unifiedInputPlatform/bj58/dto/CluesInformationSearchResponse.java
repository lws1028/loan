package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.bj58.dto;


import lombok.Getter;

/**
 * 线索查询反馈结果
 */
@Getter
public class CluesInformationSearchResponse {

    //线索状态
    private final Integer status;

    //状态变更时间
    private final Long changeTime;

    //状态说明
    private final String remark;

    //贷款产品名称
    private final String productName;

    //放款金额
    private final String amount;

    //借款期数
    private final String term;

    //利率 例：“18.00%”
    private final String rate;

    //放款时间
    private final String loanTime;

    //客服姓名
    private final String staffName;

    //客服工号
    private final String staffNumber;

    private CluesInformationSearchResponse(Builder builder) {
        this.status = builder.status;
        this.changeTime = builder.changeTime;
        this.remark = builder.remark;
        this.productName = builder.productName;
        this.amount = builder.amount;
        this.term = builder.term;
        this.rate = builder.rate;
        this.loanTime = builder.loanTime;
        this.staffName = builder.staffName;
        this.staffNumber = builder.staffNumber;
    }

    public static class Builder {
        private Integer status;
        private Long changeTime;
        private String remark;
        private String productName;
        private String amount;
        private String term;
        private String rate;
        private String loanTime;
        private String staffName;
        private String staffNumber;

        public Builder status(Integer status) {
            this.status = status;
            return this;
        }

        public Builder changeTime(Long changeTime) {
            this.changeTime = changeTime;
            return this;
        }

        public Builder remark(String remark) {
            this.remark = remark;
            return this;
        }

        public Builder productName(String productName) {
            this.productName = productName;
            return this;
        }

        public Builder amount(String amount) {
            this.amount = amount;
            return this;
        }

        public Builder rate(String rate) {
            this.rate = rate;
            return this;
        }

        public Builder term(String term) {
            this.term = term;
            return this;
        }

        public Builder loanTime(String loanTime) {
            this.loanTime = loanTime;
            return this;
        }

        public Builder staffName(String staffName) {
            this.staffName = staffName;
            return this;
        }

        public Builder staffNumber(String staffNumber) {
            this.staffNumber = staffNumber;
            return this;
        }

        public CluesInformationSearchResponse build() {
            return new CluesInformationSearchResponse(this);
        }
    }



}
