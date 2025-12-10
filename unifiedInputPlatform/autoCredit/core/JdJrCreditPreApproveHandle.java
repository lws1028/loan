package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.autoCredit.core;

import com.apollo.org.vo.OrgUserVo;
import com.apollo.util.DateUtil;
import com.zlhj.common.core.domain.R;
import com.zlhj.common.core.utils.StringUtils;
import com.zlhj.commonLoan.business.basic.service.BusinessJudgmentConditionService;
import com.zlhj.commonLoan.business.clue.dto.LoanStatePushToClueDTO;
import com.zlhj.commonLoan.business.clue.enums.LoanStatusChangeEnum;
import com.zlhj.electronicCredit.pojo.CreditLoan;
import com.zlhj.electronicCredit.pojo.CreditLoanRepository;
import com.zlhj.electronicCredit.pojo.CreditUserVO;
import com.zlhj.electronicCredit.pojo.CreditUserVORepository;
import com.zlhj.infrastructure.po.AuthorizedImage;
import com.zlhj.infrastructure.po.CreditAuthorization;
import com.zlhj.infrastructure.po.CreditAuthorizationFactory;
import com.zlhj.infrastructure.routing.RemoteCluePlatformService;
import com.zlhj.infrastructure.routing.dto.clue.CluePreApproveDTO;
import com.zlhj.infrastructure.routing.dto.clue.JdJtClueFile;
import com.zlhj.infrastructure.routing.dto.clue.JdJtClueFileType;
import com.zlhj.main.fumin.enums.BankOrgNameType;
import com.zlhj.mq.dto.PreApproveMessage;
import com.zlhj.mq.provider.Sender;
import com.zlhj.unifiedInputPlatform.ant.dto.FddRASignDTO;
import com.zlhj.unifiedInputPlatform.ant.dto.assembler.FddRASignDTOAssembler;
import com.zlhj.unifiedInputPlatform.ant.transform.CreditAuthorizationTransform;
import com.zlhj.unifiedInputPlatform.autoCredit.exceptions.CarAgeNotMatchException;
import com.zlhj.unifiedInputPlatform.autoCredit.exceptions.LicensePlateNotComplyException;
import com.zlhj.unifiedInputPlatform.autoCredit.exceptions.PreApproveException;
import com.zlhj.unifiedInputPlatform.autoCredit.exceptions.VinNotComplyException;
import com.zlhj.unifiedInputPlatform.universal.service.impl.UnifiedInputPlatformServiceImpl;
import com.zlhj.webank.business.entity.SpcCreditOtherInfoEntity;
import com.zlhj.webank.business.entity.SpcCreditOtherInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service("jdJrCreditPreApproveHandle")
public class JdJrCreditPreApproveHandle extends BaseCreditPreApproveHandle<CluePreApproveDTO> {

	@Autowired
	private RemoteCluePlatformService remoteCluePlatformService;
	@Autowired
	private CreditAuthorizationFactory creditAuthorizationFactory;
	@Autowired
	private BusinessJudgmentConditionService businessJudgmentConditionService;
	@Autowired
	private CreditUserVORepository creditUserRepository;
	@Autowired
	private CreditLoanRepository creditLoanRepository;
	@Autowired
	private SpcCreditOtherInfoRepository creditOtherInfoRepository;
	@Autowired
	private UnifiedInputPlatformServiceImpl unifiedInputPlatformService;
	@Autowired
	private Sender sender;

	@Override
	protected CluePreApproveDTO fetchAndValidateRemoteData(PreApproveMessage message) throws Exception {
		if (StringUtils.isBlank(message.getBoId())) {
			throw new PreApproveException("线索不能为空");
		}
		R<CluePreApproveDTO> response = remoteCluePlatformService.queryJdJrApprove(message.getBoId());
		if (200 != response.getCode()) {
			throw new PreApproveException("查询京东金融审批信息失败: " + response.getMsg());
		}
		CluePreApproveDTO preApproveDTO = response.getData();
		if (preApproveDTO == null) {
			throw new PreApproveException("查询京东金融审批信息为空");
		}
		if (StringUtils.isBlank(preApproveDTO.getUserName())) {
			throw new PreApproveException("用户姓名不能为空");
		}
		if (StringUtils.isBlank(preApproveDTO.getCertNo())) {
			throw new PreApproveException("身份证号不能为空");
		}
		if (StringUtils.isBlank(preApproveDTO.getMobile())) {
			throw new PreApproveException("手机号不能为空");
		}
		if (StringUtils.isBlank(preApproveDTO.getCarLicense())) {
			throw new PreApproveException("车牌号不能为空");
		}
		if (CollectionUtils.isEmpty(preApproveDTO.getFileList())) {
			throw new PreApproveException("文件不能为空");
		}
		return response.getData();
	}

