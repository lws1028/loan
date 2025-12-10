package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.bj58.dto;

import lombok.Setter;

/**
 * 拒绝原因
 */
@Setter
public class ClueInformationRefuseReason {

    private String rejectReason;

    private String approveReason;

    public String getReason(Integer status) {
        if (-110 == status || -130 == status || -200 == status) {
            return this.rejectReason;
        }else if (-210 == status || -230 == status || -235 == status){
            return this.approveReason;
        }else {
            return "";
        }
    }
}
