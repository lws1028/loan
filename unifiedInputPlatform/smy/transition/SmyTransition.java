package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.smy.transition;

import com.apollo.alds.util.ConvertionUtil;
import com.zlhj.commonLoan.util.StringUtil;
import com.zlhj.externalChannel.vo.SplBussinessBasicObject;
import com.zlhj.externalChannel.vo.SplUserInfoObject;
import com.zlhj.unifiedInputPlatform.smy.dto.ExtraContent;
import com.zlhj.unifiedInputPlatform.smy.dto.InfoSubmitParam;
import com.zlhj.unifiedInputPlatform.smy.entity.SmyLoanCityConfiguration;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 萨摩耶大额导流 转换对象
 *
 * @author LXY
 * 2023年6月2日16:58:21
 */
@Component
public class SmyTransition {

    /**
     * 合作渠道 - 待定 - 萨摩耶 （进件贷款基础信息表码值）
     */
    private final static int BUSINESS_CHANNEL_PARTNER = 14;
    /**
     * 车抵贷 - 3
     */
    private final static int AUTOMOBILE_MORTGAGE = 3;
    /**
     * 直客申请模式 1-简易模式
     */
    private final static int EAST_MODEL = 1;
    /**
     * 状态 0-未受理
     */
    private final static int NOT_ACCEPT = 0;
    /**
     * 默认期数
     */
    private final static int DEFAULT_TERM = 36;

    /**
     * 萨摩耶大额导流对象 转换为 进件贷款基础信息对象
     */
    public SplBussinessBasicObject translateSplBussiness(InfoSubmitParam infoSubmitParam, SmyLoanCityConfiguration smyLoanCityConfiguration) {
        SplBussinessBasicObject sbbo = new SplBussinessBasicObject();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        sbbo.setApplyNum(infoSubmitParam.getCustId());
        sbbo.setLoanState(NOT_ACCEPT);
        sbbo.setChannelSource(BUSINESS_CHANNEL_PARTNER);
        String remark = translateRemark(infoSubmitParam);
        ExtraContent extraContent = infoSubmitParam.getExtraContent();
        if (extraContent != null) {
            String carId = extraContent.getCarId();
            if (StringUtil.isNotEmpty(carId)) {
                sbbo.setRemark1(carId);
            }
        }
        sbbo.setRemark3(remark);
        sbbo.setProvince(smyLoanCityConfiguration.getSuperiorId());
        sbbo.setCity(smyLoanCityConfiguration.getCityId());
        sbbo.setBussinessType(AUTOMOBILE_MORTGAGE);
        sbbo.setCustomerModel(EAST_MODEL);
        sbbo.setLoanTerm(DEFAULT_TERM);
        sbbo.setLoanPrice(infoSubmitParam.getLimit());
        sbbo.setSubmitDateTi(new Timestamp(date.getTime()));
        sbbo.setSubmitDate(ConvertionUtil.getSimpleIntegerWithNull(sdf.format(date)));
        return sbbo;
    }

    /**
     * 萨摩耶大额导流对象 转换为 外部推送人员信息对象
     */
    public SplUserInfoObject translateSplUserInfo(InfoSubmitParam infoSubmitParam, SmyLoanCityConfiguration smyLoanCityConfiguration) {
        SplUserInfoObject suio = new SplUserInfoObject();
        suio.setUcity(Integer.parseInt(smyLoanCityConfiguration.getSuperiorId()));
        suio.setUprovince(Integer.parseInt(smyLoanCityConfiguration.getCityId()));
        suio.setPhone(infoSubmitParam.getPhoneNo());
        suio.setCustomerName(infoSubmitParam.getCustName());
        return suio;
    }

