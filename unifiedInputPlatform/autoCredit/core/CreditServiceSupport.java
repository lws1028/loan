package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.autoCredit.core;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zlhj.commonLoan.business.appCommon.domain.credit.TelemarketingCreditDomainService;
import com.zlhj.commonLoan.business.appCommon.domain.credit.TelemarketingCreditRepository;
import com.zlhj.commonLoan.business.appCommon.domain.credit.ZQHTCredit;
import com.zlhj.commonLoan.business.appCommon.domain.credit.ZQHTCreditType;
import com.zlhj.commonLoan.business.appCommon.enums.ZQHTCreditRuleResult;
import com.zlhj.commonLoan.business.appCommon.enums.ZQHTPreApproveRuleResult;
import com.zlhj.commonLoan.business.appCommon.service.CreditInvestigationService;
import com.zlhj.commonLoan.business.appCommon.service.RuleEngineService;
import com.zlhj.commonLoan.business.basic.service.WeChatMessagePushService;
import com.zlhj.commonLoan.business.clue.dto.LoanStatePushToClueDTO;
import com.zlhj.commonLoan.business.clue.enums.LoanStatusChangeEnum;
import com.zlhj.commonLoan.business.common.exception.BusinessException;
import com.zlhj.commonLoan.domain.creditBusiness.CreditOrderId;
import com.zlhj.commonLoan.domain.creditBusiness.hcd.HCDCreditBusiness;
import com.zlhj.commonLoan.domain.mainLoan.LoanId;
import com.zlhj.commonLoan.util.StringUtil;
import com.zlhj.electronicCredit.pojo.CreditLoan;
import com.zlhj.electronicCredit.pojo.CreditLoanRepository;
import com.zlhj.electronicCredit.pojo.SpcLiminaryVerdictVo;
import com.zlhj.electronicCredit.service.CreditLoanService;
import com.zlhj.hrxj.interfaces.dto.CarInfoDto;
import com.zlhj.hrxj.interfaces.dto.CarInfoRepository;
import com.zlhj.infrastructure.po.CreditAuthorization;
import com.zlhj.infrastructure.repository.CreditAuthorizationRepository;
import com.zlhj.jd.vo.DirectCustomerStatusDto;
import com.zlhj.loan.SendEmailMessage;
import com.zlhj.loan.service.BankApprovalRecordService;
import com.zlhj.loan.service.PreliminaryScreeningService;
import com.zlhj.loan.vo.BankApprovalRecordPo;
import com.zlhj.main.fumin.enums.BankOrgNameType;
import com.zlhj.mapper.SplBussinessbasicMapper;
import com.zlhj.mq.provider.Sender;
import com.zlhj.unifiedInputPlatform.autoCredit.dto.ClueStatusInfoDTO;
import com.zlhj.unifiedInputPlatform.autoCredit.dto.QueryClueBillDTO;
import com.zlhj.unifiedInputPlatform.autoCredit.dto.QueryClueInfoDTO;
import com.zlhj.unifiedInputPlatform.autoCredit.strategy.NotificationStrategy;
import com.zlhj.unifiedInputPlatform.jd.pojo.ClueQueryBillBusiness;
import com.zlhj.unifiedInputPlatform.jdjt.dto.VehicleEvaluationNotifyDTO;
import com.zlhj.unifiedInputPlatform.universal.service.UnifiedInputPlatformService;
import com.zlhj.user.vo.MultipleLoanObject;
import com.zlhj.user.vo.MultipleLoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CreditServiceSupport {
    @Autowired
    private CreditInvestigationService creditInvestigationService;
    @Autowired
    private TelemarketingCreditRepository telemarketingCreditRepository;
    @Autowired
    private Map<String, TelemarketingCreditDomainService> telemarketingCreditDomainServiceMap;
    @Autowired
    private RuleEngineService ruleEngineService;
    @Autowired
    private Map<String, NotificationStrategy> notificationStrategyMap;
    @Autowired
    private UnifiedInputPlatformService unifiedInputPlatformService;
    @Autowired
    private Sender sender;
    @Autowired
    private WeChatMessagePushService weChatMessagePushService;
    @Autowired
    private SendEmailMessage sendEmailMessage;
    @Autowired
    private CreditAuthorizationRepository creditAuthorizationRepository;
    @Autowired
    private CreditLoanRepository creditLoanRepository;
    @Autowired
    private PreliminaryScreeningService preliminaryScreeningService;
    @Autowired
    private CreditLoanService creditLoanService;
    @Autowired
    private SplBussinessbasicMapper splBussinessbasicMapper;
    @Autowired
    private MultipleLoanRepository loanInfoRepository;
    @Autowired
    private BankApprovalRecordService bankApprovalRecordService;
    @Autowired
    private CarInfoRepository carInfoRepository;
    @Autowired
    private ClueQueryBillBusinessFactory clueQueryBillBusinessFactory;

    public ClueStatusInfoDTO queryClueInfo(QueryClueInfoDTO queryDTO) {

        ClueStatusInfoDTO notifyDTO = new ClueStatusInfoDTO();
        notifyDTO.setBoId(queryDTO.getApplyNo());

        LoanStatusChangeEnum realTimeStatusEnum = LoanStatusChangeEnum.getEnumsByValue(queryDTO.getStatus());

        DirectCustomerStatusDto directCustomerStatusDto = splBussinessbasicMapper.selectStatusByOrderIdNoChannel(
                queryDTO.getApplyNo()
        );

        if (realTimeStatusEnum == null) {
            if (directCustomerStatusDto == null) {
                return notifyDTO;
            }
            realTimeStatusEnum = LoanStatusChangeEnum.getEnumsByValue(directCustomerStatusDto.getSplMaxActionNum());
        }

        notifyDTO.setCheckStatus(realTimeStatusEnum.getValue());

        if (LoanStatusChangeEnum.APPROVE_PASS == realTimeStatusEnum) {
            if (directCustomerStatusDto == null) {
                return notifyDTO;
            }
            MultipleLoanObject loanInfo = loanInfoRepository.getLoanIdByMainLoanId(directCustomerStatusDto.getSplLoanId());
            if (loanInfo == null) {
                return notifyDTO;
            }
            BankApprovalRecordPo bankApprovalRecord = bankApprovalRecordService.getBankApprovalRecord(directCustomerStatusDto.getSplLoanId());
            CarInfoDto carInfoDto = carInfoRepository.getCarInfoByLoanId(loanInfo.getM_loanid());

            String applyLimit = bankApprovalRecord.getApplyLimit();
            BigDecimal commonRate = loanInfo.getM_commonRate() == null ? null : new BigDecimal(loanInfo.getM_commonRate().toString());
            LocalDateTime localDateTime = LocalDateTime.parse(bankApprovalRecord.getCreateTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            long createTime = localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            LocalDateTime plusDays = localDateTime.plusDays(30);
            Integer term = loanInfo.getM_term();

            notifyDTO.setCheckAmount(applyLimit);
            notifyDTO.setAnnualRate(commonRate);
            notifyDTO.setApplySuccTime(createTime);
            notifyDTO.setCarLicense(carInfoDto.getCiLicense());
            notifyDTO.setPeriod(term);
            // 计算30天后的时间戳
            long expireTime = plusDays.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            notifyDTO.setExpireTime(expireTime);

            return notifyDTO;
        }

        if (LoanStatusChangeEnum.LEND_SUC == realTimeStatusEnum) {
            if (directCustomerStatusDto == null) {
                return notifyDTO;
            }
            MultipleLoanObject loanInfo = loanInfoRepository.getLoanIdByMainLoanId(directCustomerStatusDto.getSplLoanId());

            if (loanInfo == null) {
                return notifyDTO;
            }

            BankApprovalRecordPo bankApprovalRecord = bankApprovalRecordService.getBankApprovalRecord(directCustomerStatusDto.getSplLoanId());
            CarInfoDto carInfoDto = carInfoRepository.getCarInfoByLoanId(loanInfo.getM_loanid());

            String applyLimit = bankApprovalRecord.getApplyLimit();
            BigDecimal commonRate = loanInfo.getM_commonRate() == null ? null : new BigDecimal(loanInfo.getM_commonRate().toString());

            LocalDateTime localDateTime = LocalDateTime.parse(bankApprovalRecord.getCreateTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            long createTime = localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            LocalDateTime plusDays = localDateTime.plusDays(30);
            Integer term = loanInfo.getM_term();
            LocalDate paymentTime = LocalDate.parse(loanInfo.getM_grantMoneyDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            long paymentTimeMillis = paymentTime.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            notifyDTO.setMhtIouNo(loanInfo.getM_loanNumber());
            notifyDTO.setPaymentTime(paymentTimeMillis);
            notifyDTO.setCashAmount(loanInfo.getM_applyMoney().toString());
            notifyDTO.setCheckAmount(applyLimit);
            notifyDTO.setAnnualRate(commonRate);
            notifyDTO.setApplySuccTime(createTime);
            notifyDTO.setCarLicense(carInfoDto.getCiLicense());
            notifyDTO.setPeriod(term);
            BankOrgNameType bankOrgNameType = BankOrgNameType.get(Integer.valueOf(loanInfo.getM_selectBank()));
            notifyDTO.setRealLenderId(bankOrgNameType == null ? "" : bankOrgNameType.getDescription());
            // 计算30天后的时间戳
            long expireTime = plusDays.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            notifyDTO.setExpireTime(expireTime);

            return notifyDTO;
        }

        return notifyDTO;
    }


    public QueryClueBillDTO clueQueryBill(String applyNo) {
        DirectCustomerStatusDto directCustomerStatusDto = splBussinessbasicMapper.selectStatusByOrderIdNoChannel(
                applyNo
        );
        if (directCustomerStatusDto == null) {
            throw new BusinessException("线索没有匹配的贷款数据");
        }

        if (ObjectUtil.isEmpty(directCustomerStatusDto.getSplLoanId())) {
            throw new BusinessException("线索没有匹配的贷款数据");
        }
        ClueQueryBillBusiness clueQueryBillBusiness = clueQueryBillBusinessFactory.create(new LoanId(directCustomerStatusDto.getSplLoanId()));
        return QueryBillConvert.convert(clueQueryBillBusiness);
    }

    /**
     * 车架评估查询
     */
    public VehicleEvaluationNotifyDTO queryVehicleEvaluation(String boId){
        CreditAuthorization credit = new CreditAuthorization();
        credit.setBoId(boId);
        credit.setAuthorizationStatus(1);
        List<CreditAuthorization> creditAuthorizationList = creditAuthorizationRepository.list(credit);

        List<String> applicationNos = creditAuthorizationList.stream()
                .map(CreditAuthorization::getApplicationNo).collect(Collectors.toList());

        String evaluateAmount = "";
        VehicleEvaluationNotifyDTO evaluationNotifyDTO = new VehicleEvaluationNotifyDTO();
        if (!applicationNos.isEmpty()) {
            List<CreditLoan> creditLoans = creditLoanRepository.searchCreditDataByApplicationNo(applicationNos);
            if (!creditLoans.isEmpty()) {
                CreditLoan creditLoan = creditLoans.stream()
                        .filter(r -> "98".equals(r.getSelectCode()))
                        .max(Comparator.comparing(CreditLoan::getScreeningTime)).orElse(null);
                if (creditLoan == null) {
                    return evaluationNotifyDTO;
                }
                if("初筛拒绝".equals(creditLoan.getScreenResult())){
                    evaluationNotifyDTO.setRefuseReason("初筛拒绝");
                    evaluationNotifyDTO.setCheckStatus("FAILURE");
                    return evaluationNotifyDTO;
                }

                SpcLiminaryVerdictVo liminaryVerdictVo = preliminaryScreeningService.searchliminaryVerdictPreResultByCreditOrderIdForFull(creditLoan.getCreditOrderId());
                if (liminaryVerdictVo == null) {
                    liminaryVerdictVo = preliminaryScreeningService.searchliminaryVerdictPreResultByCreditOrderId(creditLoan.getCreditOrderId());
                }
                if (liminaryVerdictVo != null) {
                    String decisionRules = com.apollo.util.ConvertionUtil.getSimpleStringWithNull(liminaryVerdictVo
                            .getDecisionRulegroupResultlist());
                    JSONObject parseObject = JSONObject.parseObject(decisionRules);
                    JSONArray decisionRuleGroupResultList = parseObject.getJSONArray("decisionRuleGroupResultList");

                    if (decisionRuleGroupResultList.size() > 0) {
                        JSONObject showArea = (JSONObject) decisionRuleGroupResultList.get(0);
                        //不包含showarea为老逻辑数据按老逻辑处理，包含showarea为新逻辑数据按新逻辑处理
                        if (showArea.getString("showArea") != null) {
                            try {
                                JSONObject jsonObject = creditLoanService.analysisPreApproveResult(parseObject, creditLoan.getCreditOrderId(), -1);
                                List<String> CRRuleTip = (List<String>) jsonObject.get("CRRuleTip");
                                List<String> RuleGroupSentence = (List<String>) jsonObject.get("RuleGroupSentence");
                                if ("1".equals(creditLoan.getBeforeApproveConclusion()) ||
                                        "2".equals(creditLoan.getBeforeApproveConclusion()) ||
                                        "4".equals(creditLoan.getBeforeApproveConclusion())) {
                                    for (String rule : CRRuleTip) {
                                        if (rule.contains("授信额度")) {
                                            evaluateAmount = rule;
                                            break;
                                        }
                                    }
                                } else {
                                    String refuseReason = "";
                                    if (!CollectionUtils.isEmpty(CRRuleTip)) {
                                        for (String rule : CRRuleTip) {
                                            if (rule.contains("拒绝")) {
                                                refuseReason = rule;
                                                break;
                                            }
                                        }
                                    }
                                    if (!CollectionUtils.isEmpty(RuleGroupSentence) && refuseReason.isEmpty()){
                                        for (String rule : RuleGroupSentence) {
                                            if (rule.contains("拒绝")) {
                                                refuseReason = rule;
                                                break;
                                            }
                                        }
                                    }
                                    evaluationNotifyDTO.setRefuseReason(refuseReason.isEmpty() ? "初筛拒绝" : refuseReason);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                if (StringUtil.isNotEmpty(evaluateAmount)) {
                    evaluateAmount = evaluateAmount.substring(5);
                    BigDecimal finalMoney;
                    try {
                        finalMoney = new BigDecimal(evaluateAmount);
                        evaluateAmount = finalMoney.toString();
                    } catch (Exception e) {
                        finalMoney = new BigDecimal("0.0");
                        evaluateAmount = finalMoney.toString();
                    }
                } else {
                    evaluateAmount = new BigDecimal("0.0").toString();
                }

                if ("1".equals(creditLoan.getBeforeApproveConclusion()) ||
                        "2".equals(creditLoan.getBeforeApproveConclusion()) ||
                        "4".equals(creditLoan.getBeforeApproveConclusion())) {

                    if (new BigDecimal(evaluateAmount).compareTo(BigDecimal.ZERO) > 0) {
                        evaluationNotifyDTO.setCheckStatus("SUCCESS");
                        evaluationNotifyDTO.setEvaluateAmount(evaluateAmount);
                    } else {
                        evaluationNotifyDTO.setCheckStatus("FAILURE");
                    }
                }
                if ("3".equals(creditLoan.getBeforeApproveConclusion())) {
                    evaluationNotifyDTO.setCheckStatus("FAILURE");
                }
            }
        }

        return evaluationNotifyDTO;
    }

    public NotificationStrategy getNotificationStrategy(String strategyBeanName) {
        return notificationStrategyMap.get(strategyBeanName);
    }

    public ZQHTCredit findByHCDCreditBusiness(HCDCreditBusiness hcdCreditBusiness, ZQHTCreditType zqhtCreditType) {
        return telemarketingCreditRepository.findByHCDCreditBusiness(hcdCreditBusiness, zqhtCreditType);
    }
    public TelemarketingCreditDomainService getDomainService(String domainService) {
        return telemarketingCreditDomainServiceMap.get(domainService);
    }

    public void sendWechatEmail(String subject, String emailContent, String interfaceType) {
        sendEmailMessage.sendEmail(subject, emailContent, interfaceType);
    }

    public void pushWeChatMessagePrePass(String creditOrderId) {
        weChatMessagePushService.pushWeChatMessagePrePass(creditOrderId);
    }

    public void notifyCluePlatform(LoanStatePushToClueDTO dto) {
        unifiedInputPlatformService.realTimeInteraction(dto);
    }

    public void notifyTelemarketing(LoanStatePushToClueDTO dto) {
        sender.clueStatusNotify(dto);
    }

    public HCDCreditBusiness investigation(CreditOrderId creditOrderId) {
        return creditInvestigationService.investigation(creditOrderId);
    }

    public ZQHTCreditRuleResult processZQHTCredit(ZQHTCredit zqhtCredit) {
        return ruleEngineService.processZQHTCredit(zqhtCredit);
    }

    public void updateZQHTCredit(ZQHTCredit zqhtCredit) {
        telemarketingCreditRepository.update(zqhtCredit);
    }

    public ZQHTPreApproveRuleResult processFullPricePreApproval(ZQHTCredit zqhtCredit, ZQHTCreditRuleResult zqhtCreditRuleResult) {
        return ruleEngineService.processFullPricePreApproval(zqhtCredit, zqhtCreditRuleResult);
    }
}