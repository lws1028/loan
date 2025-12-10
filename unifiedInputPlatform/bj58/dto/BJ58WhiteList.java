package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.bj58.dto;

import com.zlhj.commonLoan.util.StringUtil;
import lombok.Getter;

/**
 * 58白名单校验
 */
@Getter
public class BJ58WhiteList {
    //省
    private final String province;
    //市
    private final String city;

    private BJ58WhiteList(String province, String city) {
        this.province = province;
        this.city = city;
    }

    public static BJ58WhiteList createWhiteList(String province, String city) {
        return new BJ58WhiteList(province, city);
    }

    public boolean isEmpty() {
        return StringUtil.isEmpty(city) || StringUtil.isEmpty(province);
    }
}
