package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.autoCredit.core.handler;

import com.alibaba.fastjson.JSON;
import com.apollo.org.vo.OrgUserVo;
import com.apollo.org.vo.UserObjectRepository;
import com.apollo.util.DateUtil;
import com.common.LocalDateUtils;
import com.zlhj.commonLoan.business.appCommon.enums.ClueChanelCode;
import com.zlhj.commonLoan.business.common.pojo.Identity;
import com.zlhj.electronicCredit.pojo.CreditLoan;
import com.zlhj.electronicCredit.pojo.CreditLoanRepository;
import com.zlhj.electronicCredit.pojo.CreditUserVO;
import com.zlhj.electronicCredit.pojo.CreditUserVORepository;
import com.zlhj.infrastructure.po.*;
import com.zlhj.infrastructure.repository.*;
import com.zlhj.infrastructure.routing.RemoteBigDataService;
import com.zlhj.infrastructure.routing.dto.RemoteInterfaceRequest;
import com.zlhj.infrastructure.routing.dto.RemoteInterfaceResponse;
import com.zlhj.infrastructure.routing.dto.clue.CluePreApproveDTO;
import com.zlhj.loan.SendEmailMessage;
import com.zlhj.main.fumin.enums.BankOrgNameType;
import com.zlhj.mq.dto.PreApproveMessage;
import com.zlhj.unifiedInputPlatform.ant.dto.FddRASignVO;
import com.zlhj.unifiedInputPlatform.autoCredit.core.CreditServiceSupport;
import com.zlhj.unifiedInputPlatform.autoCredit.core.context.BoId;
import com.zlhj.unifiedInputPlatform.autoCredit.core.strategy.NotificationStrategy;
import com.zlhj.unifiedInputPlatform.autoCredit.core.validator.PreApproveValidator;
import com.zlhj.unifiedInputPlatform.autoCredit.dto.FddRASignDTO;
import com.zlhj.unifiedInputPlatform.autoCredit.exceptions.*;
import com.zlhj.webank.business.entity.SpcCreditOtherInfoEntity;
import com.zlhj.webank.business.entity.SpcCreditOtherInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
public abstract class BaseCreditPreApproveHandle<T> {
	@Autowired
	protected UserObjectRepository userObjectRepository;
	@Autowired
	protected CreditAuthorizationRepository creditAuthorizationRepository;
	@Autowired
	protected AuthorizedImageFactory authorizedImageFactory;
	@Autowired
	protected AuthorizedImageRepository authorizedImageRepository;
	@Autowired
	protected RemoteBigDataService remoteBigDataService;
	@Autowired
	protected SendEmailMessage sendEmailMessage;
	@Autowired
	protected RedisTemplate<String, String> redisTemplate;
	@Autowired
	private CarAgeMileageConfigRepository carAgeMileageConfigRepository;
	@Autowired
	protected CreditServiceSupport creditServiceSupport;
	@Autowired
	private CreditAuthorizationFactory creditAuthorizationFactory;
	@Autowired
	private CreditUserVORepository creditUserRepository;
	@Autowired
	private CreditLoanRepository creditLoanRepository;
	@Autowired
	private SpcCreditOtherInfoRepository creditOtherInfoRepository;
	@Autowired
	private List<PreApproveValidator<CluePreApproveDTO>> validators;
	@Value("${zlhj.image.postUrlDomain}")
	protected String fileUrl;

	public void executePreApprove(PreApproveMessage message) {
		CreditAuthorization creditAuthorization = new CreditAuthorization();
		try {
			OrgUserVo orgUserVo = userObjectRepository.getByUserId(Integer.valueOf(message.getUserCoreId()));
			if (orgUserVo == null) {
				throw new PreApproveException("未找到ID为" + message.getUserCoreId() + "的电销客户经理账号");
			}

			T remoteData = fetchAndValidateRemoteData(message);

			creditAuthorization = transformToLocalAuth(remoteData, message, orgUserVo);

			if (!customScreening(creditAuthorization)) {
				return;
			}

			if (!creditAuthorizationRepository.saveData(creditAuthorization)) {
				throw new RuntimeException("保存征信授权信息失败");
			}

			List<AuthorizedImage> savedImages = processImages(creditAuthorization, remoteData);

			startFddSignFlow(creditAuthorization, remoteData, savedImages);

		} catch (LicensePlateNotComplyException | VinNotComplyException | CarAgeNotMatchException e) {
			rejectVehicleInfo(creditAuthorization,e);
			notificationStrategy().onPreReject(new BoId(message.getBoId()), ClueChanelCode.getByCode(message.getChannelCode()));
		} catch (PreApproveException e) {
			notificationStrategy().onPreReject(new BoId(message.getBoId()), ClueChanelCode.getByCode(message.getChannelCode()));
		} catch (Exception e) {
			handleException(e, message);
		}
	}

