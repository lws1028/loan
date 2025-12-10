package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.ant.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ClueDTO {
	/**
	 * 线索编号
	 */
	private String boId;

	private ClueSpecification clueSpecification;
	/**
	 * 申请单类型
	 */
	private String applyType;
	/**
	 * 是否已完成身份认证
	 */
	private Boolean identityVerified;
	/**
	 * 线索渠道编号
	 */
	private String channelPartnerCode;

	private Borrower borrower;

	private Area area;

	private Car car;

	private Approve approve;

	private ClueExpansionInfo clueExpansionInfo;

	@Data
	public static class ClueSpecification {
		/**
		 * 线索分类
		 */
		private String clueChannelCategory;
		/**
		 * 申请贷款金额：单位元，最多两位小数
		 */
		private BigDecimal amount;
	}

	@Data
	public static class Borrower {
		/**
		 * 申请人姓名
		 */
		private String name;
		/**
		 * 申请人手机号
		 */
		private String phone;
		/**
		 * 身份证
		 */
		private String idCard;
		/**
		 * 用户id
		 */
		private String userId;
	}

	@Data
	public static class Area {
		/**
		 * 申请人所在省份
		 */
		private String province;
		/**
		 * 申请人所在城市
		 */
		private String city;
	}

	@Data
	public static class Car {
		/**
		 * 车牌号
		 */
		private String license;
		/**
		 * 车辆年份
		 */
		private String year;
		/**
		 * 车辆品牌名称
		 */
		private String brandName;
		/**
		 * 车辆车系名称
		 */
		private String seriesName;
		/**
		 * 车辆车型名称
		 */
		private String modelName;
		/**
		 * 车辆里程数：单位公里
		 */
		private Double mileage;
		/**
		 * 车辆上牌日期
		 */
		private LocalDate licenseDate;
		/**
		 * 裸车购买价：单位元
		 */
		private BigDecimal price;
		/**
		 * 汽车情况：如全款、按揭
		 */
		private String purchaseMethod;
		/**
		 * 车架号
		 */
		private String vin;
		/**
		 * 估价单号
		 */
		private String evaluateNo;
	}

	@Data
	public static class Approve {
		/**
		 * 审批结果
		 */
		private Integer code;
		/**
		 * 结果描述
		 */
		private String desc;
	}

	@Data
	public static class ClueExpansionInfo {
		/**
		 * 预审单号
		 */
		private String preNo;
		/**
		 * 产品ID
		 */
		private String productId;
		/**
		 * 人脸照片采集时间
		 */
		private LocalDateTime authDate;
		/**
		 * 坐席切量标识
		 */
		private String sitesFlowMark;
		/**
		 * 坐席账号
		 */
		private String sitesAccount;
	}

}