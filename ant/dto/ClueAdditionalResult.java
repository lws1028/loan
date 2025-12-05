package com.zlhj.unifiedInputPlatform.ant.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class ClueAdditionalResult {
    private String boId;
    private List<String> months;
    private List<Map<String, String>> flowInfoData;
    private Map<String, String> sumPayment;
    private String pdfFiles;
    private Map<String, String> thirdSmallestMonthData;
    private BigDecimal additionalAmountRequired;
}