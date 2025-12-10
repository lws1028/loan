package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.ant.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ClueAdditionalDTO {
	/**
	 * 线索编号
	 */
	private String boId;

	/**
	 * 传输信息列表枚举
	 * borrower_marital_info: 借款人婚姻信息
	 * borrower_company_info: 借款人所在单位信息
	 * associated_person_list: 借款人联系人信息
	 * payment_log_list: 交易流水信息
	 */
	private List<String> categoryList;
	/**
	 * 借款人婚姻信息
	 */
	private BorrowerMaritalInfo borrowerMaritalInfo;
	/**
	 * 借款人单位信息
	 */
	private BorrowerCompanyInfo borrowerCompanyInfo;
	/**
	 * 借款人联系人信息
	 */
	private List<AssociatedPerson> associatedPersonList;
	/**
	 * 交易流水信息
	 */
	private List<PaymentLogInfo> paymentLogList;

	@Data
	public static class BorrowerMaritalInfo {
		/**
		 * 婚姻状况枚举:
		 * 10-未婚 20-已婚
		 * 22-再婚 30-丧偶
		 * 40-离异
		 */
		@JSONField(name = "marital_status")
		private String maritalStatus;
	}

	@Data
	public static class BorrowerCompanyInfo {
		/**
		 * 单位名称
		 */
		@JSONField(name = "company_name")
		private String companyName;
		/**
		 * 单位所在省
		 */
		@JSONField(name = "company_province")
		private String companyProvince;
		/**
		 * 单位所在省国标码
		 */
		@JSONField(name = "company_province_code")
		private String companyProvinceCode;
		/**
		 * 单位所在市
		 */
		@JSONField(name = "company_city")
		private String companyCity;
		/**
		 * 单位所在市省国标码
		 */
		@JSONField(name = "company_city_code")
		private String companyCityCode;
	}

	@Data
	public static class AssociatedPerson {
		/**
		 * 姓名
		 */
		@JSONField(name = "ap_name")
		private String apName;
		/**
		 * 关系, 枚举值:
		 * PARENTS: 父母
		 * CHILDREN: 子女
		 * SPOUSE: 配偶
		 * RELATIVES: 亲戚
		 * COLLEAGUES: 同事
		 * FRIENDS: 朋友
		 * LANDLORD: 房东
		 */
		@JSONField(name = "relationship")
		private String relationship;
		/**
		 * 联系人手机号
		 */
		@JSONField(name = "ap_mobile")
		private String apMobile;
	}

	@Data
	public static class PaymentLogInfo {
		/**
		 * 流水类型枚举:
		 * WECHAT_PAYMENT: 微信流水
		 * BANK_PAYMENT: 银行流水
		 * ALIPAY_PAYMENT: 支付宝流水
		 */
		@JSONField(name = "payment_category")
		private String paymentCategory;
		/**
		 * 交易账户名称
		 */
		@JSONField(name = "payment_account_name")
		private String paymentAccountName;
		/**
		 * 交易账户
		 */
		@JSONField(name = "payment_account_no")
		private String paymentAccountNo;
		/**
		 * PDF文件目录
		 */
		@JSONField(name = "pdf_file_dir")
		private String pdfFileDir;
		/**
		 * 开始年月
		 */
		@JSONField(name = "start_month")
		private String startMonth;
		/**
		 * 结束年月
		 */
		@JSONField(name = "end_month")
		private String endMonth;
		/**
		 * 近6个月代发工资, 单位分
		 */
		@JSONField(name = "salary_in_last_six_months")
		private Long salaryInLastSixMonths;
		/**
		 * 最近季度结息, 单位分
		 */
		@JSONField(name = "interest_in_last_quarter")
		private Long interestInLastQuarter;
		/**
		 * 最近第二季度结息, 单位分
		 */
		@JSONField(name = "interest_in_second_last_quarter")
		private Long interestInSecondLastQuarter;
		/**
		 * 交易列表
		 */
		@JSONField(name = "payment_list")
		private List<PaymentLogVo> paymentList;
	}

	@Data
	public static class PaymentLogVo {
		/**
		 * 开始日期
		 */
		@JSONField(name = "start_date")
		private String startDate;
		/**
		 * 结束日期
		 */
		@JSONField(name = "end_date")
		private String endDate;
		/**
		 * 支出总金额, 单位分
		 */
		@JSONField(name = "expense_amount")
		private String expenseAmount;
		/**
		 * 支出次数
		 */
		@JSONField(name = "expense_count")
		private String expenseCount;
		/**
		 * 收入总金额, 单位分
		 */
		@JSONField(name = "income_amount")
		private String incomeAmount;
		/**
		 * 收入次数
		 */
		@JSONField(name = "income_count")
		private String incomeCount;

		public String expenseAmount() {
			return StringUtils.isEmpty(expenseAmount) ? "0" : expenseAmount;
		}

		public String incomeAmount() {
			return StringUtils.isEmpty(incomeAmount) ? "0" : incomeAmount;
		}
	}
}
