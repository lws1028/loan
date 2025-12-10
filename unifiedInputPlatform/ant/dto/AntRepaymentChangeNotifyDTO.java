package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.ant.dto;

import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class AntRepaymentChangeNotifyDTO {
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

	public String orgApplyNo() {
		if (StringUtils.hasLength(outApplyNo) && outApplyNo.contains("ZQHT")){
			return outApplyNo;
		}
		return orgApplyNo;
	}

	public String outApplyNo() {
		if (StringUtils.hasLength(outApplyNo) && outApplyNo.contains("ZQHT")){
			return "";
		}
		return outApplyNo;
	}
}
