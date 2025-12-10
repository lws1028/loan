package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.bj58.dto;

import lombok.Getter;

/**
 * 更具状态显示额外信息
 */
@Getter
public class CluesInformationOtherInfo {

    private final String productName;
    private final Integer amount;
    private final Integer term;
    private final String rate;
    private final Long loanTime;

    public static class Builder {
        private String productName;
        private Integer amount;
        private Integer term;
        private String rate;
        private Long loanTime;

        public Builder productName(String productName) {
            this.productName = productName;
            return this;
        }

        public Builder amount(Integer amount) {
            this.amount = amount;
            return this;
        }

        public Builder term(Integer term) {
            this.term = term;
            return this;
        }

        public Builder rate(String rate) {
            this.rate = rate;
            return this;
        }

        public Builder loanTime(Long loanTime) {
            this.loanTime = loanTime;
            return this;
        }

        public CluesInformationOtherInfo build() {
            return new CluesInformationOtherInfo(this);
        }
    }

    private CluesInformationOtherInfo(Builder builder) {
        this.productName = builder.productName;
        this.amount = builder.amount;
        this.term = builder.term;
        this.rate = builder.rate;
        this.loanTime = builder.loanTime;
    }

    private CluesInformationOtherInfo(String productName, Integer amount, Integer term, String rate, Long loanTime) {
        this.productName = productName;
        this.amount = amount;
        this.term = term;
        this.rate = rate;
        this.loanTime = loanTime;
    }
    public static  CluesInformationOtherInfo createNull() {
        return new CluesInformationOtherInfo(null, null, null, null, null);
    }
}
