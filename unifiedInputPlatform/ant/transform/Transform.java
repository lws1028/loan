package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.ant.transform;

import org.springframework.stereotype.Component;

/**
 * 蚂蚁基础信息
 */
@Component
public class Transform {

    //合作渠道 - 3 - 58同城 （直客状态表码值）
    private final static int STATUS_CHANNEL_PARTNER_ANT = 6;
    //合作渠道 - 13 - 58同城 （进件贷款基础信息表码值）
    private final static int BUSINESS_CHANNEL_PARTNER_ANT = 16;

    public static int getChannelPartner() {
        return STATUS_CHANNEL_PARTNER_ANT;
    }

    public static int getBusinessChannelPartner() {
        return BUSINESS_CHANNEL_PARTNER_ANT;
    }

}
