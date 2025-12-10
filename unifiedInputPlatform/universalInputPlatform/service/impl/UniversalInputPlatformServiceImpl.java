package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.universalInputPlatform.service.impl;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.zlhj.InterfaceMessage.dto.MainLeasing;
import com.zlhj.InterfaceMessage.dto.MainLeasingRepository;
import com.zlhj.common.exception.BizException;
import com.zlhj.commonLoan.business.clue.enums.LoanStatusChangeEnum;
import com.zlhj.domain.model.exception.SftpSendFailException;
import com.zlhj.jd.vo.DirectCustomerStatusDto;
import com.zlhj.main.fumin.enums.BankOrgNameType;
import com.zlhj.mapper.SplBussinessbasicMapper;
import com.zlhj.tianfu.Util.Tool;
import com.zlhj.unifiedInputPlatform.ant.dto.ClueStatusNotifyDTO;
import com.zlhj.unifiedInputPlatform.ant.dto.UnifiedInputPlatformBillReq;
import com.zlhj.unifiedInputPlatform.universalInputPlatform.dto.*;
import com.zlhj.unifiedInputPlatform.universalInputPlatform.enums.ChannelPartnerEnum;
import com.zlhj.unifiedInputPlatform.universalInputPlatform.service.DataAnalysisService;
import com.zlhj.unifiedInputPlatform.universalInputPlatform.service.UniversalInputPlatformService;
import com.zlhj.user.vo.ExhibitionAndPayInfoRepository;
import com.zlhj.user.vo.ExhibitionAndPayInfoVo;
import com.zlhj.user.vo.MultipleLoanObject;
import com.zlhj.user.vo.MultipleLoanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * @author : wangwenhao
 * @since : 2025/9/3 14:06
 */
@Service
public class UniversalInputPlatformServiceImpl implements UniversalInputPlatformService {
    private static final Logger log = LoggerFactory.getLogger(UniversalInputPlatformServiceImpl.class);
    private final SplBussinessbasicMapper splBussinessbasicMapper;
    private final MultipleLoanRepository multipleLoanRepository;
    private final ExhibitionAndPayInfoRepository exhibitionAndPayInfoRepository;
    private final MainLeasingRepository mainLeasingRepository;
    private final DataAnalysisService dataAnalysisService;

    public UniversalInputPlatformServiceImpl(SplBussinessbasicMapper splBussinessbasicMapper, MultipleLoanRepository multipleLoanRepository, ExhibitionAndPayInfoRepository exhibitionAndPayInfoRepository, MainLeasingRepository mainLeasingRepository, DataAnalysisService dataAnalysisService) {
        this.splBussinessbasicMapper = splBussinessbasicMapper;
        this.multipleLoanRepository = multipleLoanRepository;
        this.exhibitionAndPayInfoRepository = exhibitionAndPayInfoRepository;
        this.mainLeasingRepository = mainLeasingRepository;
        this.dataAnalysisService = dataAnalysisService;
    }

