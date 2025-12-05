package com.zlhj.unifiedInputPlatform.ant.dto;

import com.zlhj.unifiedInputPlatform.BaseClueApply;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ClueApplyInput extends BaseClueApply {

    /**
     * 客户车牌号，油车6位，新能源车7位括号仅做示例，传输值不包含
     */
    private String carLicense;

    /**
     * 客户购车方式，1：全款 2：分期-已结清，3：分期-未结清
     */
    private String payMethod;

    /**
     * 常住省份国标码
     */
    private String residenceProvinceCode;

    /**
     * 常住省份
     */
    private String residenceProvince;

    /**
     * 杭州市
     */
    private String residenceCityCode;

    /**
     * 常住城市
     */
    private String residenceCity;

    /**
     * 车扩展信息
     */
    private String carExt;

    /**
     * 人扩展信息
     */
    private String userExt;
}
