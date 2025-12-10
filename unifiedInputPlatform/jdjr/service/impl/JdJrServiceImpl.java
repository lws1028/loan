package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.jdjr.service.impl;//package com.zlhj.unifiedInputPlatform.jdjr.service.impl;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.apollo.org.vo.OrgUserVo;
//import com.apollo.org.vo.UserObjectRepository;
//import com.apollo.util.DateUtil;
//import com.common.LocalDateUtils;
//import com.zlhj.common.core.domain.R;
//import com.zlhj.commonLoan.business.basic.service.BusinessJudgmentConditionService;
//import com.zlhj.commonLoan.business.clue.dto.LoanStatePushToClueDTO;
//import com.zlhj.commonLoan.business.clue.enums.LoanStatusChangeEnum;
//import com.zlhj.commonLoan.business.common.pojo.Identity;
//import com.zlhj.commonLoan.interfaces.ksyh.pojo.AdditionalFeeInformationRepository;
//import com.zlhj.commonLoan.interfaces.ksyh.pojo.AdditionalFeeRepayPlanRepository;
//import com.zlhj.commonLoan.util.StringUtil;
//import com.zlhj.electronicCredit.pojo.*;
//import com.zlhj.electronicCredit.service.CreditLoanService;
//import com.zlhj.hrxj.interfaces.dto.CarInfoRepository;
//import com.zlhj.infrastructure.po.*;
//import com.zlhj.infrastructure.repository.*;
//import com.zlhj.infrastructure.routing.RemoteBigDataService;
//import com.zlhj.infrastructure.routing.RemoteCluePlatformService;
//import com.zlhj.infrastructure.routing.dto.RemoteInterfaceRequest;
//import com.zlhj.infrastructure.routing.dto.RemoteInterfaceResponse;
//import com.zlhj.infrastructure.routing.dto.clue.CluePreApproveDTO;
//import com.zlhj.infrastructure.routing.dto.clue.JdJtClueFile;
//import com.zlhj.infrastructure.routing.dto.clue.JdJtClueFileType;
//import com.zlhj.loan.SendEmailMessage;
//import com.zlhj.loan.entity.SapdcslasRepository;
//import com.zlhj.loan.service.BankApprovalRecordService;
//import com.zlhj.loan.service.PreliminaryScreeningService;
//import com.zlhj.main.fumin.enums.BankOrgNameType;
//import com.zlhj.mapper.LoanInitialRepaymentScheduleMapper;
//import com.zlhj.mapper.SplBussinessbasicMapper;
//import com.zlhj.mq.dto.JdJrPreApproveMessage;
//import com.zlhj.mq.provider.Sender;
//import com.zlhj.unifiedInputPlatform.ant.dto.FddRASignDTO;
//import com.zlhj.unifiedInputPlatform.ant.dto.FddRASignVO;
//import com.zlhj.unifiedInputPlatform.ant.dto.assembler.FddRASignDTOAssembler;
//import com.zlhj.unifiedInputPlatform.ant.exceptions.AntPreApproveException;
//import com.zlhj.unifiedInputPlatform.ant.transform.CreditAuthorizationTransform;
//import com.zlhj.unifiedInputPlatform.jd.service.JDClueQueryBillService;
//import com.zlhj.unifiedInputPlatform.jdjr.service.JdJrService;
//import com.zlhj.unifiedInputPlatform.jdjt.exceptions.JdJtPreApproveException;
//import com.zlhj.unifiedInputPlatform.jdjt.exceptions.JdJtScreeningException;
//import com.zlhj.unifiedInputPlatform.universal.service.impl.UnifiedInputPlatformServiceImpl;
//import com.zlhj.user.vo.MultipleLoanRepository;
//import com.zlhj.webank.business.entity.SpcCreditOtherInfoEntity;
//import com.zlhj.webank.business.entity.SpcCreditOtherInfoRepository;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Service;
//
//import java.sql.Timestamp;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.List;
//
//@Service
//@Slf4j
//public class JdJrServiceImpl implements JdJrService {
//
//	@Autowired
//	private UserObjectRepository userObjectRepository;
//
//	@Autowired
//	private SendEmailMessage sendEmailMessage;
//
//	@Autowired
//	private RemoteCluePlatformService remoteCluePlatformService;
//
//	@Autowired
//	private CreditAuthorizationFactory creditAuthorizationFactory;
//
//	@Autowired
//	private AuthorizedImageFactory authorizedImageFactory;
//
//	@Autowired
//	private AuthorizedImageRepository authorizedImageRepository;
//
//	@Autowired
//	private RemoteBigDataService remoteBigDataService;
//
//	@Autowired
//	private CreditAuthorizationRepository creditAuthorizationRepository;
//
//	@Autowired
//	private CarAgeMileageConfigRepository carAgeMileageConfigRepository;
//
//	@Autowired
//	private SplBussinessbasicMapper splBussinessbasicMapper;
//
//	@Autowired
//	private AdditionalFeeInformationRepository additionalFeeInformationRepository;
//
//	@Autowired
//	private BankApprovalRecordService bankApprovalRecordService;
//
//	@Autowired
//	private MultipleLoanRepository loanInfoRepository;
//
//	@Autowired
//	private CarInfoRepository carInfoRepository;
//
//	@Value("${zlhj.image.postUrlDomain}")
//	private String fileUrl;
//
//	@Autowired
//	private LoanInitialRepaymentScheduleMapper loanInitialRepaymentScheduleMapper;
//
//	@Autowired
//	private UnifiedInputPlatformServiceImpl unifiedInputPlatformService;
//
//	@Autowired
//	private SapdcslasRepository sapdcslasRepository;
//
//	@Autowired
//	private CreditLoanRepository creditLoanRepository;
//
//	@Autowired
//	private JDClueQueryBillService jdClueQueryBillService;
//
//	@Autowired
//	private PreliminaryScreeningService preliminaryScreeningService;
//
//	@Autowired
//	private CreditLoanService creditLoanService;
//
//	@Autowired
//	private AdditionalFeeRepayPlanRepository additionalFeeRepayPlanRepository;
//
//	@Autowired
//	private LicenseAccessWhitelistRepository licenseAccessWhitelistRepository;
//
//	@Autowired
//	private VinBlacklistRepository vinBlacklistRepository;
//
//	@Autowired
//	private CreditUserVORepository creditUserRepository;
//
//	@Autowired
//	private SpcCreditOtherInfoRepository creditOtherInfoRepository;
//
//	@Autowired
//	private BusinessJudgmentConditionService businessJudgmentConditionService;
//
//	@Autowired
//	private Sender sender;
//
//	@Autowired
//	private RedisTemplate<String, String> redisTemplate;
//
//	@Override
//	public void autoPreApprove(JdJrPreApproveMessage message) {
//
//	}
//
//	private void initialScreening(CreditAuthorization creditAuth) throws JdJtScreeningException {
//		// 车龄 > 12年
//		try {
//			// 车辆上牌日期
//			LocalDate registerDate = LocalDate.parse(creditAuth.getVehicleRegisterDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
//			// 允许最大车龄
//			LocalDate now = LocalDate.now();
//			LocalDate maxAllowedDate = now.minusYears(12);
//			if (registerDate.isBefore(maxAllowedDate)) {
//				throw new JdJtScreeningException("车龄不准入");
//			}
//		} catch (Exception e) {
//			throw new JdJtScreeningException("车龄不准入");
//		}
//
//		// 车牌前两位校验（京沪津渝除外）
//		String license = creditAuth.getLicensePlateNo();
//		if (StringUtils.isBlank(license) || license.length() < 2) {
//			throw new JdJtScreeningException("车牌不准入");
//		}
//		LicenseAccessWhitelist licenseAccessWhitelist = licenseAccessWhitelistRepository.selectByLicense(license);
//		if (licenseAccessWhitelist == null) {
//			throw new JdJtScreeningException("车牌不准入");
//		}
//
//		// 车架号校验
//		String vin = creditAuth.getChassisNumber();
//		// 长度必须为17位
//		if (vin.length() != 17) {
//			throw new JdJtScreeningException("车架号不存在");
//		}
//		// 只能包含数字和大写字母
//		if (!vin.matches("[0-9A-Z]+")) {
//			throw new JdJtScreeningException("车架号不存在");
//		}
//		// 不能包含 I, O, Q
//		if (vin.contains("I") || vin.contains("O") || vin.contains("Q")) {
//			throw new JdJtScreeningException("车架号不存在");
//		}
//		// 前8位是否在黑名单
//		VinBlacklist vinBlacklist = vinBlacklistRepository.selectByVin(vin);
//		if (vinBlacklist != null) {
//			throw new JdJtScreeningException("车品牌不准入");
//		}
//	}
//
//	@Override
//	public void preApproveRetry() {
//		String listRedisKey = "jdjr:auto:credit:approve:error";
//		Long size = this.redisTemplate.opsForList().size(listRedisKey);
//		if (size != null && size > 0) {
//			for (int i = 0; i < size; i++) {
//				String message = this.redisTemplate.opsForList().rightPop(listRedisKey);
//				if (com.zlhj.common.core.utils.StringUtils.isBlank(message)) {
//					continue;
//				}
//				try {
//					JdJrPreApproveMessage approveMessage = JSONObject.parseObject(message, JdJrPreApproveMessage.class);
//
//					this.autoPreApprove(approveMessage);
//				} catch (Exception e) {
//					log.error("[重试处理京东金融自动预审异常],异常原因={}", e.getMessage(), e);
//					String emailContent =
//							"重试处理京东金融自动预审异常，原因: " + e.getMessage() +
//									"，当前信息：" + message +
//									",请及时跟进处理！";
//					sendEmailMessage.sendEmail("重试京东金融自动预审异常", emailContent, "重试京东金融自动预审");
//					redisTemplate.opsForList().leftPush("jdjr:auto:credit:approve:error", message);
//				}
//			}
//		}
//	}
//}
