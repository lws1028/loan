package com.zlhj.unifiedInputPlatform.unifiedInputPlatform.bj58.dto;

import lombok.Setter;

/**
 * 58 运维 传入orderId，可传入多个，通过‘，’区分
 */
@Setter
public class CluesInformationOperationRequest {

    private String orderIds;

    public String[] getOrderId() {
        return orderIds.split(",");
    }
}
