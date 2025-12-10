package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.smy.dto;

import lombok.Data;

/**
 * 扩展信息字段
 *
 * @author : LXY
 * @date : 2023/6/2 16:30
 */
@Data
public class ExtraContent {

    private String adId;
    private String clickId;
    private String creativeId;
    private String creativeType;

    /**
     * 车牌号码
     */
    private String carId;

    /**
     * 车辆抵押状态(1-抵押中,2-未抵押)
     */
    private String pledge;

    /**
     * 信用情况(无逾期;一年内逾期小于3次;一年内逾期超过3次)
     */
    private String creditType;

    /**
     * 车辆价值 (小于10万,大于10万)
     */
    private String carMoney;
}
