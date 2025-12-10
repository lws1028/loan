package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.universalInputPlatform.service;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.zlhj.domain.model.exception.SftpSendFailException;
import com.zlhj.unifiedInputPlatform.ant.dto.ClueStatusNotifyDTO;
import com.zlhj.unifiedInputPlatform.ant.dto.UnifiedInputPlatformBillReq;
import com.zlhj.unifiedInputPlatform.universalInputPlatform.dto.UniversalInputPlatformDataAnalysisReq;

import java.io.IOException;

/**
 * @author : wangwenhao
 * @since : 2025/9/3 11:56
 */
public interface UniversalInputPlatformService {
    ClueStatusNotifyDTO billStatus(UnifiedInputPlatformBillReq unifiedInputPlatformBillReq);

    void dataAnalysis(UniversalInputPlatformDataAnalysisReq dataAnalysisReq) throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException, SftpSendFailException;
}
