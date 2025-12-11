package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.autoCredit.core.validator;

import com.zlhj.commonLoan.business.appCommon.enums.ClueChanelCode;
import com.zlhj.infrastructure.po.CreditAuthorization;
import com.zlhj.infrastructure.po.VinBlacklist;
import com.zlhj.infrastructure.repository.VinBlacklistRepository;
import com.zlhj.infrastructure.routing.dto.clue.CluePreApproveDTO;
import com.zlhj.unifiedInputPlatform.autoCredit.exceptions.VinNotComplyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VinValidator implements PreApproveValidator<CluePreApproveDTO> {
	@Autowired
	private VinBlacklistRepository vinBlacklistRepository;

	@Override
	public void validate(CreditAuthorization creditAuth, CluePreApproveDTO data) {
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

	@Override
	public boolean supports(ClueChanelCode channelCode) {
		return ClueChanelCode.JDJR.code().equals(channelCode.code());
	}
}