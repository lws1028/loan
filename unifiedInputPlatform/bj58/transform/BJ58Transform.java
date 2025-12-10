package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.bj58.transform;

import com.apollo.alds.util.ConvertionUtil;
import com.zlhj.entity.BusinessBasicEntity;
import com.zlhj.externalChannel.vo.SplBussinessBasicObject;
import com.zlhj.externalChannel.vo.SplUserInfoObject;
import com.zlhj.jd.vo.DirectCustomerStatusDto;
import com.zlhj.unifiedInputPlatform.bj58.dto.BJ58Node;
import com.zlhj.unifiedInputPlatform.bj58.dto.CluesInformationRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 58实体类 转换对象
 */
@Component
public class BJ58Transform {

    //合作渠道 - 3 - 58同城 （直客状态表码值）
    private final static int STATUS_CHANNEL_PARTNER_58 = 3;
    //合作渠道 - 13 - 58同城 （进件贷款基础信息表码值）
    private final static int BUSINESS_CHANNEL_PARTNER_58 = 13;
    //车抵贷 - 3
    private final static int AUTOMOBILE_MORTGAGE = 3;
    //直客申请模式 1-简易模式
    private final static int EAST_MODEL = 1;
    //状态 0-未受理
    private final static int NOT_ACCEPT = 0;
    //默认期数
    private final static int DEFAULT_TERM = 36;
    //默认金额
    private final static BigDecimal DEFAULT_AMOUNT = BigDecimal.ONE;

    public int getChannelPartner58() {
        return STATUS_CHANNEL_PARTNER_58;
    }

    public int getBusinessChannelPartner58() {
        return BUSINESS_CHANNEL_PARTNER_58;
    }

    /**
     * 58线索信息结构对象 转换为 直客状态对象
     */
    public DirectCustomerStatusDto cir2Dcsd(CluesInformationRequest request) {
        DirectCustomerStatusDto dcsd = new DirectCustomerStatusDto();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        dcsd.setSplApplyNum(request.getOrderId()+"");
        dcsd.setSplChannelPartner(STATUS_CHANNEL_PARTNER_58);
        dcsd.setSplCreatetime(timestamp);
        dcsd.setSplMaxActionNum(BJ58Node.start().getStatus()+"");
        dcsd.setSplLoanBusinessStatus(NOT_ACCEPT+"");
        dcsd.setSplUpdateTime(timestamp);
        return dcsd;
    }

    /**
     * 58线索信息结构对象 转换为 进件贷款基础信息对象
     */
    public BusinessBasicEntity cir2Bbe(CluesInformationRequest request) {
        BusinessBasicEntity bbe = new BusinessBasicEntity();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        bbe.setApplyNum(request.getOrderId()+"");
        bbe.setLoanState(NOT_ACCEPT);
        bbe.setChannelPartner(BUSINESS_CHANNEL_PARTNER_58);
        bbe.setRemark1(request.getLicensePlateNum());
        bbe.setProvince(request.getProvinceCode());
        bbe.setCity(request.getCityCode());
        bbe.setBusinessType(AUTOMOBILE_MORTGAGE);
        bbe.setCustomerModal(EAST_MODEL);
        bbe.setLoanTerm(DEFAULT_TERM);
        bbe.setLoanPrice(DEFAULT_AMOUNT);
        bbe.setSubmitDateTi(new Timestamp(date.getTime()));
        bbe.setSubmitDate(ConvertionUtil.getSimpleIntegerWithNull(sdf.format(date)));
        return bbe;
    }

    /**
     * 58线索信息结构对象 转换为 进件贷款基础信息对象
     */
    public SplBussinessBasicObject cir2sbbo(CluesInformationRequest request) {
        SplBussinessBasicObject sbbo = new SplBussinessBasicObject();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        sbbo.setApplyNum(request.getOrderId()+"");
        sbbo.setLoanState(NOT_ACCEPT);
        sbbo.setChannelSource(BUSINESS_CHANNEL_PARTNER_58);
        sbbo.setRemark1(request.getLicensePlateNum());
        sbbo.setProvince(request.getProvinceCode());
        sbbo.setCity(request.getCityCode());
        sbbo.setBussinessType(AUTOMOBILE_MORTGAGE);
        sbbo.setCustomerModel(EAST_MODEL);
        sbbo.setLoanTerm(DEFAULT_TERM);
        sbbo.setLoanPrice(DEFAULT_AMOUNT.doubleValue());
        sbbo.setSubmitDateTi(new Timestamp(date.getTime()));
        sbbo.setSubmitDate(ConvertionUtil.getSimpleIntegerWithNull(sdf.format(date)));
        if ("".equals(ConvertionUtil.getSimpleStringWithNull(request.getCarValue()))){
            sbbo.setRemark3("车辆估值：--");
        }else{
            sbbo.setRemark3("车辆估值：" + cir2CarValue(request.getCarValue()));
        }

        return sbbo;
    }

    /**
     * 58线索信息结构对象 转换为 外部推送人员信息对象
     */
    public SplUserInfoObject cir2Suio(CluesInformationRequest request) {
        SplUserInfoObject suio = new SplUserInfoObject();
        suio.setUcity(Integer.parseInt(request.getCityCode()));
        suio.setUprovince(Integer.parseInt(request.getProvinceCode()));
        suio.setPhone(request.getPhone());
        suio.setCustomerName(request.getUserName());
        return suio;
    }

    public String cir2CarValue(String carValueParam){
        String carValue = "";
        switch (carValueParam){
            case "range1":
                carValue = "4万以下";
                break;
            case "range2":
                carValue = "4-10万";
                break;
            case "range3":
                carValue = "10-20万";
                break;
            case "range4":
                carValue = "20万以上";
                break;
            default:
                break;
        }
        return carValue;
    }
}