    @Override
    public ClueStatusNotifyDTO billStatus(UnifiedInputPlatformBillReq unifiedInputPlatformBillReq) {
        //获取当前节点
        LoanStatusChangeEnum realTimeStatusEnum = LoanStatusChangeEnum.getEnumsByValue(unifiedInputPlatformBillReq.getStatus());
        DirectCustomerStatusDto directCustomerStatusDto = null;
        if (ChannelPartnerEnum.YSF_XA.getCode().equals(unifiedInputPlatformBillReq.getSource())) {
            directCustomerStatusDto = splBussinessbasicMapper.selectStatusByOrderId(
                    unifiedInputPlatformBillReq.getApplyNo(),
                    Integer.valueOf(ChannelPartnerEnum.YSF_XA.getCode())
            );
            if (directCustomerStatusDto == null) {
                return ClueStatusNotifyDTO.builder().applyNo(unifiedInputPlatformBillReq.getApplyNo()).build();
            }
            realTimeStatusEnum = LoanStatusChangeEnum.getEnumsByValue(directCustomerStatusDto.getSplMaxActionNum());
        }


        //当前实时状态为空时默认返回
        if (directCustomerStatusDto == null || realTimeStatusEnum == null) {
            return ClueStatusNotifyDTO.builder().applyNo(unifiedInputPlatformBillReq.getApplyNo()).build();
        }

        //放款成功
        if (LoanStatusChangeEnum.LEND_SUC == realTimeStatusEnum) {
            Integer loanId = directCustomerStatusDto.getSplLoanId();
            //是否需要租赁放款  是 -> exhibitionAndPay->payAmount 否 -> loanInfo->applyMoney
            ExhibitionAndPayInfoVo exhibitionAndPay = exhibitionAndPayInfoRepository.getObject(loanId);
            MultipleLoanObject loanInfo = multipleLoanRepository.getLoanIdByMainLoanId(loanId);
            MainLeasing mainLeasing = mainLeasingRepository.getDataByLoanId(loanId);
            boolean isLeasingLending = Tool.searchIsleasinglending(loanId).equals("1");
            BigDecimal applyMoney = loanInfo.getM_applyMoney() == null ? null : BigDecimal.valueOf(loanInfo.getM_applyMoney());
            if (isLeasingLending) {
                String loanDateTime = mainLeasing.getGrantMoneyDate() == null ? null : LocalDate.parse(mainLeasing.getGrantMoneyDate()).atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                return ClueStatusNotifyDTO.builder().applyNo(unifiedInputPlatformBillReq.getApplyNo())
                        .status(directCustomerStatusDto.getSplMaxActionNum())
                        .creditAmt(applyMoney)
                        .orgDrawdownNo(loanInfo.getM_loanNumber())
                        .loanAmt(exhibitionAndPay.getM_payAmount())
                        .loanRate(loanInfo.getM_commonRate() == null ? BigDecimal.ZERO : BigDecimal.valueOf(loanInfo.getM_commonRate()))
                        .loanDate(loanDateTime)
                        .loanTerm(loanInfo.getM_term())
                        .loanTermUnit("M")
                        .repayType("2")
                        .build();
            } else {
                String loanDateTime = loanInfo.getM_grantMoneyDate() == null ? null : LocalDate.parse(loanInfo.getM_grantMoneyDate()).atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                return ClueStatusNotifyDTO.builder().applyNo(unifiedInputPlatformBillReq.getApplyNo())
                        .status(directCustomerStatusDto.getSplMaxActionNum())
                        .creditAmt(applyMoney)
                        .orgDrawdownNo(loanInfo.getM_loanNumber())
                        .loanAmt(applyMoney)
                        .loanRate(loanInfo.getM_commonRate() == null ? BigDecimal.ZERO : BigDecimal.valueOf(loanInfo.getM_commonRate()))
                        .loanDate(loanDateTime)
                        .loanTerm(loanInfo.getM_term())
                        .loanTermUnit("M")
                        .repayType("2")
                        .build();
            }
        }
        return ClueStatusNotifyDTO.builder().applyNo(unifiedInputPlatformBillReq.getApplyNo()).status(directCustomerStatusDto.getSplMaxActionNum()).build();
    }

    @Override
    public void dataAnalysis(UniversalInputPlatformDataAnalysisReq dataAnalysisReq) throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException, SftpSendFailException {
        //获取资方枚举
        BankOrgNameType bank = Optional.ofNullable(BankOrgNameType.get(dataAnalysisReq.getSelectCode())).orElseThrow(() -> new BizException("资方不存在"));

        //数据处理
        if (bank.equals(BankOrgNameType.XA_BANK)) {
            //生成数据
            GeneralLedger generalLedger = dataAnalysisService.createGeneralLedger(bank);
            IntradayGeneralLedger intradayGeneralLedger = dataAnalysisService.createIntradayGeneralLedger(bank);
            List<IntradayCredit> intradayCredit = dataAnalysisService.createIntradayCredit(bank);
            List<IntradayDisbursement> intradayDisbursement = dataAnalysisService.createIntradayDisbursement(bank);
            List<IntradayPayment> intradayPayment = dataAnalysisService.createIntradayPayment(bank);
            List<IntradayOverdue> intradayOverdue = dataAnalysisService.createIntradayOverdue(bank);
            log.info("新安云闪付数据生成完成");
            dataAnalysisService.processXADataAnalysis(
                    generalLedger,
                    intradayGeneralLedger,
                    intradayCredit,
                    intradayDisbursement,
                    intradayPayment,
                    intradayOverdue
            );
        }
    }
}
