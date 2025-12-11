package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.autoCredit.universal.transform;

import com.apollo.alds.util.ConvertionUtil;
import com.apollo.org.vo.OrgUserVo;
import com.apollo.util.DateUtil;
import com.zlhj.infrastructure.po.CreditAuthorization;
import com.zlhj.infrastructure.routing.dto.clue.CluePreApproveApiRespDTO;
import com.zlhj.infrastructure.routing.dto.clue.CluePreApproveDTO;
import com.zlhj.infrastructure.routing.dto.clue.JdJtCluePreApproveDTO;
import com.zlhj.mq.dto.AntPreApproveMessage;
import com.zlhj.mq.dto.JdJtPreApproveMessage;
import com.zlhj.mq.dto.PreApproveMessage;
import org.springframework.util.Assert;

public class CreditAuthorizationTransform {

	public CreditAuthorization byDTO(CluePreApproveApiRespDTO preApproveApiRespDTO,
									 String applicationNo,
									 AntPreApproveMessage message,
									 OrgUserVo orgUserVo) {
		String idNum = message.getIdCard();
		String year = idNum.substring(6, 10);  // 截取年份
		String month = String.valueOf(Integer.parseInt(idNum.substring(10, 12))); // 去零
		String day = String.valueOf(Integer.parseInt(idNum.substring(12, 14)));    // 去零

		CreditAuthorization creditAuthorization = new CreditAuthorization();

		creditAuthorization.setApplicationNo(applicationNo);
		creditAuthorization.setCustomerName(message.getName());
		creditAuthorization.setIdType(1);
		creditAuthorization.setIdNum(idNum);
		creditAuthorization.setPhone(message.getPhone());
		creditAuthorization.setCreationDate(DateUtil.getNowDate());
		creditAuthorization.setBirthDate(year + "/" + month + "/" + day);
		//从身份证上取第17位的性别
		int sex = ConvertionUtil.getSimpleIntegerWithNull(idNum.substring(16, 17));
		creditAuthorization.setSex(sex % 2 == 1 ? 1 : 2);
		creditAuthorization.setLicensePlate(preApproveApiRespDTO.getVehicle().getLicenseNo().substring(0, 2));
		creditAuthorization.setAccountManagerId(orgUserVo.getM_ID());
		creditAuthorization.setAuthorizationStatus(0);
		creditAuthorization.setCreditAuthorizationCode("999");
		creditAuthorization.setTelCreditAuthorizationCode("98");
		creditAuthorization.setOrgId(orgUserVo.getM_OrgID());
		creditAuthorization.setChassisNumber(preApproveApiRespDTO.getVehicle().getVinNo());
		creditAuthorization.setLicensePlateNo(preApproveApiRespDTO.getVehicle().getLicenseNo());
//		creditAuthorization.setVehicleSeriesId(preApproveApiRespDTO.getVehicle().getSeriesId());
		creditAuthorization.setVehicleSeriesName(preApproveApiRespDTO.getVehicle().getSeriesName());
//		creditAuthorization.setVehicleModelId(preApproveApiRespDTO.getVehicle().getModelId());
		creditAuthorization.setVehicleModelName(preApproveApiRespDTO.getVehicle().getModelName());
		creditAuthorization.setVehicleRegisterDate(preApproveApiRespDTO.getVehicle().getRegDate());
		creditAuthorization.setBoId(preApproveApiRespDTO.getBoId());
		creditAuthorization.setAuthorizationDate(DateUtil.getNowDate());
		creditAuthorization.setVehicleBrandName(preApproveApiRespDTO.getVehicle().getBrandName());
//		creditAuthorization.setVehicleBrand(preApproveApiRespDTO.getVehicle().getBrandId());
		creditAuthorization.setVehicleEngineNo(preApproveApiRespDTO.getVehicle().getEngineNo());
		return creditAuthorization;

//		creditAuthorization.setAuthority("");
//		creditAuthorization.setCertificateValidity("");
//		creditAuthorization.setIdCardAddress("");
//		creditAuthorization.setMaritalStatus("");
//		creditAuthorization.setHaveChildren("");
//		creditAuthorization.setResidentialAddress("");
//		creditAuthorization.setOccupationalCategory("");
//		creditAuthorization.setOccupation("");
//		creditAuthorization.setApplicationAmount(new BigDecimal("0"));
//		creditAuthorization.setVehicleBrand("");
//		creditAuthorization.setAuthenticationMode(0);
//		creditAuthorization.setDepositBankId(0);
//		creditAuthorization.setBankName("");
//		creditAuthorization.setOcrBankName("");
//		creditAuthorization.setBankCardNumber("");
//		creditAuthorization.setAccountName("");
//		creditAuthorization.setHouseholdIdNum("");
//		creditAuthorization.setBankReservePhone("");
//		creditAuthorization.setScreeningResults("");
//		creditAuthorization.setScreeningTime(new Date());
//		creditAuthorization.setFrequentLoanerModel("0");
//		creditAuthorization.setCorporateName("");
//		creditAuthorization.setNation("");
//		creditAuthorization.setOccupationalCategoryPost("");
//		creditAuthorization.setManagerName("");
//		creditAuthorization.setManagerTel("");
//		creditAuthorization.setIsDisplayOrgUserInfo("");
//		creditAuthorization.setResidence("");
//		creditAuthorization.setCreditProvide("");
//		creditAuthorization.setAuthFlowId("");
//		creditAuthorization.setSignFlowId("");
//		creditAuthorization.setLocalProvince("");
//		creditAuthorization.setLocalCity("");
//		creditAuthorization.setLocalArea("");
//		creditAuthorization.setLocalAddress("");
//		creditAuthorization.setCreditInquiryNumber(0);
//		creditAuthorization.setFinalRejectNumber(0);
//		creditAuthorization.setCreditRefuseNumber(0);
//		creditAuthorization.setResidenceProvince("");
//		creditAuthorization.setResidenceCity("");
//		creditAuthorization.setResidenceArea("");
//		creditAuthorization.setVehicleMileage(0);
//		creditAuthorization.setVehicleLicenseFrontImage("");
//		creditAuthorization.setVehicleLicenseSubpageImage("");
//		creditAuthorization.setVehicleModelYear(0);
//		creditAuthorization.setVehicleModelPrice(new BigDecimal("0"));
//		creditAuthorization.setVehicleLicenseFileNo("");
//		creditAuthorization.setVehicleAllowNum("");
//		creditAuthorization.setVehicleTotalMass("");
//		creditAuthorization.setVehicleCurbWeight("");
//		creditAuthorization.setVehicleExternalSize("");
//		creditAuthorization.setVehicleVehicleType("");
//		creditAuthorization.setVehicleOwner("");
//		creditAuthorization.setVehicleSeriesName("");
//		creditAuthorization.setVehicleModelName("");
//		creditAuthorization.setVehicleBrandName("");
//		creditAuthorization.setIsGreen("");
//		creditAuthorization.setGreenType("");
//		creditAuthorization.setWorkProvince("");
//		creditAuthorization.setWorkCity("");
//		creditAuthorization.setWorkArea("");
//		creditAuthorization.setAuthErrorMsg("");
//		creditAuthorization.setModelLiter("");
//		creditAuthorization.setModelGear("");
//		creditAuthorization.setModelEmissionStandard("");
//		creditAuthorization.setTempH5("");
//		creditAuthorization.setDriverLicenseImage("");
//		creditAuthorization.setBankCardImage("");
//		creditAuthorization.setCustNum("");
	}

