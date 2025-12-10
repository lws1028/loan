package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.autoCredit.core;

import com.alibaba.fastjson.JSON;
import com.apollo.org.vo.OrgUserVo;
import com.apollo.org.vo.UserObjectRepository;
import com.common.LocalDateUtils;
import com.zlhj.common.core.utils.StringUtils;
import com.zlhj.commonLoan.business.clue.dto.LoanStatePushToClueDTO;
import com.zlhj.commonLoan.business.clue.enums.LoanStatusChangeEnum;
import com.zlhj.commonLoan.business.common.pojo.Identity;
import com.zlhj.infrastructure.po.*;
import com.zlhj.infrastructure.repository.*;
import com.zlhj.infrastructure.routing.RemoteBigDataService;
import com.zlhj.infrastructure.routing.dto.RemoteInterfaceRequest;
import com.zlhj.infrastructure.routing.dto.RemoteInterfaceResponse;
import com.zlhj.loan.SendEmailMessage;
import com.zlhj.mq.dto.PreApproveMessage;
import com.zlhj.unifiedInputPlatform.ant.dto.FddRASignDTO;
import com.zlhj.unifiedInputPlatform.ant.dto.FddRASignVO;
import com.zlhj.unifiedInputPlatform.autoCredit.exceptions.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

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
	private LicenseAccessWhitelistRepository licenseAccessWhitelistRepository;
	@Autowired
	private VinBlacklistRepository vinBlacklistRepository;
	@Autowired
	private CarAgeMileageConfigRepository carAgeMileageConfigRepository;
	@Value("${zlhj.image.postUrlDomain}")
	protected String fileUrl;

	public void executePreApprove(PreApproveMessage message) {
		try {
			OrgUserVo orgUserVo = userObjectRepository.getByUserId(Integer.valueOf(message.getUserCoreId()));
			if (orgUserVo == null) {
				throw new PreApproveException("未找到ID为" + message.getUserCoreId() + "的电销客户经理账号");
			}

			T remoteData = fetchAndValidateRemoteData(message);

			CreditAuthorization creditAuthorization = transformToLocalAuth(remoteData, message, orgUserVo);

			if (!customScreening(creditAuthorization, remoteData)) {
				return;
			}

			if (!creditAuthorizationRepository.saveData(creditAuthorization)) {
				throw new RuntimeException("保存征信授权信息失败");
			}

			List<AuthorizedImage> savedImages = processImages(creditAuthorization, remoteData);

			startFddSignFlow(creditAuthorization, remoteData, savedImages);

		} catch (PreApproveException e) {
			notification(new LoanStatePushToClueDTO(message.getBoId(), LoanStatusChangeEnum.AUTO_PRE_REJECT.getValue(),
					message.getChannelCode(), feedType()));
		} catch (Exception e) {
			handleException(e, message);
		}
	}
	protected void checkCarAge(CreditAuthorization auth) throws CarAgeNotMatchException {
		try {
			if (StringUtils.isBlank(auth.getVehicleRegisterDate())) {
				return;
			}
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			LocalDate vehicleRegisterDate = LocalDate.parse(auth.getVehicleRegisterDate(), formatter);
			LocalDate authorizationDate = LocalDate.now();
			double diffYears = LocalDateUtils.getDiffYears(vehicleRegisterDate, authorizationDate);
			diffYears = (long) (diffYears * 100) / 100.0;
			// 如果註冊日期早於准入線，說明車齡太大
			if (diffYears < 0 || diffYears > 99) {
				throw new CarAgeNotMatchException("车龄不在0-99之间");
			}
		} catch (CarAgeNotMatchException e) {
			throw e;
		} catch (Exception e) {
			log.error("检查车龄异常", e);
			throw e;
		}
	}

	protected void checkLicensePlate(CreditAuthorization creditAuth) {
		try {
			// 车辆上牌日期
			LocalDate registerDate = LocalDate.parse(creditAuth.getVehicleRegisterDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			// 允许最大车龄
			LocalDate now = LocalDate.now();
			LocalDate maxAllowedDate = now.minusYears(12);
			if (registerDate.isBefore(maxAllowedDate)) {
				throw new LicensePlateNotComplyException("车龄不准入");
			}
		} catch (Exception e) {
			throw new LicensePlateNotComplyException("车龄不准入");
		}

		String license = creditAuth.getLicensePlateNo();
		if (org.apache.commons.lang3.StringUtils.isBlank(license) || license.length() < 2) {
			throw new LicensePlateNotComplyException("车牌不准入");
		}
		LicenseAccessWhitelist licenseAccessWhitelist = licenseAccessWhitelistRepository.selectByLicense(license);
		if (licenseAccessWhitelist == null) {
			throw new LicensePlateNotComplyException("车牌不准入");
		}
	}

	protected void checkVin(CreditAuthorization creditAuth) {
		// 车架号校验
		String vin = creditAuth.getChassisNumber();
		// 长度必须为17位
		if (vin.length() != 17) {
			throw new VinNotComplyException("车架号不存在");
		}
		// 只能包含数字和大写字母
		if (!vin.matches("[0-9A-Z]+")) {
			throw new VinNotComplyException("车架号不存在");
		}
		// 不能包含 I, O, Q
		if (vin.contains("I") || vin.contains("O") || vin.contains("Q")) {
			throw new VinNotComplyException("车架号不存在");
		}
		// 前8位是否在黑名单
		VinBlacklist vinBlacklist = vinBlacklistRepository.selectByVin(vin);
		if (vinBlacklist != null) {
			throw new VinNotComplyException("车品牌不准入");
		}
	}

	protected abstract T fetchAndValidateRemoteData(PreApproveMessage message) throws Exception;

	protected abstract CreditAuthorization transformToLocalAuth(T data, PreApproveMessage message, OrgUserVo userVo);

	protected abstract List<AuthorizedImage> processImages(CreditAuthorization auth, T data) throws Exception;

	protected abstract String channelName();

	protected abstract String feedType();

	protected Integer channelPartner(PreApproveMessage message){
		return message.getChannelCode();
	};

	protected String redisRetryKey() {
		return "auto:credit:approve:error";
	}

	protected abstract void notification(LoanStatePushToClueDTO push);

	protected boolean customScreening(CreditAuthorization auth, T data) {
		return true;
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
}