	@Override
	protected CreditAuthorization transformToLocalAuth(CluePreApproveDTO data, PreApproveMessage message, OrgUserVo userVo) {
		String applicationNo = creditAuthorizationFactory.splicingApplicationNo();

		CreditAuthorization creditAuthorization = new CreditAuthorizationTransform().byDTO(data, applicationNo, message, userVo);
		creditAuthorization.setVehicleMileage(vehicleMileage(creditAuthorization));
		return creditAuthorization;
	}

	@Override
	protected boolean customScreening(CreditAuthorization auth, CluePreApproveDTO data) {

		try {
			checkCarAge(auth);
			Boolean isOpen = businessJudgmentConditionService.searchSwitchState("jdjrVehicleInformationRejectionFront");
			if (Boolean.FALSE.equals(isOpen)) {
				return true;
			}
			checkLicensePlate(auth);
			checkVin(auth);

			return true;
		} catch (LicensePlateNotComplyException | VinNotComplyException e) {
			log.info("京东金条车辆信息拒绝，原因: " + e.getMessage());

			// 构造征信数据
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

			LoanStatePushToClueDTO pre = new LoanStatePushToClueDTO(auth.getBoId(),
					LoanStatusChangeEnum.AUTO_PRE_REJECT.getValue(),
					23, feedType(), e.getMessage());

			notification(pre);
			return false;
		} catch (CarAgeNotMatchException e) {
			auth.setScreeningResults("初筛拒绝");
			auth.setScreeningTime(DateUtil.getNowDate());
			creditAuthorizationRepository.saveData(auth);

			LoanStatePushToClueDTO pre = new LoanStatePushToClueDTO(auth.getBoId(),
					LoanStatusChangeEnum.AUTO_PRE_REJECT.getValue(),
					23, feedType());

			notification(pre);
			return false;
		}  catch (Exception e) {
			log.error("京东金融 初筛逻辑执行异常", e);
			throw e;
		}
	}

	@Override
	protected List<AuthorizedImage> processImages(CreditAuthorization auth, CluePreApproveDTO data) throws Exception {
		List<AuthorizedImage> result = new ArrayList<>();


		List<JdJtClueFile> files = data.getFileList();

		JdJtClueFile sfzzm = files.stream().filter(f -> JdJtClueFileType.sfzzm.getCode().equals(f.getFileType())).findFirst()
				.orElseThrow(() -> new PreApproveException("身份证正面缺失"));
		JdJtClueFile sfzfm = files.stream().filter(f -> JdJtClueFileType.sfzzf.getCode().equals(f.getFileType())).findFirst()
				.orElseThrow(() -> new PreApproveException("身份证反面缺失"));
		JdJtClueFile rxzp = files.stream().filter(f -> JdJtClueFileType.rxzp.getCode().equals(f.getFileType())).findFirst()
				.orElseThrow(() -> new PreApproveException("人像照片缺失"));


		result.add(saveAndStoreImage(auth.getId(), "身份证正面", "sfzzm", fileUrl + sfzzm.getFileCode()));
		result.add(saveAndStoreImage(auth.getId(), "身份证反面", "sfzfm", fileUrl + sfzfm.getFileCode()));
		result.add(saveAndStoreImage(auth.getId(), "人像照片", "rxzp", fileUrl + rxzp.getFileCode()));

		return result;
	}

	@Override
	protected FddRASignDTO buildFddSignDTO(CreditAuthorization auth, CluePreApproveDTO data, List<AuthorizedImage> images) {

		AuthorizedImage sfzzm = images.stream().filter(i -> "sfzzm".equals(i.getFileCodeValue())).findFirst().orElse(null);
		AuthorizedImage sfzfm = images.stream().filter(i -> "sfzfm".equals(i.getFileCodeValue())).findFirst().orElse(null);
		AuthorizedImage rxzp = images.stream().filter(i -> "rxzp".equals(i.getFileCodeValue())).findFirst().orElse(null);

		return new FddRASignDTOAssembler().toDto(auth, data, sfzzm, sfzfm, rxzp);
	}

	@Override
	protected String channelName() {
		return "京东金融";
	}

	@Override
	protected String feedType() {
		return "JD_NOTICE_AUTO_FINANCE";
	}

	@Override
	protected void notification(LoanStatePushToClueDTO push) {
		unifiedInputPlatformService.realTimeInteraction(push);
		sender.clueStatusNotify(push);
	}
}