	public CreditAuthorization byJdJtDTO(JdJtCluePreApproveDTO data, String applicationNo, JdJtPreApproveMessage message, OrgUserVo orgUserVo) {
		Assert.hasLength(data.getUserName(),"姓名不能为空");
		Assert.hasLength(data.getCertNo(),"身份证号不能为空");
		Assert.hasLength(data.getMobile(),"手机号不能为空");
		Assert.hasLength(data.getCarLicense(),"车牌号不能为空");
		Assert.hasLength(data.getVehicleCode(),"车架号不能为空");
		String idNum = data.getCertNo();
		String year = idNum.substring(6, 10);  // 截取年份
		String month = String.valueOf(Integer.parseInt(idNum.substring(10, 12))); // 去零
		String day = String.valueOf(Integer.parseInt(idNum.substring(12, 14)));    // 去零

		CreditAuthorization creditAuthorization = new CreditAuthorization();
		creditAuthorization.setApplicationNo(applicationNo);
		creditAuthorization.setCustomerName(data.getUserName());
		creditAuthorization.setIdType(1);
		creditAuthorization.setIdNum(idNum);
		creditAuthorization.setPhone(data.getMobile());
		creditAuthorization.setCreationDate(DateUtil.getNowDate());
		creditAuthorization.setBirthDate(year + "/" + month + "/" + day);
		//从身份证上取第17位的性别
		int sex = ConvertionUtil.getSimpleIntegerWithNull(idNum.substring(16, 17));
		creditAuthorization.setSex(sex % 2 == 1 ? 1 : 2);
		creditAuthorization.setLicensePlate(data.getCarLicense().substring(0, 2));
		creditAuthorization.setAccountManagerId(orgUserVo.getM_ID());
		creditAuthorization.setAuthorizationStatus(0);
		creditAuthorization.setChannelCode(message.getChannelCode());
		creditAuthorization.setTelCreditAuthorizationCode("98");
		creditAuthorization.setCreditAuthorizationCode(message.getChannelCode().toString());
		creditAuthorization.setOrgId(orgUserVo.getM_OrgID());
		creditAuthorization.setChassisNumber(data.getVehicleCode());
		creditAuthorization.setLicensePlateNo(data.getCarLicense());
		creditAuthorization.setBoId(message.getBoId());
		creditAuthorization.setAuthorizationDate(DateUtil.getNowDate());
		creditAuthorization.setVehicleRegisterDate(data.getFirstCheckDate());
		return creditAuthorization;

	}

