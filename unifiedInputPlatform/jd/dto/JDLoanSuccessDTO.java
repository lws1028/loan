package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.jd.dto;

import lombok.Data;

@Data
public class JDLoanSuccessDTO {
    /**
     * 线索编号
     */
    private String boId;
    /**
     * 汇总贷款id
     */
    private Integer mainLoanId;
    /**
     * 主贷款id
     */
    private Integer loanId;
}