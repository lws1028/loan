package com.zlhj.unifiedInputPlatform.ant.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AntRepaymentChangeNotifySendDTO {
	//蚂蚁
	private String outApplyNo;
	//我司
	private String orgApplyNo;
	//机构侧支用号
	private String orgDrawdownNo;
	//还款金额
	private String repaymentAmount;
	//资方还款单号
	private String finDrawdownNo;
	public AntRepaymentChangeNotifySendDTO(AntRepaymentChangeNotifyDTO dto) {
		this.orgApplyNo = dto.orgApplyNo();
		this.outApplyNo = dto.outApplyNo();
		this.orgDrawdownNo = dto.getOrgDrawdownNo();
		this.repaymentAmount = dto.getRepaymentAmount();
		this.finDrawdownNo = dto.getFinDrawdownNo();
	}
}