	public CreditAuthorization byDTO(CluePreApproveDTO data, String applicationNo, PreApproveMessage message, OrgUserVo orgUserVo) {
		Assert.hasLength(data.getUserName(),"姓名不能为空");
		Assert.hasLength(data.getCertNo(),"身份证号不能为空");
		Assert.hasLength(data.getMobile(),"手机号不能为空");
		Assert.hasLength(data.getCarLicense(),"车牌号不能为空");
		Assert.hasLength(data.getVehicleCode(),"车架号不能为空");
		String idNum = data.getCertNo();
		String year = idNum.substring(6, 10);  // 截取年份
		String month = String.valueOf(Integer.parseInt(idNum.substring(10, 12))); // 去零
		String day = String.valueOf(Integer.parseInt(idNum.substring(12, 14)));    // 去零

		CreditAuthorization creditAuthorization = new CreditAuthorization();
		creditAuthorization.setApplicationNo(applicationNo);
		creditAuthorization.setCustomerName(data.getUserName());
		creditAuthorization.setIdType(1);
		creditAuthorization.setIdNum(idNum);
		creditAuthorization.setPhone(data.getMobile());
		creditAuthorization.setCreationDate(DateUtil.getNowDate());
		creditAuthorization.setBirthDate(year + "/" + month + "/" + day);
		//从身份证上取第17位的性别
		int sex = ConvertionUtil.getSimpleIntegerWithNull(idNum.substring(16, 17));
		creditAuthorization.setSex(sex % 2 == 1 ? 1 : 2);
		creditAuthorization.setLicensePlate(data.getCarLicense().substring(0, 2));
		creditAuthorization.setAccountManagerId(orgUserVo.getM_ID());
		creditAuthorization.setAuthorizationStatus(0);
		creditAuthorization.setChannelCode(message.getChannelCode());
		creditAuthorization.setTelCreditAuthorizationCode("98");
		creditAuthorization.setCreditAuthorizationCode(message.getChannelCode().toString());
		creditAuthorization.setOrgId(orgUserVo.getM_OrgID());
		creditAuthorization.setChassisNumber(data.getVehicleCode());
		creditAuthorization.setLicensePlateNo(data.getCarLicense());
		creditAuthorization.setBoId(message.getBoId());
		creditAuthorization.setAuthorizationDate(DateUtil.getNowDate());
		creditAuthorization.setVehicleRegisterDate(data.getFirstCheckDate());
		return creditAuthorization;

	}
}