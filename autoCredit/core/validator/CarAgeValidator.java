package com.zlhj.unifiedInputPlatform.autoCredit.core.validator;

import com.common.LocalDateUtils;
import com.zlhj.common.core.utils.StringUtils;
import com.zlhj.commonLoan.business.appCommon.enums.ClueChanelCode;
import com.zlhj.infrastructure.po.CreditAuthorization;
import com.zlhj.infrastructure.routing.dto.clue.CluePreApproveDTO;
import com.zlhj.unifiedInputPlatform.autoCredit.exceptions.CarAgeNotMatchException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class CarAgeValidator implements PreApproveValidator<CluePreApproveDTO> {
	@Override
	public void validate(CreditAuthorization auth) {
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
	}

	@Override
	public boolean supports(ClueChanelCode channelCode) {
		return ClueChanelCode.JDJR.code().equals(channelCode.code());
	}
}