    /**
     * 备注转换
     *
     * @param infoSubmitParam
     * @return
     */
    private String translateRemark(InfoSubmitParam infoSubmitParam) {

        StringBuffer remark = new StringBuffer();
        String gender = genderTranslate(infoSubmitParam.getGender());
        if (StringUtil.isNotEmpty(gender)) {
            remark.append("性别：" + gender + "；");
        }
        if (infoSubmitParam.getAge() != null) {
            remark.append("年龄：" + infoSubmitParam.getAge() + "；");
        }

        String house = codeTranslate(infoSubmitParam.getHouse());
        if (StringUtil.isNotEmpty(house)) {
            remark.append("房产：" + house);
            if (StringUtil.isNotEmpty(infoSubmitParam.getHouseDetail())) {
                String houseDetail = houseTwoTranslate(infoSubmitParam.getHouseDetail());
                if (StringUtil.isNotEmpty(houseDetail)) {
                    remark.append("-" + houseDetail + "；");
                } else {
                    remark.append("；");
                }
            } else {
                remark.append("；");
            }
        }

        String car = codeTranslate(infoSubmitParam.getCar());
        if (StringUtil.isNotEmpty(car)) {
            remark.append("汽车：" + car + "；");
        }

        String insurance = codeTranslate(infoSubmitParam.getInsurance());
        if (StringUtil.isNotEmpty(insurance)) {
            remark.append("保单：" + insurance + "；");
        }

        String housingFund = codeTranslate(infoSubmitParam.getHousingFund());
        if (StringUtil.isNotEmpty(housingFund)) {
            remark.append("公积金：" + housingFund);
            if (StringUtil.isNotEmpty(infoSubmitParam.getHousingFundDetail())) {
                String housingFundDetail = housingFundDetailTranslate(infoSubmitParam.getHousingFundDetail());
                if (StringUtil.isNotEmpty(housingFundDetail)) {
                    remark.append("-" + housingFundDetail + "；");
                } else {
                    remark.append("；");
                }
            } else {
                remark.append("；");
            }
        }

        String businessLicense = codeTranslate(infoSubmitParam.getBusinessLicense());
        if (StringUtil.isNotEmpty(businessLicense)) {
            remark.append("营业执照：" + businessLicense + "；");
        }

        String socialSecurity = codeTranslate(infoSubmitParam.getSocialSecurity());
        if (StringUtil.isNotEmpty(socialSecurity)) {
            remark.append("社保：" + socialSecurity + "；");
        }

        if (StringUtil.isNotEmpty(infoSubmitParam.getHasCreditCard())){
            String hasCreditCard = codeTranslate(infoSubmitParam.getHasCreditCard());
            if (StringUtil.isNotEmpty(hasCreditCard)) {
                remark.append("信用卡：" + hasCreditCard + "；");
            }
        }

        ExtraContent extraContent = infoSubmitParam.getExtraContent();
        if (extraContent != null) {
            String carId = extraContent.getCarId();
            if (StringUtil.isNotEmpty(carId)) {
                remark.append("车牌：" + carId + "；");
            }
            String carMoney = extraContent.getCarMoney();
            if (StringUtil.isNotEmpty(carMoney)) {
                remark.append("车辆价值：" + carMoney + "；");
            }
            if (StringUtil.isNotEmpty(extraContent.getPledge())){
                String pledge = pledgeTranslate(extraContent.getPledge());
                if (StringUtil.isNotEmpty(pledge)) {
                    remark.append("车辆抵押状态：" + pledge + "；");
                }
            }
            String creditType = extraContent.getCreditType();
            if (StringUtil.isNotEmpty(creditType)) {
                remark.append("信用情况：" + creditType + "；");
            }
        }
        return remark.toString();
    }

    /**
     * 性别转换
     *
     * @param param
     * @return
     */
    private String genderTranslate(String param) {
        String value = "";
        switch (param) {
            case "00":
                value = "女";
                break;
            case "01":
                value = "男";
                break;
            default:
                break;
        }
        return value;
    }

    /**
     * 转换
     * 房产：0-无；1-有；2-未知
     * 车：0-无；1-有；2-未知
     * 保单：0-无；1-有；2-未知
     * 公积金：0-无；1-有；2-未知
     * 营业执照：0-无；1-有；2-未知
     * 社保：0-无；1-有；2-未知
     * 信用卡：0-无；1-有；2-未知
     *
     * @param param
     * @return
     */
    private String codeTranslate(String param) {
        String value = "";
        switch (param) {
            case "0":
                value = "无";
                break;
            case "1":
                value = "有";
                break;
            case "2":
                value = "未知";
                break;
            default:
                break;
        }
        return value;
    }


    /**
     * 房产二级资质转换
     * 1-商品房；2-自建房；null或
     * 空字符串表示落地页没有收集
     *
     * @param param
     * @return
     */
    private String houseTwoTranslate(String param) {
        String value = "";
        switch (param) {
            case "1":
                value = "商品房";
                break;
            case "2":
                value = "自建房";
                break;
            default:
                break;
        }
        return value;
    }

    /**
     * 公积金缴纳情况的二级资质
     * 1-公积金缴纳6个月以内；2-公积金缴纳6个月以上
     *
     * @param param
     * @return
     */
    private String housingFundDetailTranslate(String param) {
        String value = "";
        switch (param) {
            case "1":
                value = "公积金缴纳6个月以内";
                break;
            case "2":
                value = "公积金缴纳6个月以上";
                break;
            default:
                break;
        }
        return value;
    }

    /**
     * 公积金缴纳情况的二级资质
     * 1-公积金缴纳6个月以内；2-公积金缴纳6个月以上
     *
     * @param param
     * @return
     */
    private String pledgeTranslate(String param) {
        String value = "";
        switch (param) {
            case "1":
                value = "抵押中";
                break;
            case "2":
                value = "未抵押";
                break;
            default:
                break;
        }
        return value;
    }
}
