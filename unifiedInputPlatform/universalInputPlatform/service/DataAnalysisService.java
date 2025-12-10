package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.universalInputPlatform.service;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.zlhj.domain.model.exception.SftpSendFailException;
import com.zlhj.main.fumin.enums.BankOrgNameType;
import com.zlhj.unifiedInputPlatform.universalInputPlatform.dto.*;

import java.io.IOException;
import java.util.List;

/**
 * 数据分析接口
 */
public interface DataAnalysisService {
    GeneralLedger createGeneralLedger(BankOrgNameType bank);

    IntradayGeneralLedger createIntradayGeneralLedger(BankOrgNameType bank);

    List<IntradayCredit> createIntradayCredit(BankOrgNameType bank);

    List<IntradayDisbursement> createIntradayDisbursement(BankOrgNameType bank);

    List<IntradayPayment> createIntradayPayment(BankOrgNameType bank);

    List<IntradayOverdue> createIntradayOverdue(BankOrgNameType bank);

    void processXADataAnalysis(GeneralLedger generalLedger, IntradayGeneralLedger intradayGeneralLedger, List<IntradayCredit> intradayCredit, List<IntradayDisbursement> intradayDisbursement, List<IntradayPayment> intradayPayment, List<IntradayOverdue> intradayOverdue) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, SftpSendFailException;
}
