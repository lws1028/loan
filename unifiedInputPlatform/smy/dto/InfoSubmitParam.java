package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.smy.dto;

import lombok.Data;

/**
 * 萨摩耶大额进件-信息提交参数
 *
 * @author : LXY
 * @date : 2023/6/2 16:19
 */
@Data
public class InfoSubmitParam {

    /**
     * 客户Id唯⼀标识
     */
    private String custId;

    /**
     * 姓名
     */
    private String custName;

    /**
     * 性别：00-女；01-男
     */
    private String gender;

    /**
     * 用户注册手机号
     */
    private String phoneNo;

    /**
     * 居住地市；XX市;其他
     */
    private String addressCity;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 额度
     */
    private double limit;

    /**
     * 房产：0-无；1-有；2-未知
     */
    private String house;

    /**
     * 房产二级资质：1-商品房；2-自建房；null或
     * 空字符串表示落地页没有收集
     */
    private String houseDetail;

    /**
     * 车辆二级资质：1-未抵押车；2-抵押车；null
     * 或空字符串表示落地页没有收集
     */
    private String carDetail;

    /**
     * 车：0-无；1-有；2-未知
     */
    private String car;

    /**
     * 保单：0-无；1-有；2-未知
     */
    private String insurance;

    /**
     * 公积金：0-无；1-有；2-未知
     */
    private String housingFund;

    /**
     * 公积金缴纳情况的二级资质：1-公积金缴纳6
     * 个月以内；2-公积金缴纳6个月以上；null或
     * 空字符串表示落地页没有收集
     */
    private String housingFundDetail;

    /**
     * 营业执照：0-无；1-有；2-未知
     */
    private String businessLicense;

    /**
     * 社保：0-无；1-有；2-未知
     */
    private String socialSecurity;

    /**
     * 信用卡：0-无；1-有；2-未知
     */
    private String hasCreditCard;

    /**
     * 信用等级：1-无逾期；2-2年内逾期小于3次；
     * 3-2年内逾期超过3次
     */
    private String creditLevel;

    /**
     * 扩展字段
     */
    private ExtraContent extraContent;
}
