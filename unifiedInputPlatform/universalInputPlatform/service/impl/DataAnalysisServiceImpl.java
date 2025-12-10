package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.universalInputPlatform.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.zlhj.Interface.batchVo.LoanRepayDetailObject;
import com.zlhj.Interface.batchVo.LoanRepaymentObject;
import com.zlhj.common.core.domain.R;
import com.zlhj.commonLoan.business.jjyh.pojo.enums.Constants;
import com.zlhj.domain.model.exception.SftpSendFailException;
import com.zlhj.domain.model.ftp.BankUploadImageToSftp;
import com.zlhj.infrastructure.persistence.*;
import com.zlhj.infrastructure.po.DeductionRecord;
import com.zlhj.infrastructure.repository.DeductionRecordRepository;
import com.zlhj.infrastructure.routing.RemoteCluePlatformService;
import com.zlhj.loan.service.BankApprovalRecordService;
import com.zlhj.loan.vo.BankApprovalRecordPo;
import com.zlhj.main.fumin.enums.BankOrgNameType;
import com.zlhj.unifiedInputPlatform.ant.dto.ClueDTO;
import com.zlhj.unifiedInputPlatform.universalInputPlatform.dto.*;
import com.zlhj.unifiedInputPlatform.universalInputPlatform.service.DataAnalysisService;
import com.zlhj.user.vo.LoanProductVo;
import com.zlhj.user.vo.MultipleLoanObject;
import com.zlhj.user.vo.SplBussinessBasicRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DataAnalysisServiceImpl implements DataAnalysisService {
    private final SapDcsLoanAppStatusRepositoryMybatis sapDcsLoanAppStatusRepository;
    private final LoanRepayDetailRepositoryMybatis loanRepayDetailRepository;
    private final LoanRepaymentRepositoryMybatis loanRepaymentRepository;
    private final MultipleLoanRepositoryMybatis multipleLoanRepository;
    private final BankApprovalRecordService bankApprovalRecordService;
    private final LoanProductRepositoryMybatis loanProductRepository;
    private final DeductionRecordRepository deductionRecordRepository;
    private final RemoteCluePlatformService remoteCluePlatformService;
    private final SplBussinessBasicRepository splBussinessBasicRepository;
    private final BankExpandInfoVoRepositoryMybatis bankExpandInfoVoRepositoryMybatis;
    private final static List<String[]> EMPTYLIST = Collections.emptyList();

    @Value("${zlhj.unionPay.partnerId}")
    private String unionPayPartnerId;
    @Value("${zlhj.xayh.sftp.reconciliationFilePath}")
    private String unionPayBIPath;

    @Value("${zlhj.xayh.sftp.host}")
    private String ftpHost;
    @Value("${zlhj.xayh.sftp.port}")
    private int ftpPort;
    @Value("${zlhj.xayh.sftp.username}")
    private String ftpUserName;
    @Value("${zlhj.xayh.sftp.password}")
    private String ftpPassword;

    @Value("${zlhj.files.store.path}")
    private String fileStorePath;

    public DataAnalysisServiceImpl(SapDcsLoanAppStatusRepositoryMybatis sapDcsLoanAppStatusRepository, LoanRepayDetailRepositoryMybatis loanRepayDetailRepository, LoanRepaymentRepositoryMybatis loanRepaymentRepository, MultipleLoanRepositoryMybatis multipleLoanRepository, BankApprovalRecordService bankApprovalRecordService, LoanProductRepositoryMybatis loanProductRepository, DeductionRecordRepository deductionRecordRepository, RemoteCluePlatformService remoteCluePlatformService, SplBussinessBasicRepository splBussinessBasicRepository, BankExpandInfoVoRepositoryMybatis bankExpandInfoVoRepositoryMybatis) {
        this.sapDcsLoanAppStatusRepository = sapDcsLoanAppStatusRepository;
        this.loanRepayDetailRepository = loanRepayDetailRepository;
        this.loanRepaymentRepository = loanRepaymentRepository;
        this.multipleLoanRepository = multipleLoanRepository;
        this.bankApprovalRecordService = bankApprovalRecordService;
        this.loanProductRepository = loanProductRepository;
        this.deductionRecordRepository = deductionRecordRepository;
        this.remoteCluePlatformService = remoteCluePlatformService;
        this.splBussinessBasicRepository = splBussinessBasicRepository;
        this.bankExpandInfoVoRepositoryMybatis = bankExpandInfoVoRepositoryMybatis;
    }

    @Override
    public GeneralLedger createGeneralLedger(BankOrgNameType bank) {
        if (bank.equals(BankOrgNameType.XA_BANK)) {
            //已放款后阶段的贷款ID
            List<Integer> loanIds = sapDcsLoanAppStatusRepository.selectPostDisbursementLoanIdsByBankCode(bank.getKey(), 245, null)
                    .stream().filter(Objects::nonNull).collect(Collectors.toList());
            List<Integer> disbursementErrorLoanIds = sapDcsLoanAppStatusRepository.selectDisbursementErrorLoanIdsByBankCode(bank.getKey());
            if (loanIds.isEmpty()) {
                return null;
            }
            List<List<Integer>> splitLoanIds = CollUtil.split(loanIds, 500);
            List<BigDecimal> disbursementTotalList = new ArrayList<>();
            List<BigDecimal> principalSumList = new ArrayList<>();
            List<BigDecimal> interestSumList = new ArrayList<>();
            List<BigDecimal> penaltySumList = new ArrayList<>();
            List<BigDecimal> compoundSumList = new ArrayList<>();
            List<BigDecimal> totalPaidList = new ArrayList<>();
            long paidCountTotal = 0L;
            for (List<Integer> splitLoanId : splitLoanIds) {
                Map<Integer, Integer> loanIdMap = multipleLoanRepository.list(Wrappers.lambdaQuery(MultipleLoanObject.class)
                                .select(MultipleLoanObject::getM_mainLoanID, MultipleLoanObject::getM_loanid)
                                .in(MultipleLoanObject::getM_mainLoanID, splitLoanId)
                                .eq(MultipleLoanObject::getM_loanType, 2))
                        .stream().collect(Collectors.toMap(MultipleLoanObject::getM_mainLoanID, MultipleLoanObject::getM_loanid));
                List<LoanRepayDetailObject> loanRepayDetailList = loanRepayDetailRepository.searchDisbursementRepaymentDetails(
                        loanIdMap.values());
                BigDecimal totalDisbursement = multipleLoanRepository.list(Wrappers.lambdaQuery(MultipleLoanObject.class)
                                .select(MultipleLoanObject::getM_applyMoney)
                                .in(MultipleLoanObject::getM_loanid, splitLoanId))
                        .stream().map(MultipleLoanObject::getM_applyMoney).map(NumberUtil::nullToZero)
                        .map(BigDecimal::valueOf)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                disbursementTotalList.add(totalDisbursement);
                //本金
                BigDecimal principalSum = loanRepayDetailList.stream().map(LoanRepayDetailObject::getM_capital)
                        .collect(Collectors.toList()).stream().map(NumberUtil::nullToZero).reduce(BigDecimal.ZERO, BigDecimal::add);
                principalSumList.add(principalSum);
                //利息
                BigDecimal interestSum = loanRepayDetailList.stream().map(LoanRepayDetailObject::getM_interest)
                        .collect(Collectors.toList()).stream().map(NumberUtil::nullToZero).reduce(BigDecimal.ZERO, BigDecimal::add);
                interestSumList.add(interestSum);
                //罚息
                BigDecimal penaltySum = loanRepayDetailList.stream().map(LoanRepayDetailObject::getM_penalty)
                        .collect(Collectors.toList()).stream().map(NumberUtil::nullToZero).reduce(BigDecimal.ZERO, BigDecimal::add);
                penaltySumList.add(penaltySum);
                //复利
                BigDecimal compoundSum = loanRepayDetailList.stream().map(LoanRepayDetailObject::getM_compound)
                        .collect(Collectors.toList()).stream().map(NumberUtil::nullToZero).reduce(BigDecimal.ZERO, BigDecimal::add);
                compoundSumList.add(compoundSum);
                //总还款金额
                BigDecimal totalPaid = principalSum.add(interestSum).add(penaltySum).add(compoundSum);
                totalPaidList.add(totalPaid);
                //总还款笔数
                long paidCount = loanRepayDetailList.stream().filter(i -> NumberUtil.nullToZero(i.getM_capital()).compareTo(BigDecimal.ZERO) > 0).map(LoanRepayDetailObject::getM_loanID).distinct().count();
                paidCountTotal += paidCount;
            }
            //本金总额
            BigDecimal totalPrincipal = principalSumList.stream().reduce(BigDecimal.ZERO,BigDecimal::add);
            //利息总额
            BigDecimal totalInterest = interestSumList.stream().reduce(BigDecimal.ZERO,BigDecimal::add);
            //罚息总额
            BigDecimal totalPenalty = penaltySumList.stream().reduce(BigDecimal.ZERO,BigDecimal::add);
            //复利总额
            BigDecimal totalCompound = compoundSumList.stream().reduce(BigDecimal.ZERO,BigDecimal::add);
            //总还款
            BigDecimal totalPaid = totalPaidList.stream().reduce(BigDecimal.ZERO,BigDecimal::add);
            //总放款金额
            BigDecimal totalDisbursement = disbursementTotalList.stream().reduce(BigDecimal.ZERO,BigDecimal::add);
            //总放款笔数
            long disbursementTotal = loanIds.size() + disbursementErrorLoanIds.size();
            //贷款余额
            BigDecimal remainingTotal = totalDisbursement.subtract(totalPrincipal);
            //总授信申请人数
            long creditApplyCount = sapDcsLoanAppStatusRepository.selectCreditApplyCount(bank.getKey(), null, Timestamp.valueOf(LocalDateTime.now()));
            //总授信成功人数
            long creditApplySuccessCount = sapDcsLoanAppStatusRepository.selectCreditApplySuccessCount(bank.getKey(), null, Timestamp.valueOf(LocalDateTime.now()));
            //总放款成功笔数
            int loanSuccessCount = loanIds.size();
            return XAGeneralLedger.builder()
                    //合作伙伴ID
                    .partnerId(unionPayPartnerId)
                    //对账日期
                    .batchDate(LocalDate.now().minusDays(1).toString())
                    .remainLoanAmt(remainingTotal)
                    .loanSum(disbursementTotal)
                    .loanAmt(totalDisbursement)
                    .repaySum(paidCountTotal)
                    .repayAmt(totalPaid)
                    .repayPrin(totalPrincipal)
                    .repayInt(totalInterest)
                    .repayPenelty(totalPenalty)
                    .repayCompound(totalCompound)
                    //总还保费
                    .repayPremium(BigDecimal.ZERO)
                    //总还手续费
                    .repayFee(BigDecimal.ZERO)
                    .applyCnt(creditApplyCount)
                    .applySuccessCnt(creditApplySuccessCount)
                    //总放款申请笔数
                    .putoutCnt(disbursementTotal)
                    .putoutSuccessCnt(loanSuccessCount)
                    .build();
        }
        return null;
    }

    @Override
    public IntradayGeneralLedger createIntradayGeneralLedger(BankOrgNameType bank) {
        if (bank.equals(BankOrgNameType.XA_BANK)) {
            //已放款后阶段的贷款ID
            List<Integer> loanIds = sapDcsLoanAppStatusRepository.selectPostDisbursementLoanIdsByBankCode(bank.getKey(), 245, LocalDate.now().minusDays(1).toString());
            if (loanIds.isEmpty()) {
                return null;
            }
            List<List<Integer>> splitLoanIds = CollUtil.split(loanIds, 500);
            List<BigDecimal> disbursementTotalList = new ArrayList<>();
            List<BigDecimal> principalSumList = new ArrayList<>();
            List<BigDecimal> interestSumList = new ArrayList<>();
            List<BigDecimal> penaltySumList = new ArrayList<>();
            List<BigDecimal> compoundSumList = new ArrayList<>();
            List<BigDecimal> totalPaidList = new ArrayList<>();
            long paidCountTotal = 0L;
            for (List<Integer> splitLoanId : splitLoanIds) {
                Map<Integer, Integer> loanIdMap = multipleLoanRepository.list(Wrappers.lambdaQuery(MultipleLoanObject.class)
                                .select(MultipleLoanObject::getM_mainLoanID, MultipleLoanObject::getM_loanid)
                                .in(MultipleLoanObject::getM_mainLoanID, splitLoanId)
                                .eq(MultipleLoanObject::getM_loanType, 2))
                        .stream().collect(Collectors.toMap(MultipleLoanObject::getM_mainLoanID, MultipleLoanObject::getM_loanid));
                BigDecimal totalDisbursement = multipleLoanRepository.list(Wrappers.lambdaQuery(MultipleLoanObject.class)
                                .select(MultipleLoanObject::getM_applyMoney)
                                .in(MultipleLoanObject::getM_loanid, splitLoanId))
                        .stream().map(MultipleLoanObject::getM_applyMoney).map(NumberUtil::nullToZero)
                        .map(BigDecimal::valueOf)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                disbursementTotalList.add(totalDisbursement);
                List<LoanRepayDetailObject> loanRepayDetailList = loanRepayDetailRepository.searchIntradayDisbursementRepaymentDetails(
                        loanIdMap.values(),
                        LocalDate.now().minusDays(1).toString());
                //本金
                BigDecimal principalSum = loanRepayDetailList.stream().map(LoanRepayDetailObject::getM_capital)
                        .collect(Collectors.toList()).stream().map(NumberUtil::nullToZero).reduce(BigDecimal.ZERO, BigDecimal::add);
                principalSumList.add(principalSum);
                //利息
                BigDecimal interestSum = loanRepayDetailList.stream().map(LoanRepayDetailObject::getM_interest)
                        .collect(Collectors.toList()).stream().map(NumberUtil::nullToZero).reduce(BigDecimal.ZERO, BigDecimal::add);
                interestSumList.add(interestSum);
                //罚息
                BigDecimal penaltySum = loanRepayDetailList.stream().map(LoanRepayDetailObject::getM_penalty)
                        .collect(Collectors.toList()).stream().map(NumberUtil::nullToZero).reduce(BigDecimal.ZERO, BigDecimal::add);
                penaltySumList.add(penaltySum);
                //复利
                BigDecimal compoundSum = loanRepayDetailList.stream().map(LoanRepayDetailObject::getM_compound)
                        .collect(Collectors.toList()).stream().map(NumberUtil::nullToZero).reduce(BigDecimal.ZERO, BigDecimal::add);
                compoundSumList.add(compoundSum);
                //总还款金额
                BigDecimal totalPaid = principalSum.add(interestSum).add(penaltySum).add(compoundSum);
                totalPaidList.add(totalPaid);
                //总还款笔数
                long paidCount = loanRepayDetailList.stream().filter(i -> NumberUtil.nullToZero(i.getM_capital()).compareTo(BigDecimal.ZERO) > 0).map(LoanRepayDetailObject::getM_loanID).distinct().count();
                paidCountTotal += paidCount;
            }
            //本金总额
            BigDecimal totalPrincipal = principalSumList.stream().reduce(BigDecimal.ZERO,BigDecimal::add);
            //利息总额
            BigDecimal totalInterest = interestSumList.stream().reduce(BigDecimal.ZERO,BigDecimal::add);
            //罚息总额
            BigDecimal totalPenalty = penaltySumList.stream().reduce(BigDecimal.ZERO,BigDecimal::add);
            //复利总额
            BigDecimal totalCompound = compoundSumList.stream().reduce(BigDecimal.ZERO,BigDecimal::add);
            //总还款
            BigDecimal totalPaid = totalPaidList.stream().reduce(BigDecimal.ZERO,BigDecimal::add);
            //总放款金额
            BigDecimal totalDisbursement = disbursementTotalList.stream().reduce(BigDecimal.ZERO,BigDecimal::add);
            List<Integer> disbursementErrorLoanIds = sapDcsLoanAppStatusRepository.selectDisbursementErrorLoanIdsByBankCode(bank.getKey());
            //总放款笔数
            long disbursementTotalCount = loanIds.size() + disbursementErrorLoanIds.size();
            //总授信申请人数
            long creditApplyCount = sapDcsLoanAppStatusRepository.selectCreditApplyCount(bank.getKey(), null, Timestamp.valueOf(LocalDateTime.now()));
            //总授信成功人数
            long creditApplySuccessCount = sapDcsLoanAppStatusRepository.selectCreditApplySuccessCount(bank.getKey(), null, Timestamp.valueOf(LocalDateTime.now()));
            //总放款成功笔数
            int loanSuccessCount = loanIds.size();
            return XAIntradayGeneralLedger.builder()
                    .partnerId(unionPayPartnerId)
                    .batchDate(LocalDate.now().minusDays(1).toString())
                    .dayLoanSum(disbursementTotalCount)
                    .dayLoanAmt(totalDisbursement)
                    .dayRepaySum(paidCountTotal)
                    .dayRepayAmt(totalPaid)
                    .dayRepayPrin(totalPrincipal)
                    .dayRepayInt(totalInterest)
                    .dayRepayPenalty(totalPenalty)
                    .dayRepayCompound(totalCompound)
                    .dayPremium(BigDecimal.ZERO)
                    .dayRepayFee(BigDecimal.ZERO)
                    .applyCnt(creditApplyCount)
                    .applySuccessCnt(creditApplySuccessCount)
                    .putoutCnt(disbursementTotalCount)
                    .putoutSuccessCnt(loanSuccessCount)
                    .build();
        }
        return null;
    }

    @Override
    public List<IntradayCredit> createIntradayCredit(BankOrgNameType bank) {
        if (bank.equals(BankOrgNameType.XA_BANK)) {
            List<Integer> loanIds = sapDcsLoanAppStatusRepository.selectByBankCodeAndCommitDate(bank.getKey(), LocalDate.now().minusDays(1).toString());
            if (loanIds.isEmpty()) {
                return Collections.emptyList();
            }
            //执行利率
            Map<Integer, MultipleLoanObject> multipleLoanMap = new ConcurrentHashMap<>();
            for (List<Integer> splitLoanIds : CollUtil.split(loanIds, 500)) {
                Map<Integer, MultipleLoanObject> loanMap = multipleLoanRepository.list(Wrappers.lambdaQuery(MultipleLoanObject.class)
                                .in(MultipleLoanObject::getM_loanid, splitLoanIds))
                        .stream().collect(Collectors.toMap(MultipleLoanObject::getM_loanid, Function.identity(), (k1, k2) -> k1));
                multipleLoanMap.putAll(loanMap);
            }
            List<Integer> productIds = multipleLoanMap.values().stream().map(MultipleLoanObject::getM_productID).collect(Collectors.toList());
            Map<Integer, String> productMap = loanProductRepository.list(Wrappers.lambdaQuery(LoanProductVo.class)
                    .in(LoanProductVo::getProductId, productIds)).stream().collect(Collectors.toMap(LoanProductVo::getProductId, LoanProductVo::getProductCode, (k1, k2) -> k1));
            //授信日期
            List<BankApprovalRecordPo> bankApprovalRecordList = bankApprovalRecordService.listByLoanIds(loanIds);
            List<XAIntradayCredit> intradayCreditList = loanIds.stream().map(loanId -> {
                MultipleLoanObject loaninfo = MapUtil.getQuietly(multipleLoanMap, loanId, MultipleLoanObject.class, new MultipleLoanObject());
                BankApprovalRecordPo bankApprovalRecord = bankApprovalRecordList.stream().filter(i -> Objects.equals(i.getLoanId(), loanId))
                        .max(Comparator.comparing(BankApprovalRecordPo::getCreateTime))
                        .orElse(new BankApprovalRecordPo());
                String loanProduct = MapUtil.getQuietly(productMap, loaninfo.getM_productID(), String.class, "");
                String creditDate = bankApprovalRecord.getCreateTime() == null ? "" : DateUtil.parse(bankApprovalRecord.getCreateTime(), "yyyy-MM-dd HH:mm:ss").toLocalDateTime().toLocalDate().toString();
                String applyCode = bankApprovalRecord.getApplyCode();
                if ("0".equals(applyCode)) {
                    applyCode = "A";
                } else if ("1".equals(applyCode)) {
                    applyCode = "F";
                } else {
                    applyCode = "";
                }
                String boId = splBussinessBasicRepository.getBoIdByLoanId(loanId);
                String custId = Optional.ofNullable(remoteCluePlatformService.queryClue(boId))
                        .map(R::getData).map(ClueDTO::getBorrower).map(ClueDTO.Borrower::getUserId).orElse("");
                return XAIntradayCredit.builder()
                        .partnerId(unionPayPartnerId)
                        .batchDate(LocalDate.now().minusDays(1).toString())
                        .productNo(StrUtil.nullToEmpty(loanProduct))
                        .custId(custId)
                        .creditLimit(Objects.equals(applyCode, "F") ? BigDecimal.ZERO : NumberUtil.toBigDecimal(loaninfo.getM_applyMoney()))
                        .intRate(Objects.equals(applyCode, "F") ? BigDecimal.ZERO : NumberUtil.toBigDecimal(loaninfo.getM_commonRate()))
                        .creditDate(creditDate)
                        .limitStatus(applyCode)
                        .build();
            }).collect(Collectors.toList());
            return intradayCreditList.stream().filter(i -> StrUtil.isNotBlank(i.getLimitStatus())).collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public List<IntradayDisbursement> createIntradayDisbursement(BankOrgNameType bank) {
        if (bank.equals(BankOrgNameType.XA_BANK)) {
            Map<Integer, MultipleLoanObject> multipleLoanMap = multipleLoanRepository.list(Wrappers.lambdaQuery(MultipleLoanObject.class)
                            .eq(MultipleLoanObject::getM_grantMoneyDate, LocalDate.now().minusDays(1).toString())
                            .eq(MultipleLoanObject::getM_selectBank, BankOrgNameType.XA_BANK.getKey())
                            .eq(MultipleLoanObject::getM_loanType, "2"))
                    .stream().collect(Collectors.toMap(MultipleLoanObject::getM_loanid, Function.identity(), (k1, k2) -> k1));
            Set<Integer> mainLoanIds = multipleLoanMap.keySet();
            if (mainLoanIds.isEmpty()) {
                return Collections.emptyList();
            }
            Map<Integer, Integer> loanIdMap = multipleLoanRepository.list(Wrappers.lambdaQuery(MultipleLoanObject.class)
                            .select(MultipleLoanObject::getM_mainLoanID, MultipleLoanObject::getM_loanid)
                            .in(MultipleLoanObject::getM_loanid, mainLoanIds)
                            .eq(MultipleLoanObject::getM_loanType, 2))
                    .stream().collect(Collectors.toMap( MultipleLoanObject::getM_loanid,MultipleLoanObject::getM_mainLoanID));
            List<LoanRepayDetailObject> loanRepayDetailList = new ArrayList<>();
            for (List<Integer> loanIds : CollUtil.split(loanIdMap.keySet(), 500)) {
                List<LoanRepayDetailObject> loanRepayDetails = loanRepayDetailRepository
                        .list(Wrappers.lambdaQuery(LoanRepayDetailObject.class)
                                .in(LoanRepayDetailObject::getM_loanID, loanIds));
                loanRepayDetailList.addAll(loanRepayDetails);
            }
            List<Integer> productIds = multipleLoanMap.values().stream().map(MultipleLoanObject::getM_productID).collect(Collectors.toList());
            Map<Integer, String> productMap = loanProductRepository.list(Wrappers.lambdaQuery(LoanProductVo.class)
                    .in(LoanProductVo::getProductId, productIds)).stream().collect(Collectors.toMap(LoanProductVo::getProductId, LoanProductVo::getProductCode, (k1, k2) -> k1));
            return loanIdMap.keySet().stream().map(mainLoanId -> {
                MultipleLoanObject multipleLoanObject = MapUtil.getQuietly(multipleLoanMap, mainLoanId, MultipleLoanObject.class, new MultipleLoanObject());
                String repaymentDate = loanRepayDetailList.stream().filter(i -> Objects.equals(i.getM_loanID(), mainLoanId))
                        .filter(i -> BigDecimal.valueOf(2).equals(i.getM_thisTerm()))
                        .map(LoanRepayDetailObject::getM_repaymentDate).filter(Objects::nonNull)
                        .map(i -> String.valueOf(LocalDate.parse(i).getDayOfMonth()))
                        .findFirst().orElse("");
                String loanProduct = MapUtil.getQuietly(productMap, multipleLoanObject.getM_productID(), String.class, "");
                String boId = splBussinessBasicRepository.getBoIdByLoanId(loanIdMap.get(mainLoanId));
                String custId = Optional.ofNullable(remoteCluePlatformService.queryClue(boId))
                        .map(R::getData).map(ClueDTO::getBorrower).map(ClueDTO.Borrower::getUserId).orElse("");
                String applyNo = bankExpandInfoVoRepositoryMybatis.getApplyNoByLoanId(mainLoanId);
                return XAIntradayDisbursement.builder()
                        .partnerId(unionPayPartnerId)
                        .batchDate(LocalDate.now().minusDays(1).toString())
                        .productNo(StrUtil.nullToEmpty(loanProduct))
                        .custId(custId)
                        .duebillNo(StrUtil.nullToEmpty(applyNo))
                        .loanDate(StrUtil.nullToEmpty(multipleLoanObject.getM_grantMoneyDate()))
                        .loanAmt(NumberUtil.toBigDecimal(multipleLoanObject.getM_applyMoney()))
                        .intRate(NumberUtil.toBigDecimal(multipleLoanObject.getM_commonRate()))
                        .repayDay(repaymentDate)
                        .loanTerm(multipleLoanObject.getM_term().toString())
                        .putoutStatus("U")
                        .build();
            }).collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public List<IntradayPayment> createIntradayPayment(BankOrgNameType bank) {
        if (bank.equals(BankOrgNameType.XA_BANK)) {
            List<MultipleLoanObject> multipleLoans = multipleLoanRepository.list(Wrappers.lambdaQuery(MultipleLoanObject.class)
                    .eq(MultipleLoanObject::getM_selectBank, BankOrgNameType.XA_BANK.getKey()));
            List<Integer> mainloanIds = multipleLoans.stream().filter(i -> i.getM_loanType().equals("2")).map(MultipleLoanObject::getM_loanid).collect(Collectors.toList());
            if (mainloanIds.isEmpty()) {
                return Collections.emptyList();
            }
            List<LoanRepaymentObject> repaymentList = new ArrayList<>();
            for (List<Integer> subMainloanIds : CollUtil.split(mainloanIds, 500)) {
                List<LoanRepaymentObject> repayments = loanRepaymentRepository.list(Wrappers.lambdaQuery(LoanRepaymentObject.class)
                        .in(LoanRepaymentObject::getM_loanID, subMainloanIds)
                        .eq(LoanRepaymentObject::getM_repaymentDate, LocalDate.now().minusDays(1).toString())
                        .eq(LoanRepaymentObject::getLrDelflag, "0"));
                repaymentList.addAll(repayments);
            }
            Map<Integer, List<LoanRepaymentObject>> loanRepaymentMap = repaymentList.stream().collect(Collectors.groupingBy(LoanRepaymentObject::getM_loanID));
            List<IntradayPayment> intradayPayments = new ArrayList<>();
            loanRepaymentMap.keySet().forEach(key -> {
                //主
                MultipleLoanObject multipleLoanObject = multipleLoans.stream().filter(i -> Objects.equals(i.getM_loanid(), key)).findFirst().orElse(new MultipleLoanObject());
                Integer loanId = multipleLoans.stream().filter(i -> i.getM_loanid().equals(key)).map(MultipleLoanObject::getM_mainLoanID).findFirst().orElse(0);
                List<LoanRepaymentObject> loanRepaymentList = loanRepaymentMap.get(key);
                BigDecimal totalCapital = loanRepaymentList.stream().map(LoanRepaymentObject::getCapital).map(NumberUtil::nullToZero).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalInterest = loanRepaymentList.stream().map(LoanRepaymentObject::getInterest).map(NumberUtil::nullToZero).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalPenalty = loanRepaymentList.stream().map(LoanRepaymentObject::getPenalty).map(NumberUtil::nullToZero).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalCompound = loanRepaymentList.stream().map(LoanRepaymentObject::getCompound).map(NumberUtil::nullToZero).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalPaid = totalCapital.add(totalInterest).add(totalPenalty).add(totalCompound);
                Integer productId = multipleLoanObject.getM_productID();
                LoanProductVo loanProduct = loanProductRepository.selectByProductId(productId);
                DeductionRecord deductionRecord = deductionRecordRepository.getDataByLoanIdOrderByTime(loanId).stream().findFirst().orElse(new DeductionRecord());
                String boId = splBussinessBasicRepository.getBoIdByLoanId(loanId);
                String custId = Optional.ofNullable(remoteCluePlatformService.queryClue(boId))
                        .map(R::getData).map(ClueDTO::getBorrower).map(ClueDTO.Borrower::getUserId).orElse("");
                String applyNo = bankExpandInfoVoRepositoryMybatis.getApplyNoByMainLoanId(loanId);
                String repaymentDate = loanRepaymentList.stream().map(LoanRepaymentObject::getM_repaymentDate).filter(Objects::nonNull).findFirst().orElse("");
                String repayTerm = loanRepaymentList.stream().map(LoanRepaymentObject::getRepaymentTerm).filter(Objects::nonNull).distinct().collect(Collectors.joining(","));
                String status = "T";
                String dueBillStatus = "P";
                List<LoanRepayDetailObject> repayDetails = loanRepayDetailRepository.listByLoanId(key);
                //repaydetail的应还总额大于实还总额&& 实还>0  dueBillStatus = "U";
                List<String> repayTerms = loanRepaymentList.stream().map(LoanRepaymentObject::getRepaymentTerm).filter(Objects::nonNull).collect(Collectors.toList());
                List<LoanRepayDetailObject> repaiedDetails = repayDetails.stream().filter(i -> repayTerms.contains(i.getM_thisTerm().toString())).collect(Collectors.toList());
                BigDecimal totalPayment = repaiedDetails.stream().map(LoanRepayDetailObject::getM_thisPayment).map(NumberUtil::nullToZero).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalPaidCapital = repaiedDetails.stream().map(LoanRepayDetailObject::getM_capital).map(NumberUtil::nullToZero).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalPaidInterest = repaiedDetails.stream().map(LoanRepayDetailObject::getM_interest).map(NumberUtil::nullToZero).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalPaidPenalty = repaiedDetails.stream().map(LoanRepayDetailObject::getM_penalty).map(NumberUtil::nullToZero).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalPaidCompound = repaiedDetails.stream().map(LoanRepayDetailObject::getM_compound).map(NumberUtil::nullToZero).reduce(BigDecimal.ZERO, BigDecimal::add);
                int biggerPayment = totalPayment.subtract(totalPaidCapital).subtract(totalPaidInterest).subtract(totalPaidPenalty).subtract(totalPaidCompound).compareTo(BigDecimal.ZERO);
                if (biggerPayment > 0) {
                    dueBillStatus = "U";
                }
                //repaydetail的应还总额大于实还总额 && 应还日期小于实还日期    就是逾期
                boolean biggerActualPayDate = repaiedDetails.stream()
                        .anyMatch(i -> {
                            if (StrUtil.isNotBlank(i.getM_repaymentDate()) && StrUtil.isNotBlank(i.getM_actualRepaymentDate())) {
                                return DateUtil.parse(i.getM_repaymentDate()).isBefore(DateUtil.parse(i.getM_actualRepaymentDate()));
                            }
                            return false;
                        });
                if (biggerPayment > 0 && biggerActualPayDate) {
                    status = "O";
                    dueBillStatus = "O";
                }
                String finalStatus = status;
                String finalDueBillStatus = dueBillStatus;
                XAIntradayPayment intradayPayment = XAIntradayPayment.builder()
                        .partnerId(unionPayPartnerId)
                        .batchDate(LocalDate.now().minusDays(1).toString())
                        .productNo(StrUtil.nullToEmpty(loanProduct.getProductCode()))
                        .repayChannel("A")
                        .custId(custId)
                        .txnNo(StrUtil.nullToEmpty(deductionRecord.getOrderNumber()))
                        .duebillNo(StrUtil.nullToEmpty(applyNo))
                        .repayDate(repaymentDate)
                        //还款期数
                        .repayTerm(repayTerm)
                        //还款总额
                        .repayAmt(totalPaid)
                        //还款本金
                        .repayPrin(totalCapital)
                        //还款利息
                        .repayInt(totalInterest)
                        //还款罚息
                        .repayPenalty(totalPenalty)
                        //还款复利
                        .repayCompound(totalCompound)
                        //还款手续费
                        .repayFee(BigDecimal.ZERO)
                        //还款保费
                        .repayPremium(BigDecimal.ZERO)
                        //还款类型
                        .repayType(finalStatus)
                        //借据状态
                        .loanStatus(finalDueBillStatus)
                        //还款状态
                        .repayStatus("U")
                        //放款日期
                        .putoutDate(StrUtil.nullToEmpty(multipleLoanObject.getM_grantMoneyDate()))
                        //还款平台费用
                        .guaranteeFee(BigDecimal.ZERO)
                        //还款平台罚费
                        .guaranteePenaltyFee(BigDecimal.ZERO)
                        .build();
                intradayPayments.add(intradayPayment);
            });
            return intradayPayments;
        }
        return null;
    }

    @Override
    public List<IntradayOverdue> createIntradayOverdue(BankOrgNameType bank) {
        if (bank.equals(BankOrgNameType.XA_BANK)) {
            List<MultipleLoanObject> overdueLoans = multipleLoanRepository.list(Wrappers.lambdaQuery(MultipleLoanObject.class)
                    .eq(MultipleLoanObject::getM_selectBank, bank.getKey())
                    .eq(MultipleLoanObject::getM_repaymentStatus, "1"));
            Map<Integer, MultipleLoanObject> multipleLoanObjectMap = overdueLoans.stream().collect(Collectors.toMap(MultipleLoanObject::getM_loanid, Function.identity(), (k1, k2) -> k1));
            if (overdueLoans.isEmpty()) {
                return Collections.emptyList();
            }
            List<Integer> loanIds = overdueLoans.stream().map(MultipleLoanObject::getM_loanid).collect(Collectors.toList());
            Map<Integer, List<LoanRepayDetailObject>> loanRepayDetailsMap = new ConcurrentHashMap<>();
            for (List<Integer> splitLoanIds : CollUtil.split(loanIds, 500)) {
                Map<Integer, List<LoanRepayDetailObject>> loanRepayDetailMap = loanRepayDetailRepository.list(Wrappers.lambdaQuery(LoanRepayDetailObject.class)
                                .in(LoanRepayDetailObject::getM_loanID, splitLoanIds)
                                .eq(LoanRepayDetailObject::getM_repaymentState, Constants.REPAYMENT_STATE_PAY_IN_LATE))
                        .stream().collect(Collectors.groupingBy(LoanRepayDetailObject::getM_loanID));
                loanRepayDetailsMap.putAll(loanRepayDetailMap);
            }

            List<IntradayOverdue> intradayOverdues = new ArrayList<>();
            loanRepayDetailsMap.forEach((key, value) -> {
                String termConcatStr = value.stream().map(LoanRepayDetailObject::getM_thisTerm).map(String::valueOf).distinct().collect(Collectors.joining(","));
                String overdueDate = value.stream().map(LoanRepayDetailObject::getM_repaymentDate).map(LocalDate::parse).min(Comparator.comparing(Function.identity())).map(LocalDate::toString).orElse(null);
                BigDecimal overdueCapital = value.stream().map(LoanRepayDetailObject::getM_thisCapital).map(NumberUtil::nullToZero).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal overdueInterest = value.stream().map(LoanRepayDetailObject::getM_thisInterest).map(NumberUtil::nullToZero).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal overduePenalty = value.stream().map(LoanRepayDetailObject::getM_thisPenalty).map(NumberUtil::nullToZero).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal overdueCompound = value.stream().map(LoanRepayDetailObject::getM_thisCompound).map(NumberUtil::nullToZero).reduce(BigDecimal.ZERO, BigDecimal::add);
                MultipleLoanObject multipleLoanObject = MapUtil.getQuietly(multipleLoanObjectMap, key, MultipleLoanObject.class, new MultipleLoanObject());
                Integer productId = multipleLoanObject.getM_productID();
                LoanProductVo loanProduct = loanProductRepository.selectByProductId(productId);
                String applyNo = bankExpandInfoVoRepositoryMybatis.getApplyNoByLoanId(key);
                XAIntradayOverdue intradayOverdue = XAIntradayOverdue.builder()
                        .partnerId(unionPayPartnerId)
                        .batchDate(LocalDate.now().minusDays(1).toString())
                        .productNo(loanProduct.getProductCode())
                        .duebillNo(StrUtil.nullToEmpty(applyNo))
                        .loanDate(StrUtil.nullToEmpty(multipleLoanObject.getM_grantMoneyDate()))
                        .overdueTerm(StrUtil.nullToEmpty(termConcatStr))
                        .overdueDate(StrUtil.nullToEmpty(overdueDate))
                        .overduePrin(overdueCapital)
                        .overdueInt(overdueInterest)
                        .overduePenalty(overduePenalty)
                        .overdueCompound(overdueCompound)
                        .overdueFee(BigDecimal.ZERO)
                        .build();
                intradayOverdues.add(intradayOverdue);
            });
            return intradayOverdues;
        }
        return null;
    }

    @Override
    public void processXADataAnalysis(GeneralLedger generalLedger, IntradayGeneralLedger intradayGeneralLedger, List<IntradayCredit> intradayCredit, List<IntradayDisbursement> intradayDisbursement, List<IntradayPayment> intradayPayment, List<IntradayOverdue> intradayOverdue) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, SftpSendFailException {
        //sftp
        BankUploadImageToSftp bankUploadImageToSftp = new BankUploadImageToSftp(this.ftpHost, this.ftpPort, this.ftpUserName, this.ftpPassword);
        //生成总账csv
        String generalSumFileName = fileStorePath + "/" + unionPayPartnerId + "_" + LocalDate.now().minusDays(1).toString().replaceAll("-", "") + "_GENERAL_SUM.csv";
        try (FileWriter generalSumFileWriter = new FileWriter(generalSumFileName)) {
            ICSVWriter writer = new CSVWriterBuilder(generalSumFileWriter).withEscapeChar(ICSVWriter.NO_ESCAPE_CHARACTER).withSeparator('|').withQuoteChar(ICSVWriter.NO_QUOTE_CHARACTER).build();
            XAGeneralLedger xaGeneralLedger = (XAGeneralLedger) generalLedger;
            if (generalLedger == null) {
                writer.writeAll(EMPTYLIST, false);
                writer.close();
                bankUploadImageToSftp.uploadSftp(this.unionPayBIPath, generalSumFileName, LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            } else {
                String rightStr = StrUtil.split(xaGeneralLedger.toString(), '(').get(1);
                String lastStr = StrUtil.removeSuffix(rightStr, ")");
                String[] dataArray = Arrays.stream(lastStr.split(",")).map(i -> StrUtil.removePrefix(i, " ")).map(i -> i.equals("null") ? "" : i).toArray(String[]::new);
                log.info("生成文件: {}", generalSumFileName);
                writer.writeNext(dataArray, false);
                writer.close();
                bankUploadImageToSftp.uploadSftp(this.unionPayBIPath, generalSumFileName, LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd")));

            }
        }

        //生成当日总账csv
        String generalFileName = fileStorePath + "/" + unionPayPartnerId + "_" + LocalDate.now().minusDays(1).toString().replaceAll("-", "") + "_GENERAL.csv";
        try (FileWriter generalFileWriter = new FileWriter(generalFileName)) {
            XAIntradayGeneralLedger xaIntradayGeneralLedger = (XAIntradayGeneralLedger) intradayGeneralLedger;
            ICSVWriter writer = new CSVWriterBuilder(generalFileWriter).withEscapeChar(ICSVWriter.NO_ESCAPE_CHARACTER).withSeparator('|').withQuoteChar(ICSVWriter.NO_QUOTE_CHARACTER).build();
            if (intradayGeneralLedger == null) {
                writer.writeAll(EMPTYLIST, false);
                writer.close();
                bankUploadImageToSftp.uploadSftp(this.unionPayBIPath, generalFileName, LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            } else {
                String rightStr = StrUtil.split(xaIntradayGeneralLedger.toString(), '(').get(1);
                String lastStr = StrUtil.removeSuffix(rightStr, ")");
                String[] dataArray = Arrays.stream(lastStr.split(",")).map(i -> StrUtil.removePrefix(i, " ")).map(i -> i.equals("null") ? "" : i).toArray(String[]::new);
                log.info("生成文件: {}", generalFileName);
                writer.writeNext(dataArray, false);
                writer.close();
                bankUploadImageToSftp.uploadSftp(this.unionPayBIPath, generalFileName, LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            }
        }

        //生成当日授信明细csv
        String creditFileName = fileStorePath + "/"  + unionPayPartnerId + "_" + LocalDate.now().minusDays(1).toString().replaceAll("-", "") + "_CREDIT.csv";
        try (FileWriter creditFileWriter = new FileWriter(creditFileName)) {
            List<XAIntradayCredit> intradayCreditList = intradayCredit.stream().map(i -> (XAIntradayCredit) i).collect(Collectors.toList());
            ICSVWriter writer = new CSVWriterBuilder(creditFileWriter).withEscapeChar(ICSVWriter.NO_ESCAPE_CHARACTER).withSeparator('|').withQuoteChar(ICSVWriter.NO_QUOTE_CHARACTER).build();
            List<String[]> dataList = intradayCreditList.stream()
                    .map(i -> {
                        String rightStr = StrUtil.split(i.toString(), '(').get(1);
                        String lastStr = StrUtil.removeSuffix(rightStr, ")");
                        return Arrays.stream(lastStr.split(",")).map(j -> StrUtil.removePrefix(j," ")).toArray(String[]::new);
                    })
                    .collect(Collectors.toList());
            log.info("生成文件: {}",creditFileName);
            writer.writeAll(dataList);
            writer.close();
            bankUploadImageToSftp.uploadSftp(this.unionPayBIPath, creditFileName, LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        }

        //生成当日放款明细csv
        String disbursementFileName = fileStorePath + "/"  + unionPayPartnerId + "_" + LocalDate.now().minusDays(1).toString().replaceAll("-", "") + "_DISBURSEMENT.csv";
        try (FileWriter disbursementFileWriter = new FileWriter(disbursementFileName)) {
            List<XAIntradayDisbursement> intradayDisbursementList = intradayDisbursement.stream().map(i -> (XAIntradayDisbursement) i).collect(Collectors.toList());
            ICSVWriter writer = new CSVWriterBuilder(disbursementFileWriter).withEscapeChar(ICSVWriter.NO_ESCAPE_CHARACTER).withSeparator('|').withQuoteChar(ICSVWriter.NO_QUOTE_CHARACTER).build();
            List<String[]> dataList = intradayDisbursementList.stream()
                    .map(i -> {
                        String rightStr = StrUtil.split(i.toString(), '(').get(1);
                        String lastStr = StrUtil.removeSuffix(rightStr, ")");
                        return Arrays.stream(lastStr.split(",")).map(j -> StrUtil.removePrefix(j," ")).toArray(String[]::new);
                    })
                    .collect(Collectors.toList());
            log.info("生成文件: {}",disbursementFileName);
            writer.writeAll(dataList);
            writer.close();
            bankUploadImageToSftp.uploadSftp(this.unionPayBIPath, disbursementFileName, LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        }

        //生成当日还款明细csv
        String paymentFileName = fileStorePath + "/"  + unionPayPartnerId + "_" + LocalDate.now().minusDays(1).toString().replaceAll("-", "") + "_PAYMENT.csv";
        try (FileWriter paymentFileWriter = new FileWriter(paymentFileName)) {
            List<XAIntradayPayment> intradayPaymentList = intradayPayment.stream().map(i -> (XAIntradayPayment) i).collect(Collectors.toList());
            ICSVWriter writer = new CSVWriterBuilder(paymentFileWriter).withSeparator('|').withEscapeChar(ICSVWriter.NO_ESCAPE_CHARACTER).withQuoteChar(ICSVWriter.NO_QUOTE_CHARACTER).build();
            List<String[]> dataList = intradayPaymentList.stream()
                    .map(i -> {
                        String rightStr = StrUtil.split(i.toString(), '(').get(1);
                        String lastStr = StrUtil.removeSuffix(rightStr, ")");
                        return Arrays.stream(lastStr.split(" "))
                                .map(t -> StrUtil.removeSuffix(t,",")).toArray(String[]::new);
                    })
                    .collect(Collectors.toList());
            log.info("生成文件: {}",paymentFileName);
            writer.writeAll(dataList);
            writer.close();
            bankUploadImageToSftp.uploadSftp(this.unionPayBIPath, paymentFileName, LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        }

        //生成当日逾期明细csv
        String overdueFileName = fileStorePath + "/" + unionPayPartnerId + "_" + LocalDate.now().minusDays(1).toString().replaceAll("-", "") + "_OVERDUE.csv";
        try (FileWriter overdueFileWriter = new FileWriter(overdueFileName)) {
            List<XAIntradayOverdue> intradayOverdueList = intradayOverdue.stream().map(i -> (XAIntradayOverdue) i).collect(Collectors.toList());
            ICSVWriter writer = new CSVWriterBuilder(overdueFileWriter).withSeparator('|').withEscapeChar(ICSVWriter.NO_ESCAPE_CHARACTER).withQuoteChar(ICSVWriter.NO_QUOTE_CHARACTER).build();
            List<String[]> dataList = intradayOverdueList.stream()
                    .map(i -> {
                        String rightStr = StrUtil.split(i.toString(), '(').get(1);
                        String lastStr = StrUtil.removeSuffix(rightStr, ")");
                        return Arrays.stream(lastStr.split(" "))
                                .map(t -> StrUtil.removeSuffix(t,",")).toArray(String[]::new);
                    })
                    .collect(Collectors.toList());
            log.info("生成文件: {}",overdueFileName);
            writer.writeAll(dataList);
            writer.close();
            bankUploadImageToSftp.uploadSftp(this.unionPayBIPath, overdueFileName, LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        }
    }

}
