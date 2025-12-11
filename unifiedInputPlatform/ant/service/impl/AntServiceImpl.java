package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.ant.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.apollo.alds.util.ConvertionUtil;
import com.apollo.org.vo.OrgUserVo;
import com.apollo.org.vo.UserObjectRepository;
import com.apollo.sap.vo.SapDcsLoanAppStatusObject;
import com.apollo.sap.vo.SapDcsLoanAppStatusRepository;
import com.apollo.util.DateUtil;
import com.common.LocalDateUtils;
import com.mybatis.DbSqlSessionFactory;
import com.zlhj.InterfaceMessage.dto.MainLeasing;
import com.zlhj.InterfaceMessage.dto.MainLeasingRepository;
import com.zlhj.common.core.domain.R;
import com.zlhj.common.core.utils.StringUtils;
import com.zlhj.commonLoan.business.appCommon.service.CreditAuthorizationService;
import com.zlhj.commonLoan.business.basic.DTO.WeChatMessagePushDTO;
import com.zlhj.commonLoan.business.basic.service.WeChatMessagePushService;
import com.zlhj.commonLoan.business.clue.dto.LoanStatePushToClueDTO;
import com.zlhj.commonLoan.business.clue.enums.LoanStatusChangeEnum;
import com.zlhj.commonLoan.business.common.exception.AreaInadmissibleException;
import com.zlhj.commonLoan.business.common.exception.BusinessException;
import com.zlhj.commonLoan.business.common.exception.ClueResubmissionException;
import com.zlhj.commonLoan.business.common.pojo.Identity;
import com.zlhj.commonLoan.business.common.pojo.enums.ApproveRejectReasonZlhjToAntEnum;
import com.zlhj.commonLoan.business.common.pojo.enums.LendRejectReasonZlhjToAntEnum;
import com.zlhj.commonLoan.business.jjyh.service.LoanAppStatusService;
import com.zlhj.commonLoan.domain.creditAuth.CreditAuthID;
import com.zlhj.commonLoan.domain.cule.*;
import com.zlhj.commonLoan.domain.mainLoan.LoanId;
import com.zlhj.commonLoan.infrastructure.exception.SettleOrderNoticeUserException;
import com.zlhj.commonLoan.util.BigDecimalUtil;
import com.zlhj.commonLoan.util.SHA256Encryptor;
import com.zlhj.commonLoan.util.StringUtil;
import com.zlhj.electronicCredit.pojo.*;
import com.zlhj.electronicCredit.service.CreditLoanService;
import com.zlhj.entity.BusinessBasicEntity;
import com.zlhj.externalChannel.InterfaceDatum;
import com.zlhj.infrastructure.mapper.BankHeadOfficeMapper;
import com.zlhj.infrastructure.po.AuthorizedImage;
import com.zlhj.infrastructure.po.AuthorizedImageFactory;
import com.zlhj.infrastructure.po.CreditAuthorization;
import com.zlhj.infrastructure.po.CreditAuthorizationFactory;
import com.zlhj.infrastructure.repository.AuthorizedImageRepository;
import com.zlhj.infrastructure.repository.CarAgeMileageConfigRepository;
import com.zlhj.infrastructure.repository.CreditAuthorizationRepository;
import com.zlhj.infrastructure.repository.EncryptionPhoneCheckRepository;
import com.zlhj.infrastructure.routing.RemoteBigDataService;
import com.zlhj.infrastructure.routing.RemoteCluePlatformService;
import com.zlhj.infrastructure.routing.dto.RemoteInterfaceRequest;
import com.zlhj.infrastructure.routing.dto.RemoteInterfaceResponse;
import com.zlhj.infrastructure.routing.dto.clue.ClueApproveFileApiRespDTO;
import com.zlhj.infrastructure.routing.dto.clue.CluePreApproveApiRespDTO;
import com.zlhj.jd.vo.ClueApproveInfoDto;
import com.zlhj.jd.vo.DirectCustomerStatusDto;
import com.zlhj.loan.SendEmailMessage;
import com.zlhj.loan.service.BankApprovalRecordService;
import com.zlhj.loan.service.PreliminaryScreeningService;
import com.zlhj.loan.vo.BankApprovalRecordPo;
import com.zlhj.loan.vo.BankHeadOfficeVo;
import com.zlhj.mapper.SplBussinessbasicMapper;
import com.zlhj.mq.dto.AntPreApproveMessage;
import com.zlhj.mq.dto.ClueShowPushMessage;
import com.zlhj.mq.provider.Sender;
import com.zlhj.tianfu.Util.Tool;
import com.zlhj.tianfu.dao.AllMapper;
import com.zlhj.unifiedInputPlatform.ant.dto.*;
import com.zlhj.unifiedInputPlatform.autoCredit.universal.transform.FddRASignDTOAssembler;
import com.zlhj.unifiedInputPlatform.ant.enums.ClueLabelEnums;
import com.zlhj.unifiedInputPlatform.ant.enums.RefuseReasonType;
import com.zlhj.unifiedInputPlatform.ant.exceptions.AntPreApproveException;
import com.zlhj.unifiedInputPlatform.ant.service.AntService;
import com.zlhj.unifiedInputPlatform.autoCredit.universal.transform.CreditAuthorizationTransform;
import com.zlhj.unifiedInputPlatform.ant.transform.Transform;
import com.zlhj.unifiedInputPlatform.ant.utils.AntFlowSwitcher;
import com.zlhj.unifiedInputPlatform.autoCredit.dto.FddRASignDTO;
import com.zlhj.unifiedInputPlatform.smy.entity.EncryptionPhoneCheck;
import com.zlhj.unifiedInputPlatform.universal.service.ClueApproveInfoService;
import com.zlhj.unifiedInputPlatform.universal.service.impl.UnifiedInputPlatformServiceImpl;
import com.zlhj.user.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AntServiceImpl implements AntService {

    @Autowired
    private EncryptionPhoneCheckRepository encryptionPhoneCheckRepository;

    @Autowired
    private SplBussinessbasicMapper splBussinessbasicMapper;

    @Autowired
    private InterfaceDatum interfaceDatum;

    @Autowired
    private MultipleLoanRepository loanInfoRepository;

    @Autowired
    private MainLeasingRepository mainLeasingRepository;

    @Autowired
    private ExhibitionAndPayInfoRepository exhibitionAndPayInfoRepository;

    @Autowired
    private ApproveRepository approveRepository;

    @Autowired
    private LoanAppStatusService loanAppStatusService;

    @Autowired
    private SapDcsLoanAppStatusRepository sapDcsLoanAppStatusRepository;

    @Autowired
    private BankHeadOfficeMapper bankHeadOfficeMapper;

    @Autowired
    private CuleDomainService culeDomainService;

    @Autowired
    private UnifiedInputPlatformServiceImpl unifiedInputPlatformService;

    @Autowired
    private AntRepaymentPlanFactory antRepaymentPlanFactory;

    @Autowired
    private Sender sender;

    @Autowired
    private AuthorizedImageFactory authorizedImageFactory;

    @Autowired
    private CreditAuthorizationRepository creditAuthorizationRepository;

    @Autowired
    private RemoteCluePlatformService remoteCluePlatformService;

    @Autowired
    private CreditAuthorizationFactory creditAuthorizationFactory;
    @Autowired
    private UserObjectRepository userObjectRepository;

    @Autowired
    private AuthorizedImageRepository authorizedImageRepository;

    @Autowired
    private CreditAuthorizationService creditAuthorizationService;

    @Value("${zlhj.image.postUrlDomain}")
    private String fileUrl;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RemoteBigDataService remoteBigDataService;

    @Autowired
    private SendEmailMessage sendEmailMessage;
    @Autowired
    private CarAgeMileageConfigRepository carAgeMileageConfigRepository;
    @Autowired
    private CreditLoanRepository creditLoanRepository;
    @Autowired
    private AntCreditProductMappingRepository antCreditProductMappingRepository;
    @Autowired
    private PreliminaryScreeningService preliminaryScreeningService;
    @Autowired
    private CreditLoanService creditLoanService;
    @Autowired
    private BankApprovalRecordService bankApprovalRecordService;

    @Autowired
    private AntFlowSwitcher flowSwitcher;
    @Autowired
    private ClueApproveInfoService clueApproveInfoService;
    @Autowired
    private WeChatMessagePushService weChatMessagePushService;
    @Autowired
    private RuleProductRepository ruleProductRepository;
    @Autowired
    private CreditUserVORepository creditUserVORepository;


    @Override
    public Optional<EncryptionPhoneCheck> matchSha256PhoneNumber(String mobileSHA256) {

        if (StringUtil.isEmpty(mobileSHA256)) {
            throw new IllegalArgumentException("手机号为空");
        }

        Calendar thirtyDaysAgo = Calendar.getInstance();
        Date nowDate = thirtyDaysAgo.getTime();
        thirtyDaysAgo.add(Calendar.DATE, -30);
        Date thirtyDaysAgoDate = thirtyDaysAgo.getTime();

        EncryptionPhoneCheck queryParam = new EncryptionPhoneCheck();
        queryParam.setSha256PhoneNumber(mobileSHA256.toLowerCase());
        List<EncryptionPhoneCheck> encryptionPhoneCheckList = encryptionPhoneCheckRepository.list(queryParam);

        return encryptionPhoneCheckList.stream()
                .filter(r -> DateUtil.isEffectiveDate(r.getCreateTime(), thirtyDaysAgoDate, nowDate))
                .max(Comparator.comparing(EncryptionPhoneCheck::getCreateTime));
    }

    @Override
    public CuleApplyResult culeApply(CuleApplyCommand command) throws ClueResubmissionException, AreaInadmissibleException {

        CuleApplyResult culeApplyResult = culeDomainService.culeApply(command);

        BranchLoanId branchLoanId = culeApplyResult.getBranchLoanId();
        this.sendToTelemarketing(branchLoanId);

        return culeApplyResult;
    }

    @Override
    public ClueStatusNotifyDTO queryClue(AntQueryCluePreDTO queryDTO) {
        ClueStatusNotifyDTO input = new ClueStatusNotifyDTO();
        input.setApplyNo(queryDTO.getApplyNo());
        LoanStatusChangeEnum realTimeStatusEnum = LoanStatusChangeEnum.getEnumsByValue(queryDTO.getStatus());

        ClueApproveInfoDto clueApproveInfo = clueApproveInfoService.getClueApproveInfo(queryDTO.getApplyNo());
        if (clueApproveInfo != null) {
            input.setIncomeProofNeeded(clueApproveInfo.getIncomeProofNeeded());
            input.setInitialFinalJointReview(clueApproveInfo.getInitialFinalJointReview());
            if ("1".equals(clueApproveInfo.getIncomeProofNeeded())) {
                List<SupplementCategoryInfo> list = new ArrayList<>();
                list.add(new SupplementCategoryInfo(clueApproveInfo.getAdditionalAmountRequired()));
                input.setSupplementCategoryInfo(list);
            }
            if ("1".equals(clueApproveInfo.getInitialFinalJointReview())) {
                if (LoanStatusChangeEnum.MERGE_APPROVE_PASS_F.equals(realTimeStatusEnum)) {
                    realTimeStatusEnum = LoanStatusChangeEnum.PRE_PASS;
                } else if (LoanStatusChangeEnum.MERGE_APPROVE_PASS_T.equals(realTimeStatusEnum)) {
                    realTimeStatusEnum = LoanStatusChangeEnum.APPROVE_PASS;
                } else if (LoanStatusChangeEnum.MERGE_APPROVE_REJECT_T.equals(realTimeStatusEnum)) {
                    realTimeStatusEnum = LoanStatusChangeEnum.APPROVE_REJECT;
                }
            }
        }

        if ("ANT".equals(queryDTO.getSource())) {
            DirectCustomerStatusDto directCustomerStatusDto = splBussinessbasicMapper.selectStatusByOrderId(
                    queryDTO.getApplyNo(),
                    6
            );
            if (directCustomerStatusDto == null) {
                return input;
            }
            realTimeStatusEnum = LoanStatusChangeEnum.getEnumsByValue(directCustomerStatusDto.getSplMaxActionNum());
        }
        if ("KP".equals(queryDTO.getSource())) {
            DirectCustomerStatusDto directCustomerStatusDto = splBussinessbasicMapper.selectStatusByOrderId(
                    queryDTO.getApplyNo(),
                    28
            );
            if (directCustomerStatusDto == null) {
                return input;
            }
            realTimeStatusEnum = LoanStatusChangeEnum.getEnumsByValue(directCustomerStatusDto.getSplMaxActionNum());
        }
        if (realTimeStatusEnum == null) {
            return input;
        }

        input.setStatus(realTimeStatusEnum.getValue());

        if (LoanStatusChangeEnum.WILLING == realTimeStatusEnum) {

            return input;
        }
        CreditAuthorization credit = new CreditAuthorization();
        credit.setBoId(queryDTO.getApplyNo());
        credit.setAuthorizationStatus(1);
        List<CreditAuthorization> creditAuthorizationList = creditAuthorizationRepository.list(credit);

        List<String> applicationNos = creditAuthorizationList.stream()
                .map(CreditAuthorization::getApplicationNo).collect(Collectors.toList());
        List<AntCreditProductDTO> antCreditProductDTOList = new ArrayList<>();

        if (!applicationNos.isEmpty()) {
            List<CreditLoan> creditLoans = creditLoanRepository.searchCreditDataByApplicationNo(applicationNos);
            if (!creditLoans.isEmpty()) {
                for (CreditLoan creditLoan : creditLoans) {
                    if ("98".equals(creditLoan.getSelectCode()) &&
                            ("1".equals(creditLoan.getBeforeApproveConclusion()) || "2".equals(creditLoan.getBeforeApproveConclusion()) || "4".equals(creditLoan.getBeforeApproveConclusion()))) {
                        DirectCustomerStatusDto directCustomerStatus = new DirectCustomerStatusDto();
                        directCustomerStatus.setSplApplyNum(queryDTO.getApplyNo());
                        AntCreditProductDTO productDTO = getPrompt(creditLoan);
                        Date screeningTime = creditLoan.getScreeningTime();
                        if (screeningTime == null) {
                            screeningTime = creditLoan.getCreateTime();
                        }
                        if (screeningTime != null) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(screeningTime);
                            calendar.add(Calendar.DAY_OF_MONTH, 30);
                            productDTO.setExpireTime(DateUtil.dateToStrLong(calendar.getTime()));
                        }
                        antCreditProductDTOList.add(productDTO);
                    }
                }
            }
        }

        if (LoanStatusChangeEnum.PRE_PASS == realTimeStatusEnum ||
                LoanStatusChangeEnum.AUTO_PRE_PASS == realTimeStatusEnum ||
                LoanStatusChangeEnum.MAN_PRE_PASS == realTimeStatusEnum) {
            List<CreditDTO> creditList = new ArrayList<>();
            if (!antCreditProductDTOList.isEmpty()) {
                for (AntCreditProductDTO antCreditProductDTO : antCreditProductDTOList) {
                    CreditDTO creditDTO = new CreditDTO();
                    creditDTO.setProductCode("VMP");
                    creditDTO.setCreditAmt(antCreditProductDTO.prompt());
                    creditDTO.setExpireTime(antCreditProductDTO.getExpireTime());
                    creditDTO.setLoanType("1");

                    ArrayList<CreditPricingDTO> creditPricingList = new ArrayList<>();
                    CreditPricingDTO creditPricingDTO = new CreditPricingDTO();
                    creditPricingDTO.setIntRate(antCreditProductDTO.getRate());
                    creditPricingDTO.setLoanTerm(Long.valueOf(antCreditProductDTO.getTerm()));
                    creditPricingDTO.setLoanTermUnit("M");
                    creditPricingDTO.setRepayType("2");
                    creditPricingList.add(creditPricingDTO);

                    creditDTO.setCreditPricingList(creditPricingList);
                    creditList.add(creditDTO);
                }
                input.setCreditList(creditList);

            }

            return input;
        }

        if (LoanStatusChangeEnum.PRE_REJECT == realTimeStatusEnum || LoanStatusChangeEnum.PRE_CANCEL.equals(realTimeStatusEnum)
                || LoanStatusChangeEnum.AUTO_PRE_REJECT == realTimeStatusEnum || LoanStatusChangeEnum.MAN_PRE_REJECT == realTimeStatusEnum) {
            input.setRefuseCode(RefuseReasonType.RR_SYS_OTHER.name());
            input.setRefuseMsg(RefuseReasonType.RR_SYS_OTHER.getReason());
        }

        DirectCustomerStatusDto directCustomerStatusDto = splBussinessbasicMapper.selectStatusByOrderId(
                queryDTO.getApplyNo(),
                6
        );

        if (LoanStatusChangeEnum.APPROVE_PASS == realTimeStatusEnum) {
            if (directCustomerStatusDto == null) {
                List<CreditDTO> creditList = new ArrayList<>();
                if (!antCreditProductDTOList.isEmpty()) {
                    for (AntCreditProductDTO antCreditProductDTO : antCreditProductDTOList) {
                        CreditDTO creditDTO = new CreditDTO();
                        creditDTO.setProductCode("VMP");
                        creditDTO.setCreditAmt(antCreditProductDTO.prompt());
                        creditDTO.setExpireTime(antCreditProductDTO.getExpireTime());
                        creditDTO.setLoanType("1");

                        ArrayList<CreditPricingDTO> creditPricingList = new ArrayList<>();
                        CreditPricingDTO creditPricingDTO = new CreditPricingDTO();
                        creditPricingDTO.setIntRate(antCreditProductDTO.getRate());
                        creditPricingDTO.setLoanTerm(Long.valueOf(antCreditProductDTO.getTerm()));
                        creditPricingDTO.setLoanTermUnit("M");
                        creditPricingDTO.setRepayType("2");
                        creditPricingList.add(creditPricingDTO);

                        creditDTO.setCreditPricingList(creditPricingList);
                        creditList.add(creditDTO);
                    }
                    input.setCreditList(creditList);
                }

                return input;
            }

            BankApprovalRecordPo bankApprovalRecord = bankApprovalRecordService.getBankApprovalRecord(directCustomerStatusDto.getSplLoanId());

            MultipleLoanObject loanInfo = loanInfoRepository.getLoanIdByMainLoanId(directCustomerStatusDto.getSplLoanId());
            BigDecimal commonRate = loanInfo.getM_commonRate() == null ? null : new BigDecimal(loanInfo.getM_commonRate().toString());

            List<CreditDTO> creditList = new ArrayList<>();
            if (!antCreditProductDTOList.isEmpty()) {
                for (AntCreditProductDTO antCreditProductDTO : antCreditProductDTOList) {
                    CreditDTO creditDTO = new CreditDTO();
                    creditDTO.setProductCode("VMP");
                    creditDTO.setCreditAmt(
                            bankApprovalRecord != null
                                    ? bankApprovalRecord.getApplyLimit() == null ? 0L : new BigDecimal(bankApprovalRecord.getApplyLimit()).longValue()
                                    : antCreditProductDTO.prompt());

                    creditDTO.setExpireTime(antCreditProductDTO.getExpireTime());
                    creditDTO.setLoanType("1");

                    ArrayList<CreditPricingDTO> creditPricingList = new ArrayList<>();
                    CreditPricingDTO creditPricingDTO = new CreditPricingDTO();
                    creditPricingDTO.setIntRate(commonRate == null ? antCreditProductDTO.getRate() : commonRate.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                    creditPricingDTO.setLoanTerm(loanInfo.getM_term() == null ? Long.valueOf(antCreditProductDTO.getTerm()) : Long.valueOf(loanInfo.getM_term()));
                    creditPricingDTO.setLoanTermUnit("M");
                    creditPricingDTO.setRepayType("2");
                    creditPricingList.add(creditPricingDTO);

                    creditDTO.setCreditPricingList(creditPricingList);
                    creditList.add(creditDTO);
                }
                input.setCreditList(creditList);
            }
            return input;
        }

        if (LoanStatusChangeEnum.APPROVE_REJECT.equals(realTimeStatusEnum) ||
                LoanStatusChangeEnum.SIGN_REJECT.equals(realTimeStatusEnum) ||
                LoanStatusChangeEnum.LEND_APPLY_REJECT.equals(realTimeStatusEnum) ||
                LoanStatusChangeEnum.LEND_APPROVE_REJECT.equals(realTimeStatusEnum) ||
                LoanStatusChangeEnum.LEND_REJECT.equals(realTimeStatusEnum)) {

            if (directCustomerStatusDto == null) {
                input.setRefuseCode("RR_SYS_OTHER");
                input.setRefuseMsg("审批拒绝");
                return input;
            }
            if (directCustomerStatusDto.getSplLoanId() == null) {
                input.setRefuseCode("RR_SYS_OTHER");
                input.setRefuseMsg("审批拒绝");

                return input;
            }

            SapDcsLoanAppStatusObject sapDcsLoanAppStatusObject = sapDcsLoanAppStatusRepository.selectById(directCustomerStatusDto.getSplLoanId());
            if (sapDcsLoanAppStatusObject == null) {
                input.setRefuseCode("RR_SYS_OTHER");
                input.setRefuseMsg("审批拒绝");

                return input;
            }
            List<ApproveVo> approveList = approveRepository.list(directCustomerStatusDto.getSplLoanId());
            if (approveList == null || approveList.isEmpty()) {
                input.setRefuseCode("RR_SYS_OTHER");
                input.setRefuseMsg("审批拒绝");

                return input;
            }
            ApproveVo approve = null;
            if (LoanStatusChangeEnum.APPROVE_REJECT.equals(realTimeStatusEnum)) {
                Optional<ApproveVo> approveVoOptional = approveList.stream()
                        .filter(r -> "1".equals(r.getApproveType()))
                        .filter(r -> "3".equals(r.getConclusion()))
                        .findFirst();
                approve = approveVoOptional.orElse(null);
            } else if (LoanStatusChangeEnum.SIGN_REJECT.equals(realTimeStatusEnum)) {
                Optional<ApproveVo> approveVoOptional = approveList.stream()
                        .filter(r -> "4".equals(r.getApproveType()))
                        .filter(r -> "2".equals(r.getConclusion()))
                        .findFirst();
                approve = approveVoOptional.orElse(null);
            } else if (LoanStatusChangeEnum.LEND_APPLY_REJECT.equals(realTimeStatusEnum)) {
                Optional<ApproveVo> approveVoOptional = approveList.stream()
                        .filter(r -> "2".equals(r.getApproveType()))
                        .filter(r -> "2".equals(r.getConclusion()))
                        .findFirst();
                approve = approveVoOptional.orElse(null);
            } else if (LoanStatusChangeEnum.LEND_APPROVE_REJECT.equals(realTimeStatusEnum)) {
                Optional<ApproveVo> approveVoOptional = approveList.stream()
                        .filter(r -> "3".equals(r.getApproveType()))
                        .filter(r -> "2".equals(r.getConclusion()))
                        .findFirst();
                approve = approveVoOptional.orElse(null);
            } else {
                Optional<ApproveVo> approveVoOptional = approveList.stream()
                        .filter(r -> "2".equals(r.getApproveType()) || "3".equals(r.getApproveType()) || "4".equals(r.getApproveType()))
                        .filter(r -> "2".equals(r.getConclusion()))
                        .findFirst();
                approve = approveVoOptional.orElse(null);
            }

            if (approve == null) {
                input.setRefuseCode("RR_SYS_OTHER");
                input.setRefuseMsg("审批拒绝");

                return input;
            }

            if (LoanStatusChangeEnum.APPROVE_REJECT.equals(realTimeStatusEnum)) {
                if (new Integer(-110).equals(sapDcsLoanAppStatusObject.getM_Status())
                        || new Integer(-130).equals(sapDcsLoanAppStatusObject.getM_Status())
                        || new Integer(-200).equals(sapDcsLoanAppStatusObject.getM_Status())) {

                    ApproveRejectReasonZlhjToAntEnum antEnum =
                            ApproveRejectReasonZlhjToAntEnum.getValueByZlhjCode(approve.getRejectReason());
                    if (antEnum == null) {
                        input.setRefuseCode("RR_SYS_OTHER");
                        input.setRefuseMsg("审批拒绝");

                        return input;
                    }

                    input.setRefuseCode(antEnum.getAntCode());
                    input.setRefuseMsg(antEnum.getValue());

                    return input;

                } else if (new Integer(-135).equals(sapDcsLoanAppStatusObject.getM_Status())
                        || new Integer(-160).equals(sapDcsLoanAppStatusObject.getM_Status())) {
                    input.setRefuseCode("RR_SCORE_THIRD_CREDIT");
                    input.setRefuseMsg("资方信用评分不足");

                    return input;
                } else if (new Integer(-90).equals(sapDcsLoanAppStatusObject.getM_Status())) {
                    input.setRefuseCode("RR_BASIC_INFO_ERROR");
                    input.setRefuseMsg("信息录入有误");

                    return input;
                } else {
                    input.setRefuseCode("RR_SYS_OTHER");
                    input.setRefuseMsg("审批拒绝");

                    return input;
                }
            } else {
                if (new Integer(-210).equals(sapDcsLoanAppStatusObject.getM_Status())
                        || new Integer(-230).equals(sapDcsLoanAppStatusObject.getM_Status())
                        || new Integer(-235).equals(sapDcsLoanAppStatusObject.getM_Status())) {

                    LendRejectReasonZlhjToAntEnum antEnum = LendRejectReasonZlhjToAntEnum.getValueByZlhjCode(approve.getApproveReason());
                    if (antEnum == null) {
                        input.setRefuseCode("RR_SYS_OTHER");
                        input.setRefuseMsg("审批拒绝");

                        return input;
                    }

                    input.setRefuseCode(antEnum.getAntCode());
                    input.setRefuseMsg(antEnum.getValue());

                    return input;

                }
            }

            input.setRefuseCode("RR_SYS_OTHER");
            input.setRefuseMsg("审批拒绝");
            return input;
        }

        if (LoanStatusChangeEnum.APPROVE_CANCEL.equals(realTimeStatusEnum)) {
            input.setRefuseCode("RR_SYS_OTHER");
            input.setRefuseMsg("审批拒绝");

            return input;
        }
        if (LoanStatusChangeEnum.SIGN_CANCEL.equals(realTimeStatusEnum) ||
                LoanStatusChangeEnum.LEND_APPLY_CANCEL.equals(realTimeStatusEnum) ||
                LoanStatusChangeEnum.LEND_APPROVE_CANCEL.equals(realTimeStatusEnum)) {
            input.setRefuseCode("RR_SYS_OTHER");
            input.setRefuseMsg("放款取消");

            return input;
        }

        if (LoanStatusChangeEnum.LEND_SUC.equals(realTimeStatusEnum)) {
            if (directCustomerStatusDto == null) {
                List<CreditDTO> creditList = new ArrayList<>();
                if (!antCreditProductDTOList.isEmpty()) {
                    for (AntCreditProductDTO antCreditProductDTO : antCreditProductDTOList) {
                        CreditDTO creditDTO = new CreditDTO();
                        creditDTO.setProductCode("VMP");
                        creditDTO.setCreditAmt(antCreditProductDTO.prompt());
                        creditDTO.setExpireTime(antCreditProductDTO.getExpireTime());
                        creditDTO.setLoanType("1");

                        ArrayList<CreditPricingDTO> creditPricingList = new ArrayList<>();
                        CreditPricingDTO creditPricingDTO = new CreditPricingDTO();
                        creditPricingDTO.setIntRate(antCreditProductDTO.getRate());
                        creditPricingDTO.setLoanTerm(Long.valueOf(antCreditProductDTO.getTerm()));
                        creditPricingDTO.setLoanTermUnit("M");
                        creditPricingDTO.setRepayType("2");
                        creditPricingList.add(creditPricingDTO);

                        creditDTO.setCreditPricingList(creditPricingList);
                        creditList.add(creditDTO);
                    }
                    input.setCreditList(creditList);
                }
                return input;
            }


            Integer loanId = directCustomerStatusDto.getSplLoanId();
            BankApprovalRecordPo bankApprovalRecord = bankApprovalRecordService.getBankApprovalRecord(directCustomerStatusDto.getSplLoanId());

            MultipleLoanObject loanInfo = loanInfoRepository.getLoanIdByMainLoanId(loanId);

            BankHeadOfficeVo bankHeadOffice = Optional.ofNullable(loanInfo)
                    .map(r -> bankHeadOfficeMapper.getBankHeadOfficeByOrgId(ConvertionUtil.getSimpleIntegerWithNull(r.getM_selectBank())))
                    .orElse(null);

            if (loanInfo != null) {
                BigDecimal commonRate = loanInfo.getM_commonRate() == null ? BigDecimal.ZERO : new BigDecimal(loanInfo.getM_commonRate().toString());
                BigDecimal applyMoney = new BigDecimal(loanInfo.getM_applyMoney().toString());
                input.setCreditAmt(applyMoney);
                if (bankHeadOffice != null) {
                    input.setFinOrg(bankHeadOffice.getBaOrgName());
                }
                input.setLoanRate(commonRate.setScale(2, BigDecimal.ROUND_HALF_UP));
                input.setLoanTerm(loanInfo.getM_term());
                input.setLoanTermUnit("M");
                input.setRepayType("2");
                input.setOrgDrawdownNo(loanInfo.getM_loanNumber());
                // 是否需要租赁放款
                boolean isleasinglending = "1".equals(Tool.searchIsleasinglending(loanId));
                MainLeasing mainLeasing = mainLeasingRepository.getDataByLoanId(loanId);
                if (isleasinglending) {
                    ExhibitionAndPayInfoVo exhibitionAndPayInfoVo = exhibitionAndPayInfoRepository.getObject(loanId);
                    if (mainLeasing != null) {
                        if (null != mainLeasing.getGrantMoneyDate()) {
                            Date loanDate = DateUtil.strToDate1(mainLeasing.getGrantMoneyDate());
                            input.setLoanDate(DateUtil.dateToStrLong(loanDate));
                        }
                    }
                    if (exhibitionAndPayInfoVo != null) {
                        input.setLoanAmt(exhibitionAndPayInfoVo.getM_payAmount());
                    }
                } else {
                    if (null != loanInfo.getM_grantMoneyDate()) {
                        Date loanDate = DateUtil.strToDate1(loanInfo.getM_grantMoneyDate());
                        input.setLoanDate(DateUtil.dateToStrLong(loanDate));
                    }
                    input.setLoanAmt(applyMoney);
                }

                List<CreditDTO> creditList = new ArrayList<>();
                if (!antCreditProductDTOList.isEmpty()) {
                    for (AntCreditProductDTO antCreditProductDTO : antCreditProductDTOList) {
                        CreditDTO creditDTO = new CreditDTO();
                        creditDTO.setProductCode("VMP");
                        creditDTO.setCreditAmt(bankApprovalRecord != null
                                ? bankApprovalRecord.getApplyLimit() == null ? 0L : new BigDecimal(bankApprovalRecord.getApplyLimit()).longValue()
                                : antCreditProductDTO.prompt());

                        creditDTO.setExpireTime(antCreditProductDTO.getExpireTime());
                        creditDTO.setLoanType("1");

                        ArrayList<CreditPricingDTO> creditPricingList = new ArrayList<>();
                        CreditPricingDTO creditPricingDTO = new CreditPricingDTO();
                        creditPricingDTO.setIntRate(commonRate.equals(BigDecimal.ZERO) ? antCreditProductDTO.getRate() : commonRate.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                        creditPricingDTO.setLoanTerm(loanInfo.getM_term() == null ? Long.valueOf(antCreditProductDTO.getTerm()) : Long.valueOf(loanInfo.getM_term()));
                        creditPricingDTO.setLoanTermUnit("M");
                        creditPricingDTO.setRepayType("2");
                        creditPricingList.add(creditPricingDTO);

                        creditDTO.setCreditPricingList(creditPricingList);
                        creditList.add(creditDTO);
                    }
                    input.setCreditList(creditList);
                }

            }
            return input;
        }

        return input;
    }

    public AntCreditProductDTO getPrompt(CreditLoan creditLoan) {
        //查询征信结果表
        String prompt = "";
        AntCreditProductMapping antCreditProductMapping = null;
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
                List<AntCreditProductMapping> list = antCreditProductMappingRepository.getList();

                JSONObject showArea = (JSONObject) decisionRuleGroupResultList.get(0);
                //不包含showarea为老逻辑数据按老逻辑处理，包含showarea为新逻辑数据按新逻辑处理
                if (showArea.getString("showArea") != null) {
                    try {
                        JSONObject jsonObject = creditLoanService.analysisPreApproveResult(parseObject, creditLoan.getCreditOrderId(), -1);
                        List<String> CRRuleTip = (List<String>) jsonObject.get("CRRuleTip");
                        List<String> RuleGroupSentence = (List<String>) jsonObject.get("RuleGroupSentence");
                        for (String rule : CRRuleTip) {
                            if (rule.contains("授信额度")) {
                                prompt = rule;
                                break;
                            }
                        }
                        for (String rule : RuleGroupSentence) {
                            antCreditProductMapping = list.stream()
                                    .filter(r -> rule.contains(r.getProductName()))
                                    .findFirst().orElse(null);

                            if (antCreditProductMapping != null) {
                                break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (StringUtil.isNotEmpty(prompt)) {
            prompt = prompt.substring(5);
            BigDecimal finalMoney = new BigDecimal("0.0");
            try {
                finalMoney = new BigDecimal(prompt);
                prompt = finalMoney.toString();
            } catch (Exception e) {
                finalMoney = new BigDecimal("0.0");
                prompt = finalMoney.toString();
            }
        } else {
            prompt = new BigDecimal("0.0").toString();
        }
        if (antCreditProductMapping == null) {
            String emailContent = "发送蚂蚁机构初审数据未匹配到规则引擎产品,当前单号为：" +
                    creditLoan.getApplyNum() +
                    "\n" +
                    "请知悉！";
            sendEmailMessage.sendEmailByTypeForAnt("蚂蚁机构初审数据未匹配到规则引擎产品", emailContent, "15");
            return new AntCreditProductDTO(prompt, "0.24", "48");
        } else {
            return new AntCreditProductDTO(
                    prompt,
                    antCreditProductMapping.getLoanRate(),
                    antCreditProductMapping.getLoanTerm());
        }
    }

    @Override
    public void productNameStore(String decisionRules,CreditLoan creditLoan) {

        if(!"98".equals(creditLoan.getSelectCode())){
            return;
        }

        JSONObject parseObject = JSONObject.parseObject(decisionRules);
        JSONArray decisionRuleGroupResultList = parseObject.getJSONArray("decisionRuleGroupResultList");

        if (decisionRuleGroupResultList.size() > 0) {
            JSONObject showArea = (JSONObject) decisionRuleGroupResultList.get(0);
            //不包含showarea为老逻辑数据按老逻辑处理，包含showarea为新逻辑数据按新逻辑处理
            if (showArea.getString("showArea") != null) {
                try {
                    JSONObject jsonObject = creditLoanService.analysisPreApproveResult(parseObject, creditLoan.getCreditOrderId(), -1);
                    List<String> RuleGroupSentence = (List<String>) jsonObject.get("RuleGroupSentence");

                    Boolean flag = true;
                    String regex = ruleProductRepository.getRuleProduct().getProductName();
                    Pattern TARGET_PATTERN = Pattern.compile(regex);
                    for(String rule : RuleGroupSentence){
                        Matcher matcher = TARGET_PATTERN.matcher(rule);
                        if(matcher.find()){
                            flag = false;
                            creditLoan.setProductName(rule);
                            creditLoanRepository.store(creditLoan);
                            break;
                        }
                    }
                    if(flag){
                        //发企微消息
                        SqlSessionFactory sessionFactory = DbSqlSessionFactory.getSqlSessionFactory();
                        try (SqlSession sqlSession = sessionFactory.openSession(true)) {
                            AllMapper mapper = sqlSession.getMapper(AllMapper.class);
                            //查询所有结清工单管理人员
                            List<Map<String, Object>> mapList = mapper.getUserBySignageType("18");

                            if (mapList != null && mapList.size() > 0) {
                                for (int i = 0; i < mapList.size(); i++) {
                                    // 发送对象手机号
                                    String phone = ConvertionUtil.getSimpleStringWithNull(mapList.get(i).get("PHONE"));
                                    Optional<CreditUserVO> creditUser = creditUserVORepository.getCreditUserByCreditOrderId(creditLoan.getOrderId());
                                    String emailContent = "提醒，"+creditUser.get().getUserName()+"客户的产品提示异常";
                                    WeChatMessagePushDTO weChatMessagePushDTO = new WeChatMessagePushDTO(phone, emailContent);
                                    weChatMessagePushService.pushWeChatMessage(weChatMessagePushDTO);
                                }
                            }
                            throw new SettleOrderNoticeUserException("未找到风控产品名称埋点异常推送对象配置信息，请查看T_OPERATOR_SETTING表数据");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public ClueStatusNotifyDTO queryClueForOrgApprove(AntQueryCluePreDTO preDTO) {

        ClueStatusNotifyDTO input = new ClueStatusNotifyDTO();
        input.setApplyNo(preDTO.getApplyNo());
        ClueApproveInfoDto clueApproveInfo = clueApproveInfoService.getClueApproveInfo(preDTO.getApplyNo());
        if (clueApproveInfo != null) {
            LoanStatusChangeEnum realTimeStatusEnum = LoanStatusChangeEnum.getEnumsByValue(preDTO.getStatus());
            input.setIncomeProofNeeded(clueApproveInfo.getIncomeProofNeeded());
            input.setInitialFinalJointReview(clueApproveInfo.getInitialFinalJointReview());
            if ("1".equals(clueApproveInfo.getIncomeProofNeeded())) {
                List<SupplementCategoryInfo> list = new ArrayList<>();
                list.add(new SupplementCategoryInfo(clueApproveInfo.getAdditionalAmountRequired()));
                input.setSupplementCategoryInfo(list);
            }
            if ("1".equals(clueApproveInfo.getInitialFinalJointReview())) {
                if (LoanStatusChangeEnum.MERGE_APPROVE_PASS_F.equals(realTimeStatusEnum)) {
                    preDTO.setStatus(LoanStatusChangeEnum.PRE_PASS.getValue());
                }
            }
        }

        if (LoanStatusChangeEnum.PRE_PASS.getValue().equals(preDTO.getStatus()) ||
                LoanStatusChangeEnum.AUTO_PRE_PASS.getValue().equals(preDTO.getStatus()) ||
                LoanStatusChangeEnum.MAN_PRE_PASS.getValue().equals(preDTO.getStatus())) {
            input.setStatus(preDTO.getStatus());

            CreditAuthorization credit = new CreditAuthorization();
            credit.setBoId(preDTO.getApplyNo());
            credit.setAuthorizationStatus(1);
            List<CreditAuthorization> creditAuthorizationList = creditAuthorizationRepository.list(credit);

            List<String> applicationNos = creditAuthorizationList.stream()
                    .map(CreditAuthorization::getApplicationNo).collect(Collectors.toList());
            List<AntCreditProductDTO> antCreditProductDTOList = new ArrayList<>();

            if (!applicationNos.isEmpty()) {
                List<CreditLoan> creditLoans = creditLoanRepository.searchCreditDataByApplicationNo(applicationNos);
                if (!creditLoans.isEmpty()) {
                    for (CreditLoan creditLoan : creditLoans) {
                        if ("98".equals(creditLoan.getSelectCode())
                                && ("1".equals(creditLoan.getBeforeApproveConclusion()) || "2".equals(creditLoan.getBeforeApproveConclusion()) || "4".equals(creditLoan.getBeforeApproveConclusion()))) {
                            DirectCustomerStatusDto directCustomerStatus = new DirectCustomerStatusDto();
                            directCustomerStatus.setSplApplyNum(preDTO.getApplyNo());
                            AntCreditProductDTO productDTO = getPrompt(creditLoan);
                            Date screeningTime = creditLoan.getScreeningTime();
                            if (screeningTime == null) {
                                screeningTime = creditLoan.getCreateTime();
                            }
                            if (screeningTime != null) {
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(screeningTime);
                                calendar.add(Calendar.DAY_OF_MONTH, 30);
                                productDTO.setExpireTime(DateUtil.dateToStrLong(calendar.getTime()));
                            }
                            antCreditProductDTOList.add(productDTO);
                        }
                    }
                }
            }


            List<CreditDTO> creditList = new ArrayList<>();
            if (!antCreditProductDTOList.isEmpty()) {
                for (AntCreditProductDTO antCreditProductDTO : antCreditProductDTOList) {
                    CreditDTO creditDTO = new CreditDTO();
                    creditDTO.setProductCode("VMP");
                    creditDTO.setCreditAmt(antCreditProductDTO.prompt());
                    creditDTO.setExpireTime(antCreditProductDTO.getExpireTime());
                    creditDTO.setLoanType("1");

                    ArrayList<CreditPricingDTO> creditPricingList = new ArrayList<>();
                    CreditPricingDTO creditPricingDTO = new CreditPricingDTO();
                    creditPricingDTO.setIntRate(antCreditProductDTO.getRate());
                    creditPricingDTO.setLoanTerm(Long.valueOf(antCreditProductDTO.getTerm()));
                    creditPricingDTO.setLoanTermUnit("M");
                    creditPricingDTO.setRepayType("2");
                    creditPricingList.add(creditPricingDTO);

                    creditDTO.setCreditPricingList(creditPricingList);
                    creditList.add(creditDTO);
                }
                input.setCreditList(creditList);

            }

            return input;
        }
        if (LoanStatusChangeEnum.PRE_REJECT.getValue().equals(preDTO.getStatus()) || LoanStatusChangeEnum.AUTO_PRE_REJECT.getValue().equals(preDTO.getStatus())
                || LoanStatusChangeEnum.MAN_PRE_REJECT.getValue().equals(preDTO.getStatus())) {
            input.setStatus(preDTO.getStatus());
            RefuseReasonType refuseReasonType = RefuseReasonType.RR_SYS_OTHER;
            input.setRefuseCode(refuseReasonType.name());
            input.setRefuseMsg(refuseReasonType.getReason());
        }
        return input;
    }

    /**
     * 线索同步电销系统
     */
    private void sendToTelemarketing(BranchLoanId branchLoanId) {

        if (branchLoanId == null) {
            return;
        }

        TreeMap<String, Object> resMap = new TreeMap<>();
        resMap.put("BRANCHID", branchLoanId.getValue());
        resMap.put("RESULT", 1);
        resMap.put("MESSAGE", "成功");
        interfaceDatum.synchronizationBusinessOpportunity(resMap);
    }


    @Override
    public void antNotSubmitSchedule() {
        log.info("合作渠道=16-蚂蚁金服-30天超期定时任务开始：[{}]", DateUtil.formatToYYYYMMDDHHMMSS2(new Date()));
        //获取30天前的日期
        String date = DateUtil.getNowBeforeDate(30);
        log.info("合作渠道=16-蚂蚁金服-30天超期定时任务，实际执行的创建时间[{}]", date);

        //筛选线索进件时间超过30天、没有进件提交线索
        List<String> applyNums = splBussinessbasicMapper.selectAntNotSubmitList(date);
        log.info("筛选线索进件时间超过30天、没有进件提交线索:[{}]", applyNums);
        if (!applyNums.isEmpty()) {
            for (String applyNum : applyNums) {
                //查询该线索进件业务的当前贷款状态,若有多笔贷款取贷款状态最大的该笔
                List<Map<String, Object>> maxLoanStatus = splBussinessbasicMapper.getMaxLoanStatus(applyNum, 16);
                DirectCustomerStatusDto directCustomerStatusDto = splBussinessbasicMapper.selectStatusByOrderId(applyNum, Transform.getChannelPartner());
                String maxActionNum = ConvertionUtil.getSimpleStringWithNull(directCustomerStatusDto.getSplMaxActionNum());
                if (maxLoanStatus != null && !maxLoanStatus.isEmpty()) {
                    //如果有多条，则取第一条
                    Map<String, Object> maxStatus = maxLoanStatus.get(0);
                    //sapdcslas表中汇总贷款id
                    Integer loanId = ConvertionUtil.getSimpleIntegerWithNull(maxStatus.get("loanId"));
                    //sapdcslas表中状态
                    int status = ConvertionUtil.getSimpleIntegerWithNull(maxStatus.get("status"));

                    LoanStatusChangeEnum loanStatusChangeEnum = null;
                    if (status >= 245) {
                        unifiedInputPlatformService.realTimeInteraction(new LoanStatePushToClueDTO(applyNum, loanId, LoanStatusChangeEnum.LEND_SUC.getValue(), 16));
                    } else if (status < 0) {
                        loanStatusChangeEnum = LoanStatusChangeEnum.valueOf(maxActionNum).nextReject();
                        unifiedInputPlatformService.realTimeInteraction(new LoanStatePushToClueDTO(applyNum, loanId, loanStatusChangeEnum.getValue(), 16));
                        maxLoanStatus.remove(0);
                        if (!maxLoanStatus.isEmpty()) {
                            maxLoanStatus.forEach(r -> {
                                Integer id = Integer.valueOf(r.get("loanId").toString());
                                Integer status2 = Integer.valueOf(r.get("status").toString());
                                //其他状态做系统取消处理，取消原因：线索超期取消。取消后调用接口，按当前状态拼装参数
                                loanAppStatusService.updateState(id, -15, status2);
                                loanAppStatusService.saveNode(id, -15, status2, 0, "线索超期取消", 4);
                                //添加节点
                                loanAppStatusService.saveNode(id, -15, -15, 0, "线索超期取消", 4);
                            });
                        }

                        List<BusinessBasicEntity> basicEntity = splBussinessbasicMapper.getBusinessMaxLoanStatusByApplyNum(applyNum, 16);

                        basicEntity.forEach(r -> {
                            //受理阶段做取消操作
                            if (r.getLoanState() == 0 || r.getLoanState() == 3) {
                                splBussinessbasicMapper.updateSplBussinessBasic(9, "线索超期取消", r.getBranchLoanId());
                            }
                        });

                    }
                } else {
                    List<BusinessBasicEntity> basicEntity = splBussinessbasicMapper.getBusinessMaxLoanStatusByApplyNum(applyNum, 16);
                    if (basicEntity != null && !basicEntity.isEmpty()) {
                        unifiedInputPlatformService.realTimeInteraction(new LoanStatePushToClueDTO(applyNum, null, LoanStatusChangeEnum.valueOf(maxActionNum).nextReject().getValue(), 16));
                    }
                    basicEntity.forEach(r -> {
                        //受理阶段做取消操作
                        if (r.getLoanState() == 0 || r.getLoanState() == 3) {
                            splBussinessbasicMapper.updateSplBussinessBasic(9, "线索超期取消", r.getBranchLoanId());
                        }
                    });
                }

            }
        } else {
            log.info("合作渠道=16-蚂蚁金服的线索-30天超期定时任务,查询无数据，不需要处理");
        }
    }

    @Override
    public void handleSHA256() {

        Timestamp startTime = new Timestamp(System.currentTimeMillis());
        System.out.println("SHA256数据处理开始: " + startTime);

        List<EncryptionPhoneCheck> encryptionPhoneCheckList = encryptionPhoneCheckRepository.selectLimit(1000);
        while (!encryptionPhoneCheckList.isEmpty()) {
            try {
                encryptionPhoneCheckList.forEach(r -> r.setSha256PhoneNumber(SHA256Encryptor.encrypt(r.getOriginalPhoneNumber())));
                encryptionPhoneCheckRepository.store(encryptionPhoneCheckList);
                encryptionPhoneCheckList = encryptionPhoneCheckRepository.selectLimit(1000);
            } catch (Exception e) {
                log.info("SHA256数据处理异常：", e);
            }
        }

        Timestamp endTime = new Timestamp(System.currentTimeMillis());
        System.out.println("SHA256数据处理结束: " + endTime);

    }

    @Override
    public List<AntRepaymentPlanDTO> queryRepayPlan(ClueNumber clueNumber) {
        DirectCustomerStatusDto directCustomerStatusDto = splBussinessbasicMapper.selectStatusByOrderIdOrBoId(
                clueNumber.getNumber(),
                6
        );
        if (directCustomerStatusDto == null || directCustomerStatusDto.getSplLoanId() == null) {
            return new ArrayList<>();
        }
        LoanId loanId = new LoanId(directCustomerStatusDto.getSplLoanId());

        return antRepaymentPlanFactory.create(loanId);
    }

    @Override
    public void repaymentPlanChangeNotify() {
        List<AntRepaymentChangeNotifyDTO> antRepayPlanChangeData = splBussinessbasicMapper.antRepayPlanChangeData();
        if (antRepayPlanChangeData.isEmpty()) {
            return;
        }
        antRepayPlanChangeData.forEach(sender::repaymentChangePush);
    }

    @Override
    public void antPreApprove(AntPreApproveMessage message) {
        try {
            OrgUserVo orgUserVo = userObjectRepository.getByUserId(Integer.valueOf(message.getUserCoreId()));
            if (orgUserVo == null) {
                String emailContent =
                        "处理蚂蚁机构初审异常，原因: " + "未找到id为" + message.getUserCoreId() + "的电销客户经理账号" +
                                "，当前请求报文：" + JSON.toJSONString(message) +
                                ",请及时跟进处理！";
                sendEmailMessage.sendEmailByType("蚂蚁机构初审处理异常", emailContent, "15");
                return;
            }
            //查询线索平台
            R<CluePreApproveApiRespDTO> queryApprove = remoteCluePlatformService.queryApprove(message.getBoId());
            R<List<ClueApproveFileApiRespDTO>> approveFiles = remoteCluePlatformService.queryApproveFile(message.getBoId());
            if (200 != queryApprove.getCode()) {
                throw new AntPreApproveException("查询审批信息失败");
            }
            if (200 != approveFiles.getCode()) {
                throw new AntPreApproveException("查询审批文件失败");
            }

            if (queryApprove.getData() == null) {
                throw new AntPreApproveException("查询审批信息为空");
            }
            if (approveFiles.getData() == null) {
                throw new AntPreApproveException("查询审批文件信息为空");
            }
            String applicationNo = creditAuthorizationFactory.splicingApplicationNo();
            CreditAuthorizationTransform transform = new CreditAuthorizationTransform();

            CreditAuthorization creditAuthorization = transform.byDTO(
                    queryApprove.getData(), applicationNo,
                    message, orgUserVo
            );
            if (flowSwitcher.useNewFlow()) {
                creditAuthorization.setIsAntNewFlow("1");
            }

            ClueApproveFileApiRespDTO sfzzm = approveFiles.getData().stream().filter(image -> "02".equals(image.getFileType())).findFirst().orElseThrow(() -> new AntPreApproveException("身份证正面缺失"));
            ClueApproveFileApiRespDTO sfzfm = approveFiles.getData().stream().filter(image -> "01".equals(image.getFileType())).findFirst().orElseThrow(() -> new AntPreApproveException("身份证反面缺失"));
            ClueApproveFileApiRespDTO rxzp = approveFiles.getData().stream().filter(image -> "03".equals(image.getFileType())).findFirst().orElseThrow(() -> new AntPreApproveException("人像照片缺失"));

            if (!StringUtil.isEmpty(creditAuthorization.getVehicleRegisterDate())){
                //获取车龄
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate vehicleRegisterDate = LocalDate.parse(creditAuthorization.getVehicleRegisterDate(), formatter);
                LocalDate authorizationDate = LocalDate.now();
                double diffYears = LocalDateUtils.getDiffYears(vehicleRegisterDate, authorizationDate);
                diffYears = (long) (diffYears * 100) / 100.0;

                //获取里程
                int vehicleMileage = carAgeMileageConfigRepository.getMileageInRange(diffYears).intValue();
                creditAuthorization.setVehicleMileage(vehicleMileage);
            }


            //保存征信授权信息
            boolean saved = creditAuthorizationRepository.saveData(creditAuthorization);

            if (!saved) {
                throw new AntPreApproveException("蚂蚁保存征信授权信息失败");
            }

            AuthorizedImage sfzzmImage = authorizedImageFactory.saveAuthorizedImage(creditAuthorization.getId(), "身份证正面", "sfzzm", fileUrl + sfzzm.getFileCode());
            AuthorizedImage sfzfmimage = authorizedImageFactory.saveAuthorizedImage(creditAuthorization.getId(), "身份证反面", "sfzfm", fileUrl + sfzfm.getFileCode());
            AuthorizedImage rxzpImage = authorizedImageFactory.saveAuthorizedImage(creditAuthorization.getId(), "人像照片", "rxzp", fileUrl + rxzp.getFileCode());
            //保存影像信息
            authorizedImageRepository.saveDate(sfzzmImage);
            authorizedImageRepository.saveDate(sfzfmimage);
            authorizedImageRepository.saveDate(rxzpImage);


            FddRASignDTOAssembler assembler = new FddRASignDTOAssembler();
            FddRASignDTO signDTO = assembler.toDto(
                    creditAuthorization,
                    queryApprove.getData(),
                    sfzzmImage,
                    sfzfmimage,
                    rxzpImage);

            //发起法大大电子签
            RemoteInterfaceResponse<FddRASignVO> response = remoteBigDataService.fddStartSignFlow(
                    RemoteInterfaceRequest.create(signDTO, new Identity(creditAuthorization.getId()))
            );
            if (!response.isSuccess()) {
                throw new AntPreApproveException(StringUtil.isEmpty(response.getMsg()) ? "法大大电子签失败" : response.getMsg());
            }
            if ("3".equals(response.getData().getState())) {
                throw new AntPreApproveException(StringUtil.isEmpty(response.getData().getMessage()) ? "法大大电子签失败" : response.getData().getMessage());
            }


            CreditAuthorization authorization = new CreditAuthorization();
            //记录法大大签署流程id
            authorization.setSignFlowId(response.getData().getFollowId());


            creditAuthorizationRepository.updateDataById(creditAuthorization.getId(), authorization);
        } catch (Exception e) {
            log.error("[处理蚂蚁机构初审异常],异常原因={}", e.getMessage(), e);
            String emailContent =
                    "处理蚂蚁机构初审异常，原因: " + e.getMessage() +
                            "，当前线索编号：" + message.getBoId() +
                            ",请及时跟进处理！";
            sendEmailMessage.sendEmailByType("蚂蚁机构初审处理异常", emailContent, "15");
            redisTemplate.opsForList().leftPush("ant:auto:credit:approve:error", JSON.toJSONString(message));
        }
    }

    @Override
    public void fddSignComplete(FddRASignVO fddRASignVO) {
        log.info("[初审法大大签署完成,开始处理],入参={}", JSON.toJSONString(fddRASignVO));
        try {
            CreditAuthorization creditAuthorization = new CreditAuthorization();
            creditAuthorization.setSignFlowId(fddRASignVO.getFollowId());
            CreditAuthorization authorization = creditAuthorizationRepository.getObject(creditAuthorization);
            if (authorization == null) {
                throw new AntPreApproveException("法大大电子签流程id不存在");
            }

            List<FileDTO> fileList = fddRASignVO.getFileList();

            if (fileList.isEmpty()) {
                //未拿到文件  放到队列
                redisTemplate.opsForList().leftPush("fdd:signing:list", fddRASignVO.getFollowId());
            }

            FileDTO yuanOu = fileList.stream().filter(image -> "元欧个人征信授权书".equals(image.getFileName())).findFirst().orElseThrow(() -> new AntPreApproveException("元欧个人征信授权书缺失"));
            FileDTO zqhtCre = fileList.stream().filter(image -> "中企汇通信用信息查询、使用授权书".equals(image.getFileName())).findFirst().orElseThrow(() -> new AntPreApproveException("中企汇通信用信息查询、使用授权书缺失"));

            AuthorizedImage yuanOuCreImage = authorizedImageFactory.saveAuthorizedImage(
                    authorization.getId(), "元欧个人征信授权书", "yo_cre", yuanOu.getFileUrl());

            AuthorizedImage zqhtCreImage = authorizedImageFactory.saveAuthorizedImage(
                    authorization.getId(), "中企汇通信用信息查询、使用授权书", "zqht", zqhtCre.getFileUrl());


            //拿到文件 签署完成
            authorization.signComplete(fddRASignVO.getFollowId());
            creditAuthorizationRepository.store(authorization);
            authorizedImageRepository.saveDate(yuanOuCreImage);
            authorizedImageRepository.saveDate(zqhtCreImage);

            //电销版初筛
            creditAuthorizationService.preScreeningForTelemarketing(new CreditAuthID(authorization.getId()));
        } catch (Exception e) {
            log.error("[法大大签约完成处理异常],异常原因={}", e.getMessage(), e);
            String emailContent =
                    "处理初审法大大签约完成处理异常，原因: " + e.getMessage() +
                            "，当前法大大签约完成报文：" + JSON.toJSONString(fddRASignVO) +
                            ",请及时跟进处理！";
            sendEmailMessage.sendEmail("初审法大大签约完成处理异常", emailContent, "初审法大大签约完成处理异常");
            redisTemplate.opsForList().leftPush("credit:fdd:sign:error", JSON.toJSONString(fddRASignVO));
        }
    }

    @Override
    public void antPreApproveRetry() {
        String listRedisKey = "ant:auto:credit:approve:error";
        Long size = this.redisTemplate.opsForList().size(listRedisKey);
        if (size != null && size > 0) {
            for (int i = 0; i < size; i++) {
                String message = this.redisTemplate.opsForList().rightPop(listRedisKey);
                if (StringUtils.isBlank(message)) {
                    continue;
                }
                try {
                    AntPreApproveMessage approveMessage = JSONObject.parseObject(message, AntPreApproveMessage.class);

                    this.antPreApprove(approveMessage);
                } catch (Exception e) {
                    log.error("[重试处理蚂蚁自动预审],异常原因={}", e.getMessage(), e);
                    String emailContent =
                            "重试处理蚂蚁自动预审异常，原因: " + e.getMessage() +
                                    "，当前信息：" + message +
                                    ",请及时跟进处理！";
                    sendEmailMessage.sendEmail("重试蚂蚁自动预审处理异常", emailContent, "重试蚂蚁自动预审");
                    redisTemplate.opsForList().leftPush("ant:auto:credit:approve:error", message);
                }
            }
        }
    }

    @Override
    public ClueAdditionalResult queryIncomeProof(Integer loanId) {
        String applyNum = splBussinessbasicMapper.getApplyNum(loanId);
        R<ClueAdditionalDTO> clueAdditionalDTO = remoteCluePlatformService.queryIncomeProof(new BoIdMessage(applyNum));
        ClueApproveInfoDto clueApproveInfoDto = clueApproveInfoService.getClueApproveInfo(applyNum);
        if (clueApproveInfoDto == null || !"1".equals(clueApproveInfoDto.getIncomeProofFinished())) {
            throw new BusinessException("未开启蚂蚁自证流程");
        }
        return handlerClueAdditional(clueAdditionalDTO,clueApproveInfoDto);
    }

    private ClueAdditionalResult handlerClueAdditional(R<ClueAdditionalDTO> clueAdditionalDTO,ClueApproveInfoDto clueApproveInfoDto) {
        ClueAdditionalResult result = new ClueAdditionalResult();
        result.setBoId(clueApproveInfoDto.getBoId());
        result.setAdditionalAmountRequired(clueApproveInfoDto.getAdditionalAmountRequired());
        if (!"1".equals(clueApproveInfoDto.getIncomeProofFinished())) {
            return result;
        }
        if (200 != clueAdditionalDTO.getCode() || clueAdditionalDTO.getData() == null) {
            throw new BusinessException("蚂蚁自证信息为空");
        }
        ClueAdditionalDTO data = clueAdditionalDTO.getData();
        List<ClueAdditionalDTO.PaymentLogInfo> paymentLogList = data.getPaymentLogList();
        if (paymentLogList == null || paymentLogList.isEmpty()) {
            throw new BusinessException("蚂蚁自证信息为空");
        }
        List<String> previousMonths = getPreviousMonths(6,clueApproveInfoDto.getIncomeProofFinishedTime());
        List<Map<String, String>> paymentLists = new ArrayList<>();
        result.setMonths(previousMonths);
        List<String> pdfFiles = new ArrayList<>();
        for (ClueAdditionalDTO.PaymentLogInfo paymentLogInfo : paymentLogList) {
            String paymentCategory = paymentLogInfo.getPaymentCategory();
            Map<String, String> payment = new HashMap<>();
            switch (paymentCategory) {
                case "ALIPAY_PAYMENT":
                    payment.put("type", "支付宝");
                    break;
                case "BANK_PAYMENT":
                    payment.put("type", "银行");
                    break;
                case "WECHAT_PAYMENT":
                    payment.put("type", "微信");
                    break;
                default:
                    continue;
            }
            if (StringUtil.isNotEmpty(paymentLogInfo.getPdfFileDir())) {
                pdfFiles.add(paymentLogInfo.getPdfFileDir());
            }
            // 初始化数据
            previousMonths.forEach(month -> {
                payment.put(month + "_incomeAmount", "0");
                payment.put(month + "_expenseAmount", "0");
            });
            List<ClueAdditionalDTO.PaymentLogVo> paymentList = paymentLogInfo.getPaymentList();
            if (paymentList != null && !paymentList.isEmpty()) {
                paymentList.forEach(p -> {
                    String month = LocalDateTime.parse(p.getStartDate(), DATE_FORMATTER).format(MONTH_FORMATTER);
                    // 只处理前六个月的数据
                    if (previousMonths.contains(month)) {
                        payment.put(month + "_incomeAmount", BigDecimalUtil.centToYuan(p.getIncomeAmount()).toString());
                        payment.put(month + "_expenseAmount", BigDecimalUtil.centToYuan(p.getExpenseAmount()).toString());
                    }
                });
            }
            paymentLists.add(payment);
        }

        if (paymentLists.size() < 3) {
            ClueAdditionalDTO.PaymentLogInfo BANK_PAYMENT = paymentLogList.stream().filter(p -> "BANK_PAYMENT".equals(p.getPaymentCategory())).findFirst().orElse(null);
            ClueAdditionalDTO.PaymentLogInfo ALIPAY_PAYMENT = paymentLogList.stream().filter(p -> "ALIPAY_PAYMENT".equals(p.getPaymentCategory())).findFirst().orElse(null);
            ClueAdditionalDTO.PaymentLogInfo WECHAT_PAYMENT = paymentLogList.stream().filter(p -> "WECHAT_PAYMENT".equals(p.getPaymentCategory())).findFirst().orElse(null);
            if (BANK_PAYMENT == null) {
                paymentLists.add(addZeroData(previousMonths,"银行"));
            }
            if (ALIPAY_PAYMENT == null) {
                paymentLists.add(addZeroData(previousMonths,"支付宝"));
            }
            if (WECHAT_PAYMENT == null) {
                paymentLists.add(addZeroData(previousMonths,"微信"));
            }
        }

        // 计算合计数据
        Map<String, String> sumPayment = new HashMap<>();
        sumPayment.put("type", "合计");
        // 初始化合计数据
        previousMonths.forEach(month -> {
            sumPayment.put(month + "_incomeAmount", "0");
            sumPayment.put(month + "_expenseAmount", "0");
        });

        // 累加每个支付类型的数据
        paymentLists.forEach(p -> {
            previousMonths.forEach(month -> {
                String incomeKey = month + "_incomeAmount";
                String expenseKey = month + "_expenseAmount";
                String income = String.valueOf(p.getOrDefault(incomeKey, "0"));
                String expense = String.valueOf(p.getOrDefault(expenseKey, "0"));
                sumPayment.put(incomeKey, BigDecimalUtil.add(sumPayment.get(incomeKey), income));
                sumPayment.put(expenseKey, BigDecimalUtil.add(sumPayment.get(expenseKey), expense));
            });
        });

        // 将合计数据添加到结果列表
        paymentLists.add(sumPayment);
        String pdfFileDir = "";
        if (!pdfFiles.isEmpty()) {
            pdfFileDir = pdfFiles.stream().map(pdfFile -> fileUrl + pdfFile).collect(Collectors.joining("|"));
        }
        result.setFlowInfoData(paymentLists);
        result.setSumPayment(sumPayment);
        result.setPdfFiles(pdfFileDir);

        return result;
    }

    private Map<String, String> addZeroData(List<String> previousMonths,String type) {
        Map<String, String> payment = new HashMap<>();
        payment.put("type", type);
        previousMonths.forEach(month -> {
            payment.put(month + "_incomeAmount", "0");
            payment.put(month + "_expenseAmount", "0");
        });
        return payment;
    }

    private static Map<String, String> handlerThirdSmallestMonthData(List<String> previousMonths, Map<String, String> sumPayment, List<Map<String, String>> paymentLists) {
        // 找出第三小的合计收入及其对应的月份
        Map<String, String> thirdSmallestMonthData = new HashMap<>();
        Map<String, BigDecimal> incomeByMonth = new HashMap<>();
        for (String month : previousMonths) {
            String incomeKey = month + "_incomeAmount";
            incomeByMonth.put(month, new BigDecimal(sumPayment.get(incomeKey)));
        }

        List<Map.Entry<String, BigDecimal>> incomeList = new ArrayList<>(incomeByMonth.entrySet());
        incomeList.sort(Map.Entry.comparingByValue());

        if (incomeList.size() >= 3) {
            Map.Entry<String, BigDecimal> thirdSmallestEntry = incomeList.get(2);
            String thirdSmallestMonth = thirdSmallestEntry.getKey();
            BigDecimal thirdSmallestIncome = thirdSmallestEntry.getValue();

            // 构建第三小月份的详细信息
            thirdSmallestMonthData.put("month", thirdSmallestMonth);
            thirdSmallestMonthData.put("incomeAmount", thirdSmallestIncome.toString());
            for (Map<String, String> payment : paymentLists) {
                String type = payment.get("type");
                thirdSmallestMonthData.put(type + "_incomeAmount", payment.get(thirdSmallestMonth + "_incomeAmount"));
                thirdSmallestMonthData.put(type + "_expenseAmount", payment.get(thirdSmallestMonth + "_expenseAmount"));
            }
        }
        return thirdSmallestMonthData;
    }

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取从当前月往前推的N个月份（剔除当前月）
     *
     * @param monthsCount             需要获取的月份数量
     * @param timestamp
     * @return 月份列表，格式为yyyyMM
     */
    public static List<String> getPreviousMonths(int monthsCount, Timestamp timestamp) {
        List<String> months = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();
        if (timestamp != null) {
            currentDate = timestamp.toLocalDateTime().toLocalDate();
        }
        for (int i = monthsCount - 1; i >= 0; i--) {
            YearMonth yearMonth = YearMonth.from(currentDate.minusMonths(i + 1));
            months.add(yearMonth.format(MONTH_FORMATTER));
        }
        return months;
    }

    @Override
    public ClueAdditionalResult queryIncomeProof(String applyNum) {
        R<ClueAdditionalDTO> clueAdditionalDTO = remoteCluePlatformService.queryIncomeProof(new BoIdMessage(applyNum));
        ClueApproveInfoDto clueApproveInfoDto = clueApproveInfoService.getClueApproveInfo(applyNum);
        if (clueApproveInfoDto == null) {
            log.error("未开启蚂蚁自证流程");
            return null;
        }
        return handlerClueAdditional(clueAdditionalDTO,clueApproveInfoDto);
    }

    @Override
    public void incomeProofFinished(ClueShowPushMessage message) {
        ClueApproveInfoDto clueApproveInfo = clueApproveInfoService.getClueApproveInfo(message.getBoId());
        if (clueApproveInfo == null) {
            log.error("查询蚂蚁自证收入失败,线索审批信息为空{}",clueApproveInfo);
            return;
        }
        String incomeProofFinishedOld = clueApproveInfo.getIncomeProofFinished();
        if ("1".equals(incomeProofFinishedOld)) {
            log.error("当前流水自证补录状态为已完成不再接受状态变更{}",message);
            return;
        }

        R<ClueAdditionalDTO> clueAdditionalDTO = remoteCluePlatformService.queryIncomeProof(new BoIdMessage(message.getBoId()));
        String incomeProofMessage = null;
//        clueApproveInfo.setBoId(message.getBoId());
        ClueLabelEnums clueLabelEnums = ClueLabelEnums.getByLabelType(message.getClueLabel());
        if (clueLabelEnums == null) {
            log.error("查询蚂蚁自证收入失败,推送流水补录状态为空;{}",clueLabelEnums);
            return;
        }
        clueApproveInfo.setIncomeProofFinished(clueLabelEnums.getCode());
        if ("1".equals(clueLabelEnums.getCode())) {
            try {
                ClueAdditionalResult result = handlerClueAdditional(clueAdditionalDTO,clueApproveInfo);
                Map<String, String> thirdSmallestMonthData = handlerThirdSmallestMonthData(result.getMonths(), result.getSumPayment(), result.getFlowInfoData());
                incomeProofMessage = String.format("蚂蚁自证收入：%s（包括支付宝收入：%s；微信收入：%s；银行卡收入：%s）",
                        thirdSmallestMonthData.get("合计_incomeAmount"),
                        thirdSmallestMonthData.get("支付宝_incomeAmount"),
                        thirdSmallestMonthData.get("微信_incomeAmount"),
                        thirdSmallestMonthData.get("银行_incomeAmount"));
                clueApproveInfo.setIncomeProofFinishedTime(new Timestamp(System.currentTimeMillis()));
                clueApproveInfo.setIncomeProofMessage(incomeProofMessage);
            } catch (Exception e) {
                log.error("查询蚂蚁自证收入失败;{}",e.getMessage());
            }
        }

        clueApproveInfoService.updateIncomeProofFinished(clueApproveInfo);

        //如果订单取消或自证流水未提交，仅修改状态不推送企微消息
        if ("0".equals(clueLabelEnums.getCode()) || "4".equals(clueLabelEnums.getCode())) {
            return;
        }

        //如果是已补充|已催办|已超时，不推送企微消息
        if (!Arrays.asList(new String[]{"1", "2", "3"}).contains(incomeProofFinishedOld)) {
            CreditAuthorization par = new CreditAuthorization();
            par.setBoId(message.getBoId());
            par.setAuthorizationStatus(1);
            List<CreditAuthorization> creditAuthorizationList = creditAuthorizationRepository.list(par);
            List<String> applicationNos = creditAuthorizationList.stream()
                    .map(CreditAuthorization::getApplicationNo).collect(Collectors.toList());

            if (applicationNos.isEmpty()){
                return;
            }

            List<CreditLoan> creditLoans = creditLoanRepository.searchCreditDataByApplicationNo(applicationNos);

            CreditLoan loan = creditLoans.stream()
                    .filter(r -> "98".equals(r.getSelectCode()) && "1".equals(r.getCreditFlag()))
                    .max(Comparator.comparing(CreditLoan::getCreateTime)).orElse(new CreditLoan());
            if (StringUtil.isNotEmpty(loan.getCreditOrderId())) {
                weChatMessagePushService.pushWeChatMessagePrePass(loan.getCreditOrderId());
            }
        }
    }

    @Override
    public void fddSignCompleteRetry() {
        String listRedisKey = "credit:fdd:sign:error";
        Long size = this.redisTemplate.opsForList().size(listRedisKey);
        if (size != null && size > 0) {
            for (int i = 0; i < size; i++) {
                String message = this.redisTemplate.opsForList().rightPop(listRedisKey);
                if (StringUtils.isBlank(message)) {
                    continue;
                }
                try {
                    FddRASignVO fddRASignVO = JSONObject.parseObject(message, FddRASignVO.class);

                    this.fddSignComplete(fddRASignVO);
                } catch (Exception e) {
                    log.error("[重试处理法大大电子签异常],异常原因={}", e.getMessage(), e);
                    String emailContent =
                            "重试处理法大大电子签异常，原因: " + e.getMessage() +
                                    "，当前信息：" + message +
                                    ",请及时跟进处理！";
                    sendEmailMessage.sendEmail("重试自动预审法大大电子签处理异常", emailContent, "重试自动预审法大大电子签");
                    redisTemplate.opsForList().leftPush("credit:fdd:sign:error", message);
                }
            }
        }
    }
}