	protected abstract T fetchAndValidateRemoteData(PreApproveMessage message) throws Exception;

	protected abstract CreditAuthorization transformToLocalAuth(T data, PreApproveMessage message, OrgUserVo userVo);

	protected abstract List<AuthorizedImage> processImages(CreditAuthorization auth, T data) throws Exception;

	protected abstract String channelName();

	protected Integer channelPartner(PreApproveMessage message){
		return message.getChannelCode();
	};

	protected String redisRetryKey() {
		return "auto:credit:approve:error";
	}
	protected abstract NotificationStrategy notificationStrategy();

	protected boolean customScreening(CreditAuthorization auth) {
		try {
			for (PreApproveValidator<CluePreApproveDTO> v : validators) {
				if (!v.supports(ClueChanelCode.getByCode(auth.getChannelCode()))) {
					continue;
				}
				v.validate(auth);
			}
			return true;

		} catch (Exception e) {
			log.error("京东金融 初筛逻辑执行异常", e);
			throw e;
		}
	}

	protected void startFddSignFlow(CreditAuthorization auth, T data, List<AuthorizedImage> images) {
		FddRASignDTO signDTO = buildFddSignDTO(auth, data, images);

		RemoteInterfaceResponse<FddRASignVO> response = remoteBigDataService.fddStartSignFlow(
				RemoteInterfaceRequest.create(signDTO, new Identity(auth.getId()))
		);

		if (!response.isSuccess() ||
				(response.getData() != null && "3".equals(response.getData().getState()))) {
			String msg = response.getData() != null ? response.getData().getMessage() : response.getMsg();
			throw new PreReviewFddSignException(msg != null ? msg : "法大大电子签失败");
		}

		CreditAuthorization updatePo = new CreditAuthorization();
		updatePo.setSignFlowId(response.getData().getFollowId());
		creditAuthorizationRepository.updateDataById(auth.getId(), updatePo);
	}


	protected abstract FddRASignDTO buildFddSignDTO(CreditAuthorization auth, T data, List<AuthorizedImage> images);


	protected void handleException(Exception e, PreApproveMessage message) {
		log.error("[处理{}自动预审异常], boId={}, 原因={}", channelName(), message.getBoId(), e.getMessage(), e);

		String emailContent = String.format("处理%s自动预审异常，原因: %s，当前线索编号：%s，报文：%s，请及时跟进！",
				channelName(), e.getMessage(), message.getBoId(), JSON.toJSONString(message));

		sendEmailMessage.sendEmailByType(channelName() + "自动预审异常", emailContent, "15");


		redisTemplate.opsForList().leftPush(redisRetryKey(), JSON.toJSONString(message));
	}

	protected AuthorizedImage saveAndStoreImage(Integer authId, String imageName, String imageType, String fileCode) {
		AuthorizedImage image = authorizedImageFactory.saveAuthorizedImage(authId, imageName, imageType, fileUrl + fileCode);
		authorizedImageRepository.saveDate(image);
		return image;
	}

	protected Integer vehicleMileage(CreditAuthorization auth) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate vehicleRegisterDate = LocalDate.parse(auth.getVehicleRegisterDate(), formatter);
		LocalDate authorizationDate = LocalDate.now();
		double diffYears = LocalDateUtils.getDiffYears(vehicleRegisterDate, authorizationDate);
		diffYears = (long) (diffYears * 100) / 100.0;
		return carAgeMileageConfigRepository.getMileageInRange(diffYears).intValue();
	}

	private void rejectVehicleInfo(CreditAuthorization auth,Exception e) {
		log.info("京东金融车辆信息拒绝，原因: " + e.getMessage());

		CreditUserVO creditUser = creditAuthorizationFactory.splicingCreditUserData(auth, BankOrgNameType.HCD.getKey(), 2, "电销拒绝");
		CreditLoan creditLoan = creditAuthorizationFactory.splicingCreditLoanData(auth, creditUser.getCreditOrderId(), BankOrgNameType.HCD.getKey(), 2, "电销拒绝");
		SpcCreditOtherInfoEntity creditOtherInfo = creditAuthorizationFactory.splicingOtherInfoData(auth, creditUser.getCreditOrderId());
		creditLoan.setScreenResult("初筛拒绝");
		creditLoan.setCreditErrorMsg(e.getMessage());
		creditLoan.setScreeningTime(new Timestamp(System.currentTimeMillis()));
		creditLoan.setBeforeApproveConclusion("3");
		auth.setScreeningResults("初筛拒绝");
		auth.setScreeningTime(DateUtil.getNowDate());

		creditUserRepository.saveData(creditUser);
		creditLoanRepository.saveData(creditLoan);
		creditOtherInfoRepository.saveCreditOtherInfo(creditOtherInfo);
		creditAuthorizationRepository.saveData(auth);
	}
}