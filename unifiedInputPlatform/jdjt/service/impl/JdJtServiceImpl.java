package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.jdjt.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.apollo.org.vo.OrgUserVo;
import com.apollo.org.vo.UserObjectRepository;
import com.apollo.util.DateUtil;
import com.common.LocalDateUtils;
import com.zlhj.common.core.domain.R;
import com.zlhj.commonLoan.business.basic.service.BusinessJudgmentConditionService;
import com.zlhj.commonLoan.business.clue.dto.LoanStatePushToClueDTO;
import com.zlhj.commonLoan.business.clue.enums.LoanStatusChangeEnum;
import com.zlhj.commonLoan.business.common.exception.BusinessException;
import com.zlhj.commonLoan.business.common.pojo.Identity;
import com.zlhj.commonLoan.domain.cule.ClueNumber;
import com.zlhj.commonLoan.domain.mainLoan.LoanId;
import com.zlhj.commonLoan.interfaces.ksyh.pojo.AdditionalFeeInformationRepository;
import com.zlhj.commonLoan.interfaces.ksyh.pojo.AdditionalFeeRepayPlan;
import com.zlhj.commonLoan.interfaces.ksyh.pojo.AdditionalFeeRepayPlanRepository;
import com.zlhj.commonLoan.util.StringUtil;
import com.zlhj.electronicCredit.pojo.*;
import com.zlhj.electronicCredit.service.CreditLoanService;
import com.zlhj.hrxj.interfaces.dto.CarInfoDto;
import com.zlhj.hrxj.interfaces.dto.CarInfoRepository;
import com.zlhj.infrastructure.po.*;
import com.zlhj.infrastructure.repository.*;
import com.zlhj.infrastructure.routing.RemoteBigDataService;
import com.zlhj.infrastructure.routing.RemoteCluePlatformService;
import com.zlhj.infrastructure.routing.dto.RemoteInterfaceRequest;
import com.zlhj.infrastructure.routing.dto.RemoteInterfaceResponse;
import com.zlhj.infrastructure.routing.dto.clue.JdJtClueFile;
import com.zlhj.infrastructure.routing.dto.clue.JdJtClueFileType;
import com.zlhj.infrastructure.routing.dto.clue.JdJtCluePreApproveDTO;
import com.zlhj.jd.vo.DirectCustomerStatusDto;
import com.zlhj.loan.SendEmailMessage;
import com.zlhj.loan.entity.SapdcslasRepository;
import com.zlhj.loan.service.BankApprovalRecordService;
import com.zlhj.loan.service.PreliminaryScreeningService;
import com.zlhj.loan.vo.BankApprovalRecordPo;
import com.zlhj.main.fumin.enums.BankOrgNameType;
import com.zlhj.mapper.LoanInitialRepaymentScheduleMapper;
import com.zlhj.mapper.SplBussinessbasicMapper;
import com.zlhj.mq.dto.JdJtPreApproveMessage;
import com.zlhj.mq.provider.Sender;
import com.zlhj.unifiedInputPlatform.autoCredit.dto.FddRASignDTO;
import com.zlhj.unifiedInputPlatform.ant.dto.FddRASignVO;
import com.zlhj.unifiedInputPlatform.autoCredit.universal.transform.FddRASignDTOAssembler;
import com.zlhj.unifiedInputPlatform.ant.exceptions.AntPreApproveException;
import com.zlhj.unifiedInputPlatform.autoCredit.universal.transform.CreditAuthorizationTransform;
import com.zlhj.unifiedInputPlatform.autoCredit.dto.QueryClueBillDTO;
import com.zlhj.unifiedInputPlatform.jd.dto.JDLoanSuccessDTO;
import com.zlhj.unifiedInputPlatform.jd.service.JDClueQueryBillService;
import com.zlhj.unifiedInputPlatform.jdjt.dto.JdJtClueStatusNotifyDTO;
import com.zlhj.unifiedInputPlatform.jdjt.dto.JdJtQueryClueInfoDTO;
import com.zlhj.unifiedInputPlatform.jdjt.dto.JdJtRepaymentChangeNotifyDTO;
import com.zlhj.unifiedInputPlatform.jdjt.dto.VehicleEvaluationNotifyDTO;
import com.zlhj.unifiedInputPlatform.jdjt.exceptions.JdJtPreApproveException;
import com.zlhj.unifiedInputPlatform.jdjt.exceptions.JdJtScreeningException;
import com.zlhj.unifiedInputPlatform.jdjt.service.JdJtService;
import com.zlhj.unifiedInputPlatform.universal.service.impl.UnifiedInputPlatformServiceImpl;
import com.zlhj.user.vo.MultipleLoanObject;
import com.zlhj.user.vo.MultipleLoanRepository;
import com.zlhj.webank.business.entity.SpcCreditOtherInfoEntity;
import com.zlhj.webank.business.entity.SpcCreditOtherInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JdJtServiceImpl implements JdJtService {

	@Autowired
	private UserObjectRepository userObjectRepository;

	@Autowired
	private SendEmailMessage sendEmailMessage;

	@Autowired
	private RemoteCluePlatformService remoteCluePlatformService;

	@Autowired
	private CreditAuthorizationFactory creditAuthorizationFactory;

	@Autowired
	private AuthorizedImageFactory authorizedImageFactory;

	@Autowired
	private AuthorizedImageRepository authorizedImageRepository;

	@Autowired
	private RemoteBigDataService remoteBigDataService;

	@Autowired
	private CreditAuthorizationRepository creditAuthorizationRepository;

	@Autowired
	private CarAgeMileageConfigRepository carAgeMileageConfigRepository;

	@Autowired
	private SplBussinessbasicMapper splBussinessbasicMapper;

	@Autowired
	private AdditionalFeeInformationRepository additionalFeeInformationRepository;

	@Autowired
	private BankApprovalRecordService bankApprovalRecordService;

	@Autowired
	private MultipleLoanRepository loanInfoRepository;

	@Autowired
	private CarInfoRepository carInfoRepository;

	@Value("${zlhj.image.postUrlDomain}")
	private String fileUrl;

	@Autowired
	private LoanInitialRepaymentScheduleMapper loanInitialRepaymentScheduleMapper;

	@Autowired
	private UnifiedInputPlatformServiceImpl unifiedInputPlatformService;

	@Autowired
	private SapdcslasRepository sapdcslasRepository;

	@Autowired
	private CreditLoanRepository creditLoanRepository;

	@Autowired
	private JDClueQueryBillService jdClueQueryBillService;

	@Autowired
	private PreliminaryScreeningService preliminaryScreeningService;

	@Autowired
	private CreditLoanService creditLoanService;

	@Autowired
	private AdditionalFeeRepayPlanRepository additionalFeeRepayPlanRepository;

	@Autowired
	private LicenseAccessWhitelistRepository licenseAccessWhitelistRepository;

	@Autowired
	private VinBlacklistRepository vinBlacklistRepository;

	@Autowired
	private CreditUserVORepository creditUserRepository;

	@Autowired
	private SpcCreditOtherInfoRepository creditOtherInfoRepository;

	@Autowired
	private BusinessJudgmentConditionService businessJudgmentConditionService;

	@Autowired
	private Sender sender;

	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@Override
	public void autoPreApprove(JdJtPreApproveMessage message) {
		try {
			OrgUserVo orgUserVo = userObjectRepository.getByUserId(Integer.valueOf(message.getUserCoreId()));

			if (orgUserVo == null) {
				String emailContent =
						"处理京东金条机构初审异常，原因: " + "未找到id为" + message.getUserCoreId() + "的电销客户经理账号" +
								"，当前请求报文：" + JSON.toJSONString(message) +
								",请及时跟进处理！";
				sendEmailMessage.sendEmailByType("京东金条初审处理异常", emailContent, "15");
				return;
			}

			R<JdJtCluePreApproveDTO> jdJtCluePreApproveDTO = remoteCluePlatformService.queryJdJtApprove(message.getBoId());

			if (200 != jdJtCluePreApproveDTO.getCode()) {
				throw new JdJtPreApproveException("查询京东金条审批信息失败");
			}

			//校验必填
			jdJtCluePreApproveDTO.getData().checkRequired();
			String applicationNo = creditAuthorizationFactory.splicingApplicationNo();

			CreditAuthorizationTransform transform = new CreditAuthorizationTransform();

			CreditAuthorization creditAuthorization = transform.byJdJtDTO(
					jdJtCluePreApproveDTO.getData(),
					applicationNo,
					message,
					orgUserVo
			);

			//获取车龄
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			LocalDate vehicleRegisterDate = LocalDate.parse(creditAuthorization.getVehicleRegisterDate(), formatter);
			LocalDate authorizationDate = LocalDate.now();
			double diffYears = LocalDateUtils.getDiffYears(vehicleRegisterDate, authorizationDate);
			diffYears = (long) (diffYears * 100) / 100.0;

			if (diffYears < 0) {
				log.info("车龄为{}，车龄不能小于0", diffYears);
				creditAuthorization.setScreeningResults("初筛拒绝");
				creditAuthorization.setScreeningTime(DateUtil.getNowDate());
				creditAuthorizationRepository.saveData(creditAuthorization);

				//推送京东金条预审结果通知
                LoanStatePushToClueDTO pre = new LoanStatePushToClueDTO(message.getBoId(), null, LoanStatusChangeEnum.AUTO_PRE_REJECT.getValue(), message.getChannelCode(), "JDJT_NOTICE");
				unifiedInputPlatformService.realTimeInteraction(pre);
				sender.jdJtPrePassPush(pre);
				return;
			}else {
				//获取里程
				int vehicleMileage = carAgeMileageConfigRepository.getMileageInRange(diffYears).intValue();
				creditAuthorization.setVehicleMileage(vehicleMileage);
			}

			// 京东金条车辆信息拒绝前置
			Boolean isOpen = businessJudgmentConditionService.searchSwitchState("jdjtVehicleInformationRejectionFront");
			// 开关是否开启
			if (isOpen) {
				try {
					// 京东金条车辆信息校验
					this.initialScreening(creditAuthorization);
				} catch (JdJtScreeningException e) {
					log.info("京东金条车辆信息拒绝，原因: " + e.getMessage());

					// 构造征信数据
					CreditUserVO creditUser = creditAuthorizationFactory.splicingCreditUserData(creditAuthorization, BankOrgNameType.HCD.getKey(), 2, "电销拒绝");
					CreditLoan creditLoan = creditAuthorizationFactory.splicingCreditLoanData(creditAuthorization, creditUser.getCreditOrderId(), BankOrgNameType.HCD.getKey(), 2, "电销拒绝");
					SpcCreditOtherInfoEntity creditOtherInfo = creditAuthorizationFactory.splicingOtherInfoData(creditAuthorization, creditUser.getCreditOrderId());
					creditLoan.setScreenResult("初筛拒绝");
					creditLoan.setCreditErrorMsg(e.getMessage());
					creditLoan.setScreeningTime(new Timestamp(System.currentTimeMillis()));
					creditLoan.setBeforeApproveConclusion("3");
					creditAuthorization.setScreeningResults("初筛拒绝");
					creditAuthorization.setScreeningTime(DateUtil.getNowDate());

					creditUserRepository.saveData(creditUser);
					creditLoanRepository.saveData(creditLoan);
					creditOtherInfoRepository.saveCreditOtherInfo(creditOtherInfo);
					creditAuthorizationRepository.saveData(creditAuthorization);

					// 推送京东金条预审结果通知
                    LoanStatePushToClueDTO pre = new LoanStatePushToClueDTO(message.getBoId(), null, LoanStatusChangeEnum.AUTO_PRE_REJECT.getValue(), message.getChannelCode(), "JDJT_NOTICE");
					pre.setRefuseReason(e.getMessage());
					unifiedInputPlatformService.realTimeInteraction(pre);
					sender.jdJtPrePassPush(pre);
					return;
				}
			}

			List<JdJtClueFile> approveFiles = jdJtCluePreApproveDTO.getData().getFileList();

			JdJtClueFile sfzzm = approveFiles
					.stream()
					.filter(image -> JdJtClueFileType.sfzzm.getCode().equals(image.getFileType()))
					.findFirst()
					.orElseThrow(
							() -> new AntPreApproveException("身份证正面缺失"));

			JdJtClueFile sfzfm = approveFiles
					.stream()
					.filter(image -> JdJtClueFileType.sfzzf.getCode().equals(image.getFileType()))
					.findFirst()
					.orElseThrow(
							() -> new AntPreApproveException("身份证反面缺失"));

			JdJtClueFile rxzp = approveFiles
					.stream()
					.filter(image -> JdJtClueFileType.rxzp.getCode().equals(image.getFileType()))
					.findFirst()
					.orElseThrow(
							() -> new AntPreApproveException("人像照片缺失"));

			boolean saved = creditAuthorizationRepository.saveData(creditAuthorization);

			if (!saved) {
				throw new AntPreApproveException("京东金条保存征信授权信息失败");
			}

			AuthorizedImage sfzzmImage = authorizedImageFactory
					.saveAuthorizedImage(
							creditAuthorization.getId(), "身份证正面",
							"sfzzm", fileUrl + sfzzm.getFileCode());

			AuthorizedImage sfzfmimage = authorizedImageFactory
					.saveAuthorizedImage(
							creditAuthorization.getId(), "身份证反面",
							"sfzfm", fileUrl + sfzfm.getFileCode());

			AuthorizedImage rxzpImage = authorizedImageFactory
					.saveAuthorizedImage(
							creditAuthorization.getId(), "人像照片",
							"rxzp", fileUrl + rxzp.getFileCode());

			authorizedImageRepository.saveDate(sfzzmImage);

			authorizedImageRepository.saveDate(sfzfmimage);

			authorizedImageRepository.saveDate(rxzpImage);

			FddRASignDTOAssembler assembler = new FddRASignDTOAssembler();
			FddRASignDTO signDTO = assembler.toDto(
					creditAuthorization,
					jdJtCluePreApproveDTO.getData(),
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
			log.error("[处理京东金条自动预审异常],异常原因={}", e.getMessage(), e);
			String emailContent =
					"处理京东金条自动预审异常，原因: " + e.getMessage() +
							"，当前线索编号：" + message.getBoId() +
							"，当前报文：" + JSON.toJSONString(message) +
							",请及时跟进处理！";

			sendEmailMessage.sendEmailByType("京东金条自动预审异常", emailContent, "15");
			redisTemplate.opsForList().leftPush("jdjt:auto:credit:approve:error", JSON.toJSONString(message));
		}
	}

	@Override
	public JdJtClueStatusNotifyDTO queryClueInfo(JdJtQueryClueInfoDTO queryDTO) {
		JdJtClueStatusNotifyDTO notifyDTO = new JdJtClueStatusNotifyDTO();
		notifyDTO.setBoId(queryDTO.getApplyNo());

		LoanStatusChangeEnum realTimeStatusEnum = LoanStatusChangeEnum.getEnumsByValue(queryDTO.getStatus());

		DirectCustomerStatusDto directCustomerStatusDto = splBussinessbasicMapper.selectStatusByOrderIdNoChannel(
				queryDTO.getApplyNo()
		);

		if ("JDJT".equals(queryDTO.getSource())) {
			if (directCustomerStatusDto == null) {
				return notifyDTO;
			}
			realTimeStatusEnum = LoanStatusChangeEnum.getEnumsByValue(directCustomerStatusDto.getSplMaxActionNum());
		}

		if (realTimeStatusEnum == null) {
			return notifyDTO;
		}

		notifyDTO.setCheckStatus(realTimeStatusEnum.getValue());

		if (LoanStatusChangeEnum.APPROVE_PASS == realTimeStatusEnum) {
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

	@Override
	public void loanSuccessNotify() {

		List<JDLoanSuccessDTO> loanSuccessData = splBussinessbasicMapper.jdJtLoanSuccessData();

		loanSuccessData.stream()
				.filter(data -> !CollectionUtils.isEmpty(loanInitialRepaymentScheduleMapper.selectList(data.getLoanId())))
				.forEach(data -> {
					unifiedInputPlatformService.realTimeInteraction(
							new LoanStatePushToClueDTO(
									data.getBoId(),
									null,
									LoanStatusChangeEnum.LEND_SUC.getValue(),
									data.getChannelPartner(),
									"JDJT_NOTICE"
							)
					);
					sapdcslasRepository.updateNotifyStatus(data.getMainLoanId(), "Y");
				});
	}

	@Override
	public QueryClueBillDTO clueQueryBill(String applyNo) {

		LoanId loanId = unifiedInputPlatformService.getLoanIdByClueSystem(new ClueNumber(applyNo), 35);

		if (ObjectUtil.isEmpty(loanId)) {
			loanId = unifiedInputPlatformService.getLoanIdByClueSystem(new ClueNumber(applyNo), 36);
		}

		if (ObjectUtil.isEmpty(loanId)) {
			throw new BusinessException("线索没有匹配的贷款数据");
		}
		log.info("当前Loanid为：{}", loanId);

		return jdClueQueryBillService.createForJdJt(loanId);
	}

	@Override
	public VehicleEvaluationNotifyDTO queryVehicleEvaluation(String applyNo) {

		CreditAuthorization credit = new CreditAuthorization();
		credit.setBoId(applyNo);
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

	@Override
	public void repayChangeNotify() {

		List<JdJtRepaymentChangeNotifyDTO> jdRepayPlanChangeData = splBussinessbasicMapper.jdjtRepayPlanChangeData();
		if (jdRepayPlanChangeData.isEmpty()) {
			return;
		}
		for (JdJtRepaymentChangeNotifyDTO jdRepayPlanChangeDatum : jdRepayPlanChangeData) {

			AdditionalFeeRepayPlan additionalFee =
					additionalFeeRepayPlanRepository.getByLoanIdAndTermAndExpenseType(jdRepayPlanChangeDatum.getLoanId(), Integer.parseInt(jdRepayPlanChangeDatum.getPaymentPeriod()), "1");
			AdditionalFeeRepayPlan gpsFee =
					additionalFeeRepayPlanRepository.getByLoanIdAndTermAndExpenseType(jdRepayPlanChangeDatum.getLoanId(), Integer.parseInt(jdRepayPlanChangeDatum.getPaymentPeriod()), "2");

			BigDecimal add = safeValue(additionalFee != null ? additionalFee.getApRepayAmount() : BigDecimal.ZERO)
					.add(safeValue(additionalFee != null ? additionalFee.getApPenalSum() : BigDecimal.ZERO))
					.add(safeValue(gpsFee != null ? gpsFee.getApRepayAmount() : BigDecimal.ZERO))
					.add(safeValue(gpsFee != null ? gpsFee.getApPenalSum() : BigDecimal.ZERO));
			jdRepayPlanChangeDatum.setGuaranteeAmount(add);
		}

		jdRepayPlanChangeData.forEach(sender::repaymentChangePushJdJt);
	}

	private static BigDecimal safeValue(BigDecimal value) {
		return value != null ? value : BigDecimal.ZERO;
	}

	private void initialScreening(CreditAuthorization creditAuth) throws JdJtScreeningException {
		// 车龄 > 12年
		try {
			// 车辆上牌日期
			LocalDate registerDate = LocalDate.parse(creditAuth.getVehicleRegisterDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			// 允许最大车龄
			LocalDate now = LocalDate.now();
			LocalDate maxAllowedDate = now.minusYears(12);
			if (registerDate.isBefore(maxAllowedDate)) {
				throw new JdJtScreeningException("车龄不准入");
			}
		} catch (Exception e) {
			throw new JdJtScreeningException("车龄不准入");
		}

		// 车牌前两位校验（京沪津渝除外）
		String license = creditAuth.getLicensePlateNo();
		if (StringUtils.isBlank(license) || license.length() < 2) {
			throw new JdJtScreeningException("车牌不准入");
		}
		LicenseAccessWhitelist licenseAccessWhitelist = licenseAccessWhitelistRepository.selectByLicense(license);
		if (licenseAccessWhitelist == null) {
			throw new JdJtScreeningException("车牌不准入");
		}

		// 车架号校验
		String vin = creditAuth.getChassisNumber();
		// 长度必须为17位
		if (vin.length() != 17) {
			throw new JdJtScreeningException("车架号不存在");
		}
		// 只能包含数字和大写字母
		if (!vin.matches("[0-9A-Z]+")) {
			throw new JdJtScreeningException("车架号不存在");
		}
		// 不能包含 I, O, Q
		if (vin.contains("I") || vin.contains("O") || vin.contains("Q")) {
			throw new JdJtScreeningException("车架号不存在");
		}
		// 前8位是否在黑名单
		VinBlacklist vinBlacklist = vinBlacklistRepository.selectByVin(vin);
		if (vinBlacklist != null) {
			throw new JdJtScreeningException("车品牌不准入");
		}
	}

	@Override
	public void preApproveRetry() {
		String listRedisKey = "jdjt:auto:credit:approve:error";
		Long size = this.redisTemplate.opsForList().size(listRedisKey);
		if (size != null && size > 0) {
			for (int i = 0; i < size; i++) {
				String message = this.redisTemplate.opsForList().rightPop(listRedisKey);
				if (com.zlhj.common.core.utils.StringUtils.isBlank(message)) {
					continue;
				}
				try {
					JdJtPreApproveMessage approveMessage = JSONObject.parseObject(message, JdJtPreApproveMessage.class);

					this.autoPreApprove(approveMessage);
				} catch (Exception e) {
					log.error("[重试处理京东金条自动预审异常],异常原因={}", e.getMessage(), e);
					String emailContent =
							"重试处理京东金条自动预审异常，原因: " + e.getMessage() +
									"，当前信息：" + message +
									",请及时跟进处理！";
					sendEmailMessage.sendEmail("重试京东金条自动预审异常", emailContent, "重试京东金条自动预审");
					redisTemplate.opsForList().leftPush("jdjt:auto:credit:approve:error", message);
				}
			}
		}
	}
}
