package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.ant.enums;

import lombok.Getter;

@Getter
public enum RefuseReasonType {
    RR_SYS_OTHER("其他拒绝原因"),
    RR_USER_NO_WILL("无资金需求"),
    RR_USER_NO_ANSWER("无人接听"),
    RR_BASIC_AGE("年龄不符合要求"),
    RR_BASIC_CHE_MORTGAGED("车辆在押"),
    RR_OVD_PBOC_NOW("征信查询当前严重逾期"),
    RR_SYS_OVER_INT("利息超限"),
    RR_USER_NO_INTENTION("客户无意愿"),
    RR_USER_UNWILLING_MORTGAGE("客户不愿意抵押"),
    RR_BASIC_CHE_NOT_MATCH("车辆不符");
    private final String reason;

    RefuseReasonType(String reason) {
        this.reason = reason;
    }

}
