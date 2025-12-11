package com.zlhj.unifiedInputPlatform.autoCredit.core.validator;

import com.zlhj.commonLoan.business.appCommon.enums.ClueChanelCode;
import com.zlhj.infrastructure.po.CreditAuthorization;
import com.zlhj.infrastructure.po.LicenseAccessWhitelist;
import com.zlhj.infrastructure.repository.LicenseAccessWhitelistRepository;
import com.zlhj.unifiedInputPlatform.autoCredit.exceptions.LicensePlateNotComplyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class LicensePlateValidator implements PreApproveValidator {

	@Autowired
	private LicenseAccessWhitelistRepository licenseAccessWhitelistRepository;

	@Override
	public void validate(CreditAuthorization creditAuth) {
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

	@Override
	public boolean supports(ClueChanelCode channelCode) {
		return ClueChanelCode.JDJR.code().equals(channelCode.code());
	}
}