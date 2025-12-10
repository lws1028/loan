package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.bj58.dto;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 58 运维 返回数据
 */
@Getter
public class CluesInformationOperationResponse {

    //成功的条数
    private Integer successTotal;
    //失败的申请编号
    private final List<ErrorApplyNum> errorApplyNum;

    public CluesInformationOperationResponse(Integer successTotal) {
        this.successTotal = successTotal;
        errorApplyNum = new ArrayList<>();
    }

    public void setOrder(String orderId, String msg) {
        errorApplyNum.add(new ErrorApplyNum(orderId, msg));
        this.successTotal --;
    }


    @Getter
    class ErrorApplyNum {
        final String orderId;

        final String msg;

        ErrorApplyNum(String orderId, String msg) {
            this.orderId = orderId;
            this.msg = msg;
        }
    }
}
